package group.msg.jpowermonitor.junit;

import group.msg.jpowermonitor.JPowerMonitorException;
import group.msg.jpowermonitor.dto.DataPoint;
import group.msg.jpowermonitor.dto.SensorValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Result writer for the JUnit extension.
 */
public class ResultsWriter {
    static {
        setLocaleDependentValues();
    }

    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd'T'HH:mm:ss-SSS");
    private static DecimalFormat DECIMAL_FORMAT;
    private final Path pathToMeasurementCsv, pathToResultCsv;
    private static ResourceBundle labels;
    private static String dataPointFormatCsv, sensorValueFormatCsv, powerSensorValueFormatCsv, SEP;

    public static void setLocaleDependentValues() {
        labels = ResourceBundle.getBundle("csvExport", Locale.getDefault());
        SEP = Locale.getDefault().getCountry().toLowerCase(Locale.ROOT).equals("de") ? ";" : ",";
        dataPointFormatCsv = Locale.getDefault().getCountry().toLowerCase(Locale.ROOT).equals("de") ? "%s;%s;%s;%s;%s%s" : "%s,%s,%s,%s,%s%s";
        sensorValueFormatCsv = Locale.getDefault().getCountry().toLowerCase(Locale.ROOT).equals("de") ? "%s;%s;%s;%s;%s;%s" : "%s,%s,%s,%s,%s,%s";
        powerSensorValueFormatCsv = Locale.getDefault().getCountry().toLowerCase(Locale.ROOT).equals("de") ? "%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s" : "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s";
        DECIMAL_FORMAT = new DecimalFormat("###0.#####", DecimalFormatSymbols.getInstance(Locale.getDefault()));
    }

    public ResultsWriter(@Nullable Path pathToResultCsv, @Nullable Path pathToMeasurementCsv) {
        this.pathToResultCsv = pathToResultCsv;
        this.pathToMeasurementCsv = pathToMeasurementCsv;
        if (pathToResultCsv != null && !pathToResultCsv.toFile().exists()) {
            createFile(pathToResultCsv);
            String headings = labels.getString("measureTime") + SEP + labels.getString("measureName") + SEP + labels.getString("sensorName") + SEP + labels.getString("sensorValue") + SEP
                + labels.getString("sensorValueUnit") + SEP + labels.getString("baseLoad") + SEP + labels.getString("baseLoadUnit") + SEP + labels.getString("valuePlusBaseLoad")
                + SEP + labels.getString("valuePlusBaseLoadUnit") + SEP + labels.getString("energyOfValue") + SEP + labels.getString("energyOfValueUnit") + SEP
                + labels.getString("energyOfValuePlusBaseLoad") + SEP + labels.getString("energyOfValuePlusBaseLoadUnit");
            appendToFile(pathToResultCsv, headings + NEW_LINE);
        }
        if (pathToMeasurementCsv != null && !pathToMeasurementCsv.toFile().exists()) {
            createFile(pathToMeasurementCsv);
            String headings = labels.getString("measureTime") + SEP + labels.getString("measureName") + SEP + labels.getString("sensorName") + SEP + labels.getString("sensorValue") + SEP
                + labels.getString("sensorValueUnit");
            appendToFile(pathToMeasurementCsv, headings + NEW_LINE);
        }
    }

    private void createFile(@NotNull Path fileToCreate) {
        try {
            if (fileToCreate.toFile().getParentFile() != null) {
                boolean createdDir = fileToCreate.toFile().getParentFile().mkdirs();
                if (createdDir) {
                    System.out.println("Created directory for writing the csv file to: " + fileToCreate.toFile().getParentFile().getAbsolutePath());
                }
            }
            Files.createFile(fileToCreate);
        } catch (IOException e) {
            throw new JPowerMonitorException("Unable to create file: " + fileToCreate, e);
        }
    }

    public void writeToMeasurementCsv(String testName, List<DataPoint> dataPoints) {
        writeToMeasurementCsv(testName, dataPoints, "");
    }

    public void writeToMeasurementCsv(String testName, List<DataPoint> dataPoints, String namePrefix) {
        if (pathToMeasurementCsv == null) {
            return; // do nothing, if path is not configured.
        }
        for (DataPoint dp : dataPoints) {
            String csvEntry = createCsvEntryForDataPoint(dp, namePrefix, testName);
            appendToFile(pathToMeasurementCsv, csvEntry);
        }
    }

    public static String createCsvEntryForDataPoint(@NotNull DataPoint dp, String namePrefix, String testName) {
        return String.format(dataPointFormatCsv,
            DATE_TIME_FORMATTER.format(dp.getTime()),
            namePrefix + testName,
            dp.getName(),
            DECIMAL_FORMAT.format(dp.getValue()),
            dp.getUnit(), NEW_LINE);
    }

    public void writeToResultCsv(String testName, SensorValue sensorValue) {
        if (pathToResultCsv == null) {
            return; // do nothing, if path is not configured.
        }
        String csvEntry = createCsvEntryForSensorValue(testName, sensorValue);
        appendToFile(pathToResultCsv, csvEntry);
    }

    private String createCsvEntryForSensorValue(String testName, SensorValue sensorValue) {
        String csvEntry;
        if (sensorValue.isPowerSensor()) { // only power values=>
            csvEntry = String.format(powerSensorValueFormatCsv, DATE_TIME_FORMATTER.format(sensorValue.getExecutionTime()), testName,
                sensorValue.getName(),
                formatNumber(sensorValue.getValue()), sensorValue.getUnit(),
                formatNumber(sensorValue.getPowerInIdleMode()), sensorValue.getUnit(),
                formatNumber(sensorValue.getValue().add(sensorValue.getPowerInIdleMode())), sensorValue.getUnit(),
                formatNumber(sensorValue.getValueWithoutIdlePowerPerHour()), sensorValue.getUnit() + "h",
                formatNumber(sensorValue.getValueWithIdlePowerPerHour()), sensorValue.getUnit() + "h",
                NEW_LINE);
        } else {
            csvEntry = String.format(sensorValueFormatCsv, DATE_TIME_FORMATTER.format(sensorValue.getExecutionTime()), testName,
                sensorValue.getName(), formatNumber(sensorValue.getValue()), sensorValue.getUnit(), NEW_LINE);
        }
        return csvEntry;
    }

    @NotNull
    private String formatNumber(BigDecimal val) {
        return DECIMAL_FORMAT.format(val);
    }

    private void appendToFile(@NotNull Path path, @NotNull String lineToAppend) {
        try {
            Files.writeString(path, lineToAppend, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Unable to append to csv file: " + path);
            e.printStackTrace();
        }
    }
}
