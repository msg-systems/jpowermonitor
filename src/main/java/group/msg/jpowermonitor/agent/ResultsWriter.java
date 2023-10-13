package group.msg.jpowermonitor.agent;

import group.msg.jpowermonitor.dto.Activity;
import group.msg.jpowermonitor.dto.DataPoint;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import static group.msg.jpowermonitor.config.DefaultConfigProvider.APP_TITLE;
import static group.msg.jpowermonitor.config.DefaultConfigProvider.MATH_CONTEXT;

/**
 * Write power and energy measurement results to CSV files at application shutdown.
 *
 * @author deinerj
 */
public class ResultsWriter implements Runnable {
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd'T'HH:mm:ss-SSS");
    private static final DecimalFormat DECIMAL_FORMAT;
    private static final String dataPointFormatCsv;
    private static final String dataPointFormatEnergyConsumptionCsv;
    private static final String UNIT_GRAMS_CO2 = "gCO2";

    static {
        dataPointFormatCsv = Locale.getDefault().getCountry().toLowerCase(Locale.ROOT).equals("de") ? "%s;%s;%s;%s;%s%s" : "%s,%s,%s,%s,%s%s";
        dataPointFormatEnergyConsumptionCsv = Locale.getDefault().getCountry().toLowerCase(Locale.ROOT).equals("de") ? "%s;%s;%s;%s;%s;%s;%s%s" : "%s,%s,%s,%s,%s,%s,%s%s";
        DECIMAL_FORMAT = new DecimalFormat("###0.#####", DecimalFormatSymbols.getInstance(Locale.getDefault()));
    }

    private static final BigDecimal JOULE_TO_WATT_HOURS_FACTOR = new BigDecimal("3600");
    private static final BigDecimal WATT_HOURS_TO_KWH_FACTOR = new BigDecimal("1000");
    protected static final String FILE_NAME_PREFIX = APP_TITLE + "_";
    protected static final String SEPARATOR = "-----------------------------------------------------------------------------------------";

    private final PowerStatistics powerStatistics;
    private final boolean doWriteStatistics;
    private final BigDecimal carbonDioxideEmissionFactor;

    private String energyConsumptionPerMethodFileName;
    private String energyConsumptionPerFilteredMethodFileName;
    private String powerConsumptionPerMethodFileName;
    private String powerConsumptionPerFilteredMethodFileName;

    /**
     * Constructor
     *
     * @param powerStatistics             energy consumption measurements
     * @param doWriteStatistics           set 'true' if this is shutdown hook - logs some statistics
     * @param carbonDioxideEmissionFactor conversion factor to calculate CO2 usage from energy usage
     */
    public ResultsWriter(PowerStatistics powerStatistics, boolean doWriteStatistics, BigDecimal carbonDioxideEmissionFactor) {
        this.powerStatistics = powerStatistics;
        this.doWriteStatistics = doWriteStatistics;
        this.carbonDioxideEmissionFactor = carbonDioxideEmissionFactor;
        initCsvFileNames();
    }

    @Override
    public void run() {
        execute();
    }

    public void execute() {
        writeEnergyConsumptionToCsv();
        logStatistics();
    }

    private void initCsvFileNames() {
        energyConsumptionPerMethodFileName = FILE_NAME_PREFIX + powerStatistics.getPid() + "_energy_per_method.csv";
        energyConsumptionPerFilteredMethodFileName = FILE_NAME_PREFIX + powerStatistics.getPid() + "_energy_per_method_filtered.csv";
        powerConsumptionPerMethodFileName = FILE_NAME_PREFIX + powerStatistics.getPid() + "_power_per_method.csv";
        powerConsumptionPerFilteredMethodFileName = FILE_NAME_PREFIX + powerStatistics.getPid() + "_power_per_method_filtered.csv";
    }

    private void writeEnergyConsumptionToCsv() {
        createUnfilteredAndFilteredPowerConsumptionPerMethodCsvAndWriteToFiles(powerStatistics.getEnergyConsumptionPerMethod(false), energyConsumptionPerMethodFileName);
        createUnfilteredAndFilteredPowerConsumptionPerMethodCsvAndWriteToFiles(powerStatistics.getEnergyConsumptionPerMethod(true), energyConsumptionPerFilteredMethodFileName);
    }

    private void logStatistics() {
        if (doWriteStatistics && powerStatistics != null) {
            System.out.println(SEPARATOR);
            System.out.println("JPowerMonitorAgent successfully finished monitoring application with PID " + powerStatistics.getPid());
            logStatisticsCommon(System.out::println);
        }
    }

    private void logStatisticsCommon(Consumer<String> prioritizedLogger) {
        if (prioritizedLogger == null || powerStatistics == null
            || powerStatistics.getEnergyConsumptionTotalInJoule() == null
            || powerStatistics.getEnergyConsumptionTotalInJoule().get() == null
            || powerStatistics.getEnergyConsumptionTotalInJoule().get().getValue() == null) {
            return;
        }
        prioritizedLogger.accept(String.format("Application consumed %.2f joule - %.3f wh - %.6f kwh - %.3f gCO2 total",
            powerStatistics.getEnergyConsumptionTotalInJoule().get().getValue()
            , convertJouleToWattHours(powerStatistics.getEnergyConsumptionTotalInJoule().get().getValue())
            , convertJouleToKiloWattHours(powerStatistics.getEnergyConsumptionTotalInJoule().get().getValue())
            , convertJouleToCarbonDioxideGrams(powerStatistics.getEnergyConsumptionTotalInJoule().get().getValue(), carbonDioxideEmissionFactor)));
        prioritizedLogger.accept("Energy consumption per method and filtered methods written to '" + energyConsumptionPerMethodFileName + "' / '" + energyConsumptionPerFilteredMethodFileName + "'");
        prioritizedLogger.accept(SEPARATOR);
    }


    public void createUnfilteredAndFilteredPowerConsumptionPerMethodCsvAndWriteToFiles(Map<String, DataPoint> measurements, String fileName) {
        writeToFile(createCsv(measurements), fileName);
    }

    public void createUnfilteredAndFilteredPowerConsumptionPerMethodCsvAndWriteToFiles(Collection<Activity> measurements) {
        writeToFile(createCsv(powerStatistics.aggregateActivityToDataPoints(measurements, false)), powerConsumptionPerMethodFileName, true);
        writeToFile(createCsv(powerStatistics.aggregateActivityToDataPoints(measurements, true)), powerConsumptionPerFilteredMethodFileName, true);
    }

    protected String createCsv(Map<String, DataPoint> measurements) {
        StringBuilder csv = new StringBuilder();
        measurements.forEach((method, energy) -> csv.append(createCsvEntryForDataPoint(energy)));
        return csv.toString();
    }

    protected String createCsvEntryForDataPoint(@NotNull DataPoint dp) {
        if (Unit.JOULE == dp.getUnit()) {
            return String.format(dataPointFormatEnergyConsumptionCsv, DATE_TIME_FORMATTER.format(dp.getTime()), dp.getThreadName(), dp.getName(), DECIMAL_FORMAT.format(dp.getValue()), dp.getUnit(),
                DECIMAL_FORMAT.format(convertJouleToCarbonDioxideGrams(dp.getValue(), carbonDioxideEmissionFactor)), UNIT_GRAMS_CO2, NEW_LINE);
        }
        return String.format(dataPointFormatCsv, DATE_TIME_FORMATTER.format(dp.getTime()), dp.getThreadName(), dp.getName(), DECIMAL_FORMAT.format(dp.getValue()), dp.getUnit(), NEW_LINE);
    }

    protected void writeToFile(String csv, String fileName) {
        writeToFile(csv, fileName, false);
    }

    protected void writeToFile(String csv, String fileName, boolean append) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, append))) {
            bw.write(csv);
        } catch (IOException ex) {
            System.err.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }

    protected BigDecimal convertJouleToWattHours(BigDecimal joule) {
        return joule.divide(JOULE_TO_WATT_HOURS_FACTOR, MATH_CONTEXT);
    }

    protected BigDecimal convertJouleToKiloWattHours(BigDecimal joule) {
        return convertJouleToWattHours(joule).divide(WATT_HOURS_TO_KWH_FACTOR, MATH_CONTEXT);
    }

    protected BigDecimal convertKiloWattHoursToCarbonDioxideGrams(BigDecimal kWh, BigDecimal energyMix) {
        return kWh.multiply(energyMix, MATH_CONTEXT);
    }

    protected BigDecimal convertJouleToCarbonDioxideGrams(BigDecimal joule, BigDecimal energyMix) {
        return convertKiloWattHoursToCarbonDioxideGrams(convertJouleToKiloWattHours(joule), energyMix);
    }

}
