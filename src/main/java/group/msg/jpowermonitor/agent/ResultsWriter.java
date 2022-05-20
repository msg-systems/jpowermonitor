package group.msg.jpowermonitor.agent;

import group.msg.jpowermonitor.dto.Activity;
import group.msg.jpowermonitor.dto.DataPoint;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static group.msg.jpowermonitor.agent.Utils.FILE_NAME_PREFIX;
import static group.msg.jpowermonitor.agent.Utils.SEPARATOR;
import static group.msg.jpowermonitor.agent.Utils.convertJouleToKiloWattHours;
import static group.msg.jpowermonitor.agent.Utils.convertJouleToWattHours;
import static group.msg.jpowermonitor.agent.Utils.createCsvAndWriteToFile;

/**
 * Write power and energy measurement results to CSV files at application shutdown.
 *
 * @author deinerj
 */
@Slf4j
public class ResultsWriter implements Runnable {

    private final PowerStatistics powerStatistics;
    private final boolean doWriteStatistics;

    private String energyConsumptionPerMethodFileName;
    private String energyConsumptionPerFilteredMethodFileName;

    /**
     * Constructor
     *
     * @param powerStatistics energy consumption measurements
     * @param doWriteStatistics set 'true' if this is shutdown hook - logs some statistics
     */
    public ResultsWriter(PowerStatistics powerStatistics, boolean doWriteStatistics) {
        this.powerStatistics = powerStatistics;
        this.doWriteStatistics = doWriteStatistics;
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
    }

    private void writeEnergyConsumptionToCsv() {
        List<Activity> recentActivity = powerStatistics == null ? null : powerStatistics.getRecentActivity();
        if (recentActivity == null || recentActivity.isEmpty()) {
            return;
        }
        createCsvAndWriteToFile(aggregateActivity(recentActivity, false), energyConsumptionPerMethodFileName);
        createCsvAndWriteToFile(aggregateActivity(recentActivity, true), energyConsumptionPerFilteredMethodFileName);
    }

    protected static Map<String, DataPoint> aggregateActivity(List<Activity> activitySet, boolean filtered) {
        return activitySet.stream()
            .filter(activity -> activity.getIdentifier(filtered) != null)
            .collect(Collectors.toMap(
                activity -> activity.getIdentifier(filtered),
                activity -> new DataPoint(activity.getIdentifier(filtered), activity.getRepresentedQuantity().getValue(), activity.getRepresentedQuantity().getUnit(), LocalDateTime.now()),
                (current, next) -> new DataPoint(current.getName(), current.getValue().add(next.getValue()), current.getUnit(), current.getTime())
            ));
    }

    private void logStatistics() {
        if (doWriteStatistics && powerStatistics != null) {
            log.info(SEPARATOR);
            log.info("JPowerMonitorAgent successfully finished monitoring application with PID {}", powerStatistics.getPid());
            logStatisticsCommon(log::info);
        } else if (log.isTraceEnabled()) {
            logStatisticsCommon(log::trace);
        }
    }

    private void logStatisticsCommon(Consumer<String> prioritizedLogger) {
        if (prioritizedLogger == null || powerStatistics == null
            || powerStatistics.getEnergyConsumptionTotalInJoule() == null
            || powerStatistics.getEnergyConsumptionTotalInJoule().get() == null
            || powerStatistics.getEnergyConsumptionTotalInJoule().get().getValue() == null) {
            return;
        }
        prioritizedLogger.accept(String.format("Application consumed %.2f joule - %.3f wh - %.6f kwh total",
            powerStatistics.getEnergyConsumptionTotalInJoule().get().getValue()
            , convertJouleToWattHours(powerStatistics.getEnergyConsumptionTotalInJoule().get().getValue().doubleValue())
            , convertJouleToKiloWattHours(powerStatistics.getEnergyConsumptionTotalInJoule().get().getValue().doubleValue())));
        prioritizedLogger.accept("Energy consumption per method and filtered methods written to '" + energyConsumptionPerMethodFileName + "' / '" + energyConsumptionPerFilteredMethodFileName + "'");
        prioritizedLogger.accept(SEPARATOR);
    }
}
