package group.msg.jpowermonitor.measurement.csv;

import group.msg.jpowermonitor.JPowerMonitorException;
import group.msg.jpowermonitor.MeasureMethod;
import group.msg.jpowermonitor.agent.Unit;
import group.msg.jpowermonitor.config.CsvColumn;
import group.msg.jpowermonitor.config.JPowerMonitorConfig;
import group.msg.jpowermonitor.dto.DataPoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommaSeparatedValuesReader implements MeasureMethod {
    private final JPowerMonitorConfig config;

    public CommaSeparatedValuesReader(JPowerMonitorConfig config) {
        this.config = config;
        validateCsvConfig(config);
    }

    private void validateCsvConfig(JPowerMonitorConfig config) {
        Path csvInputFile = config.getMeasurement().getCsv().getInputFileAsPath();
        if (!Files.isRegularFile(csvInputFile) || !Files.isReadable(csvInputFile)) {
            throw new JPowerMonitorException("No measurements csv file found at '" + csvInputFile.toAbsolutePath().normalize() + "'");
        }
    }

    @Override
    public @NotNull List<DataPoint> measure() throws JPowerMonitorException {
        return List.of(measureFirstConfiguredPath());
    }

    @Override
    public @NotNull DataPoint measureFirstConfiguredPath() throws JPowerMonitorException {
        Path csvInputFile = config.getMeasurement().getCsv().getInputFileAsPath();

        //( BufferedReader reader = Files.newBufferedReader(csvInputFile, config.getMeasurement().getCsv().getEncodingAsCharset())
        try {
            System.out.println("Trying to read measurements from file '" + csvInputFile.toAbsolutePath().normalize() + "' using encoding " + config.getMeasurement().getCsv().getEncodingAsCharset().displayName());
            String lastLine = readLastLine(csvInputFile.toFile(), config.getMeasurement().getCsv().getEncodingAsCharset());
            // String lastLine="", tmp;
            // while ((tmp = reader.readLine()) != null) {
            //    lastLine = tmp;
            // }

            String[] values = lastLine.split(config.getMeasurement().getCsv().getDelimiter());
            BigDecimal value = new BigDecimal(values[config.getMeasurement().getCsv().getColumns().get(0).getIndex()]);
            return new DataPoint(config.getMeasurement().getCsv().getInputFile(), value, Unit.WATT, LocalDateTime.now(), null);
        } catch (IOException ex) {
            throw new JPowerMonitorException("Cannot read measurements from file '" + csvInputFile.toAbsolutePath().normalize() + "'");
        }
    }

    static byte[] reverseArray(byte array[], int n) {
        byte[] destArray = new byte[n];
        int j = n;
        for (int i = 0; i < n; i++) {
            destArray[j - 1] = array[i];
            j = j - 1;
        }
        return destArray;
    }

    String readLastLine(File file, Charset encoding) throws IOException {
        try (RandomAccessFile fileHandler = new RandomAccessFile(file, "r")) {
            int counter = 0;
            long fileLength = fileHandler.length() - 1;
            int SIZE = 4096;
            byte[] bytes = new byte[SIZE];
            for (long filePointer = fileLength; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);
                byte readByte = fileHandler.readByte();
                if (readByte == 0xA) { // \n
                    if (filePointer == fileLength) {
                        continue;
                    }
                    break;
                } else if (readByte == 0xD) { // \r
                    if (filePointer == fileLength - 1) {
                        continue;
                    }
                    break;
                }
                bytes[counter] = readByte;
                if (counter > SIZE - 2) {
                    byte[] copy = new byte[bytes.length + bytes.length];
                    System.arraycopy(bytes, 0, copy, 0, bytes.length);
                    bytes = copy;
                }
                counter++;
            }
            if (counter < SIZE - 1) {
                byte[] copy = new byte[counter + 1];
                System.arraycopy(bytes, 0, copy, 0, counter + 1);
                bytes = copy;
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
