package group.msg.jpowermonitor.measurement.csv;

import group.msg.jpowermonitor.JPowerMonitorException;
import group.msg.jpowermonitor.MeasureMethod;
import group.msg.jpowermonitor.agent.Unit;
import group.msg.jpowermonitor.config.CsvColumn;
import group.msg.jpowermonitor.config.CsvMeasurementCfg;
import group.msg.jpowermonitor.config.JPowerMonitorConfig;
import group.msg.jpowermonitor.dto.DataPoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommaSeparatedValuesReader implements MeasureMethod {
    private final JPowerMonitorConfig config;

    public CommaSeparatedValuesReader(JPowerMonitorConfig config) {
        this.config = config;
        initCsvConfig(config);
    }

    private void initCsvConfig(JPowerMonitorConfig config) {
        CsvMeasurementCfg csvConfig = config.getMeasurement().getCsv();
        Path csvInputPath = Stream.of(
                (Supplier<Path>) () -> this.tryReadingFromFileSystem(csvConfig.getInputFile()),
                () -> this.tryReadingFromResources(csvConfig.getInputFile()),
                () -> findFileIgnoringCase(Path.of("."), csvConfig.getInputFile()))
            .map(Supplier::get)
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new JPowerMonitorException(String.format("Unable to read csv input file from source '%s'", csvConfig.getInputFile())));
        csvConfig.setInputFileAsPath(csvInputPath);
    }

    public Path findFileIgnoringCase(Path path, String fileName) {
        System.out.println("Reading csv input file from given source '" + fileName + "' on path " + path);
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a directory!");
        }
        try (Stream<Path> walk = Files.walk(path)) {
            return walk
                .filter(Files::isReadable)
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().equalsIgnoreCase(fileName)).findFirst().orElse(null);
        } catch (IOException e) {
            return null;
        }
    }

    private Path tryReadingFromFileSystem(String source) {
        System.out.println("Reading csv input file from filesystem: '" + source + "'");
        Path path = Paths.get(source);
        if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
            System.out.println("'" + source + "' is not a regular file or not readable, it will not be read from filesystem");
            return null;
        }
        return path;
    }

    private Path tryReadingFromResources(String source) {
        System.out.println("Reading csv input file from resources: '" + source + "'");
        URL inputUrl = CommaSeparatedValuesReader.class.getClassLoader().getResource(source);
        if (inputUrl == null) {
            System.out.println("'" + source + "' is not available as resource");
            return null;
        }
        try {
            return Paths.get(inputUrl.toURI());
        } catch (URISyntaxException e) {
            System.out.println("'" + source + "' URL cannot be converted to URI");
            return null;
        }
    }

    @Override
    public @NotNull List<DataPoint> measure() throws JPowerMonitorException {
        return List.of(measureFirstConfiguredPath());
    }

    @Override
    public @NotNull DataPoint measureFirstConfiguredPath() throws JPowerMonitorException {
        Path csvInputFile = config.getMeasurement().getCsv().getInputFileAsPath();
        try {
            String lastLine = "last".equalsIgnoreCase(config.getMeasurement().getCsv().getLineToRead()) ?
                readLastLine(csvInputFile, config.getMeasurement().getCsv().getEncodingAsCharset()) :
                readFirstLine(csvInputFile, config.getMeasurement().getCsv().getEncodingAsCharset());
            String[] values = lastLine.split(config.getMeasurement().getCsv().getDelimiter());
            CsvColumn column = config.getMeasurement().getCsv().getColumns().get(0);
            BigDecimal value = parseBigDecimalFromColumnConfig(csvInputFile, values[column.getIndex()]);
            return new DataPoint(column.getName(), value, Unit.WATT, LocalDateTime.now(), null);
        } catch (IOException ex) {
            throw new JPowerMonitorException("Cannot read measurements from file '" + csvInputFile.toAbsolutePath().normalize() + "'");
        }
    }

    @NotNull
    private BigDecimal parseBigDecimalFromColumnConfig(Path csvInputFile, String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            throw new JPowerMonitorException("Cannot read measurements from file '" + csvInputFile.toAbsolutePath().normalize() +
                "'. Unable to parse '" + value + "' as a number!");
        }
    }

    String readFirstLine(Path csvInputFile, Charset encoding) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(csvInputFile, encoding)) {
            return reader.readLine();
        }
    }

    static byte[] reverseArray(byte[] array, int n) {
        byte[] destinationArray = new byte[n];
        int j = n;
        for (int i = 0; i < n; i++) {
            destinationArray[j - 1] = array[i];
            j = j - 1;
        }
        return destinationArray;
    }

    private byte[] resizeArray(byte[] bytes, int newLength) {
        byte[] copy = new byte[newLength];
        System.arraycopy(bytes, 0, copy, 0, Math.min(bytes.length, newLength)); // to avoid outofbounds: use at most bytes.length
        return copy;
    }

    String readLastLine(Path inputPath, Charset encoding) throws IOException {
        final int BUFFER_SIZE = 8192;
        try (RandomAccessFile fileHandler = new RandomAccessFile(inputPath.toFile(), "r")) {
            int counter = 0;
            long fileLength = fileHandler.length() - 1;

            int reSized = 1;
            byte[] bytes = new byte[BUFFER_SIZE];
            // Read file backwards, starting with the last byte until the line break:
            for (long filePointer = fileLength; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);
                byte readByte = fileHandler.readByte();
                if (readByte == 0xA) { // \LF
                    if (filePointer == fileLength) {
                        continue;
                    }
                    break;
                } else if (readByte == 0xD) { // \CR
                    if (filePointer == fileLength - 1) {
                        continue;
                    }
                    break;
                }
                bytes[counter] = readByte;
                if (counter > (BUFFER_SIZE * reSized) - 2) { // if array is too small: expand it
                    bytes = resizeArray(bytes, 2 * bytes.length);
                    reSized++;
                }
                counter++;
            }
            if (counter < (BUFFER_SIZE * reSized) - 1) { // shrink the array to the read bytes.
                bytes = resizeArray(bytes, counter);
            }
            return new String(reverseArray(bytes, bytes.length), encoding);
        }
    }

    @Override
    public @NotNull List<String> configuredSensors() {
        return config.getMeasurement().getCsv().getColumns().stream().map(CsvColumn::getName).collect(Collectors.toList()); // only ohm
    }

    @Override
    public @NotNull Map<String, BigDecimal> defaultEnergyInIdleModeForMeasuredSensors() {
        return new HashMap<>(); // only ohm
    }

    // same as ohwm TODO remove duplication
    @Override
    public int getSamplingInterval() {
        return config.getSamplingIntervalInMs();
    }

    @Override
    public int initCycles() {
        return config.getInitCycles();
    }

    @Override
    public int getSamplingIntervalForInit() {
        return config.getSamplingIntervalForInitInMs();
    }

    @Override
    public int getCalmDownIntervalInMs() {
        return config.getCalmDownIntervalInMs();
    }

    @Override
    public @Nullable Path getPathToResultCsv() {
        return config.getCsvRecording().getResultCsv() != null ? Paths.get(
            config.getCsvRecording().getResultCsv()) : null;
    }

    @Override
    public @Nullable Path getPathToMeasurementCsv() {
        return config.getCsvRecording().getMeasurementCsv() != null ? Paths.get(
            config.getCsvRecording().getMeasurementCsv()) : null;
    }

    @Override
    public @NotNull BigDecimal getPercentageOfSamplesAtBeginningToDiscard() {
        return config.getPercentageOfSamplesAtBeginningToDiscard();
    }
}
