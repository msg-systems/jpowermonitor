package group.msg.jpowermonitor.junit;

import group.msg.jpowermonitor.JPowerMonitorException;
import group.msg.jpowermonitor.agent.Unit;
import group.msg.jpowermonitor.dto.DataPoint;
import group.msg.jpowermonitor.dto.SensorValue;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static group.msg.jpowermonitor.util.Constants.DATE_TIME_FORMATTER;
import static group.msg.jpowermonitor.util.Constants.NEW_LINE;
import static group.msg.jpowermonitor.util.Converter.convertJouleToCarbonDioxideGrams;
import static group.msg.jpowermonitor.util.Converter.convertWattHoursToJoule;

/**
 * Result writer for the JUnit extension.
 */
@Slf4j
public class JUnitResultsWriter {

    static {
        setLocaleDependentValues();
    }

    private static DecimalFormat DECIMAL_FORMAT;
    private final Path pathToMeasurementCsv, pathToResultCsv;
    private final Double carbonDioxideEmissionFactor;
    private static ResourceBundle labels;
    private static String dataPointFormatCsv, nonPowerSensorResultFormatCsv, powerSensorResultFormatCsv, SEP;
    private static final String DATA_POINT_FORMAT = "%s;%s;%s;%s;%s%s";
    private static final String NON_POWER_SENSOR_RESULT_FORMAT = "%s;%s;%s;%s;%s;%s";
    private static final String POWER_SENSOR_RESULT_FORMAT = "%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s";

    public static void setLocaleDependentValues() {
        labels = ResourceBundle.getBundle("csvExport", Locale.getDefault());
        SEP = Locale.getDefault().getCountry().toLowerCase(Locale.ROOT).equals("de") ? ";" : ",";
        dataPointFormatCsv = setCorrectDelimiter(DATA_POINT_FORMAT);
        nonPowerSensorResultFormatCsv = setCorrectDelimiter(NON_POWER_SENSOR_RESULT_FORMAT);
        powerSensorResultFormatCsv = setCorrectDelimiter(POWER_SENSOR_RESULT_FORMAT);
        DECIMAL_FORMAT = new DecimalFormat("###0.#####", DecimalFormatSymbols.getInstance(Locale.getDefault()));
    }

    private static String setCorrectDelimiter(String format) {
        return Locale.getDefault().getCountry().toLowerCase(Locale.ROOT).equals("de") ? format : format.replace(';', ',');
    }

    public JUnitResultsWriter(@Nullable Path pathToResultCsv, @Nullable Path pathToMeasurementCsv, @NotNull Double carbonDioxideEmissionFactor) {
        this.pathToResultCsv = pathToResultCsv;
        this.pathToMeasurementCsv = pathToMeasurementCsv;
        this.carbonDioxideEmissionFactor = carbonDioxideEmissionFactor;
        if (pathToResultCsv != null && !pathToResultCsv.toFile().exists()) {
            createFile(pathToResultCsv);
            String headings = labels.getString("measureTime") + SEP + labels.getString("measureName") + SEP + labels.getString("sensorName") + SEP + labels.getString("sensorValue") + SEP
                              + labels.getString("sensorValueUnit") + SEP + labels.getString("baseLoad") + SEP + labels.getString("baseLoadUnit") + SEP + labels.getString("valuePlusBaseLoad")
                              + SEP + labels.getString("valuePlusBaseLoadUnit") + SEP + labels.getString("energyOfValue") + SEP + labels.getString("energyOfValueUnit") + SEP
                              + labels.getString("energyOfValuePlusBaseLoad") + SEP + labels.getString("energyOfValuePlusBaseLoadUnit") + SEP + labels.getString("co2Value") + SEP + labels.getString("co2Unit");
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
                    log.debug("Created directory for writing the csv file to: {}", fileToCreate.toFile().getParentFile().getAbsolutePath());
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
            double valueWithoutIdlePowerJ = convertWattHoursToJoule(sensorValue.getValueWithoutIdlePowerPerHour());
            double valueWithIdlePowerJ = convertWattHoursToJoule(sensorValue.getValueWithIdlePowerPerHour());
            double co2Equivalent = convertJouleToCarbonDioxideGrams(sensorValue.getValueWithIdlePowerPerHour(), carbonDioxideEmissionFactor);
            csvEntry = String.format(powerSensorResultFormatCsv,
                DATE_TIME_FORMATTER.format(sensorValue.getExecutionTime()),
                testName,
                sensorValue.getName(),
                formatNumber(sensorValue.getValue()),
                sensorValue.getUnit(),
                formatNumber(sensorValue.getPowerInIdleMode()),
                sensorValue.getUnit(),
                formatNumber(sensorValue.getValue() + sensorValue.getPowerInIdleMode()),
                sensorValue.getUnit(),
                formatNumber(valueWithoutIdlePowerJ),
                Unit.JOULE.getAbbreviation(),
                formatNumber(valueWithIdlePowerJ),
                Unit.JOULE.getAbbreviation(),
                formatNumber(co2Equivalent),
                Unit.GRAMS_CO2.getAbbreviation(),
                NEW_LINE);
        } else {
            csvEntry = String.format(nonPowerSensorResultFormatCsv, DATE_TIME_FORMATTER.format(sensorValue.getExecutionTime()), testName,
                sensorValue.getName(), formatNumber(sensorValue.getValue()), sensorValue.getUnit(), NEW_LINE);
        }
        return csvEntry;
    }

    @NotNull
    private String formatNumber(double val) {
        return DECIMAL_FORMAT.format(val);
    }

    private void appendToFile(@NotNull Path path, @NotNull String lineToAppend) {
        try {
            Files.writeString(path, lineToAppend, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.error("Unable to append to csv file: {}", path, e);
        }
    }
}
