package group.msg.jpowermonitor.junit;

import group.msg.jpowermonitor.MeasureMethod;
import group.msg.jpowermonitor.MeasureMethodProvider;
import group.msg.jpowermonitor.agent.Unit;
import group.msg.jpowermonitor.config.DefaultConfigProvider;
import group.msg.jpowermonitor.config.JPowerMonitorConfig;
import group.msg.jpowermonitor.dto.DataPoint;
import group.msg.jpowermonitor.dto.PowerQuestionable;
import group.msg.jpowermonitor.dto.SensorValue;
import group.msg.jpowermonitor.dto.SensorValues;
import group.msg.jpowermonitor.ohwm.MeasureOpenHwMonitor;
import group.msg.jpowermonitor.util.HumanReadableTime;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implements AfterTestExecutionCallback in order to be able to access results in the @AfterEach method in the test class.
 * AfterEachCallback would be too late, since @AfterEach is called before this callback.
 */
public class JPowerMonitorExtension implements BeforeAllCallback, BeforeEachCallback, AfterTestExecutionCallback {
    private final static MathContext mathContext = new MathContext(30, RoundingMode.HALF_UP);
    private final Map<String, List<DataPoint>> powerMeasurements = new HashMap<>();
    private Timer timer;
    private TimerTask timedMeasurement;
    private long timeBeforeTest;
    private MeasureMethod measureMethod;
    private Map<String, BigDecimal> energyInIdleMode;
    private ResultsWriter resultsWriter;

    @Override
    public void beforeAll(ExtensionContext context) {
        String configFile = context.getTestClass().map(c -> c.getSimpleName() + ".yaml").orElse(null);
        JPowerMonitorConfig config = new DefaultConfigProvider().readConfig(configFile);
        measureMethod = MeasureMethodProvider.resolveMeasureMethod(config);
        resultsWriter = new ResultsWriter(measureMethod.getPathToResultCsv(), measureMethod.getPathToMeasurementCsv());
        energyInIdleMode = measureIdleMode();
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        powerMeasurements.clear();
        measureMethod.configuredSensors().forEach(k -> powerMeasurements.put(k, new ArrayList<>())); // init result map with configured keys
        System.out.println("sleeping for " + measureMethod.getCalmDownIntervalInMs() + "ms in order to calm down");
        TimeUnit.MILLISECONDS.sleep(measureMethod.getCalmDownIntervalInMs());
        timer = new Timer();
        timedMeasurement = new TimerTask() {
            @Override
            public void run() {
                List<DataPoint> dataPoints = measureMethod.measure();
                dataPoints.forEach(dp -> powerMeasurements.get(dp.getName()).add(dp));
            }
        };
        timer.schedule(timedMeasurement, measureMethod.getSamplingInterval(), measureMethod.getSamplingInterval());
        timeBeforeTest = System.nanoTime();
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        long timeTaken = System.nanoTime() - timeBeforeTest;
        timedMeasurement.cancel();
        timer.cancel();
        timer.purge();
        String testName = getTestName(context);
        List<SensorValue> sensorValues = new ArrayList<>();
        for (Map.Entry<String, List<DataPoint>> entry : powerMeasurements.entrySet()) {
            List<DataPoint> dataPoints = entry.getValue();
            // cut off the first x% measurements
            List<DataPoint> dataPointsToConsider = dataPoints.subList(firstXPercent(dataPoints.size()), dataPoints.size());
            DataPoint average = calculateAvg(dataPointsToConsider);
            SensorValue sensorValue = calculateResult(timeTaken, energyInIdleMode.get(entry.getKey()), average);
            sensorValues.add(sensorValue);
            logSensorValue(testName, sensorValue);
            resultsWriter.writeToMeasurementCsv(testName, dataPointsToConsider);
            resultsWriter.writeToResultCsv(testName, sensorValue);
        }
        setSensorValueIntoAnnotatedFields(context, sensorValues);
    }

    private void logSensorValue(String testName, SensorValue sensorValue) {
        if (sensorValue.isPowerSensor()) {
            System.out.printf("%s: energy consumption for %s is %s %s, that is %s Wh for %s%n", testName, sensorValue.getName(), sensorValue.getValue(),
                sensorValue.getUnit(), sensorValue.getValueWithoutIdlePowerPerHour(), HumanReadableTime.ofNanos(sensorValue.getDurationOfTestInNanoSeconds()));
        } else {
            System.out.printf("%s: sensor value for %s is %s %s%n", testName, sensorValue.getName(), sensorValue.getValue(), sensorValue.getUnit());
        }
    }

    private void setSensorValueIntoAnnotatedFields(ExtensionContext context, List<SensorValue> sensorValues) {
        // Get the list of test instances (instances of test classes)
        final List<Object> testInstances = context.getRequiredTestInstances().getAllInstances();
        Set<Field> sensorValueFields = new HashSet<>();
        for (Object testInst : testInstances) {
            for (Field field : testInst.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(SensorValues.class) && field.getType().isAssignableFrom(List.class)) {
                    sensorValueFields.add(field);
                }
            }
            for (Field field : sensorValueFields) {
                try {
                    field.setAccessible(true);
                    field.set(testInst, sensorValues);
                } catch (Exception e) {
                    System.err.printf("Unable to set sensor values into @SensorValues annotated field %s on class %s: %s%n",
                        field.getName(), testInst.getClass(), e.getMessage());
                }
            }
        }
    }

    private SensorValue calculateResult(long timeTaken, BigDecimal powerInIdleMode, DataPoint dp) {
        BigDecimal valueWithoutIdlePower = dp.getValue().subtract(powerInIdleMode);
        BigDecimal valuePerHour = dp.isPowerSensor() ?
            valueWithoutIdlePower.multiply(HumanReadableTime.nanosToHours(timeTaken), mathContext).setScale(5, mathContext.getRoundingMode())
            : BigDecimal.ZERO;
        BigDecimal valueWithIdlePowerPerHour = dp.isPowerSensor() ?
            dp.getValue().multiply(HumanReadableTime.nanosToHours(timeTaken), mathContext).setScale(5, mathContext.getRoundingMode())
            : BigDecimal.ZERO;
        return SensorValue.builder().name(dp.getName())
            .value(valueWithoutIdlePower)
            .unit(dp.getUnit())
            .executionTime(dp.getTime())
            .durationOfTestInNanoSeconds(timeTaken)
            .valueWithoutIdlePowerPerHour(valuePerHour)
            .valueWithIdlePowerPerHour(valueWithIdlePowerPerHour)
            .powerInIdleMode(powerInIdleMode)
            .build();
    }

    private Map<String, BigDecimal> measureIdleMode() {
        Map<String, BigDecimal> defaults = measureMethod.defaultEnergyInIdleModeForMeasuredSensors();
        defaults.forEach((k, v) -> System.out.printf("(configured) energy consumption in idle mode for %s is %s%n", k, v));
        if (defaults.size() == measureMethod.configuredSensors().size()) {
            return defaults; // then we are done
        }
        // Else fill up the values with own measurements:
        Map<String, List<DataPoint>> measurements = new HashMap<>();
        measureMethod.configuredSensors().forEach(k -> measurements.put(k, new ArrayList<>())); // init result map with configured keys

        List<DataPoint> dataPointsSample = measureMethod.measure(); // do one measurement in order to find out, which configured sensors are power sensors...
        Set<String> powerSensorNames = dataPointsSample.stream().filter(PowerQuestionable::isPowerSensor).map(DataPoint::getName).collect(Collectors.toSet());
        Set<String> defaultsNames = defaults.keySet();
        if (powerSensorNames.equals(defaultsNames)) {
            Set<String> nonPowerSensorNames = dataPointsSample.stream().filter(x -> !x.isPowerSensor()).map(DataPoint::getName).collect(Collectors.toSet());
            nonPowerSensorNames.forEach(x -> defaults.putIfAbsent(x, BigDecimal.ZERO)); // add zero, if not a power sensor otherwise the value is not initialized in the map!
            return defaults; // then we are done
        }

        long timeBeforeTest = System.nanoTime();
        for (int i = 0; i < measureMethod.initCycles(); i++) {
            try {
                TimeUnit.MILLISECONDS.sleep(measureMethod.getSamplingIntervalForInit()); // first sleep, then measure.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            List<DataPoint> dataPoints = measureMethod.measure();
            dataPoints.forEach(dp -> measurements.get(dp.getName()).add(dp));
        }
        System.out.printf("energy measurement in idle mode took %s%n", HumanReadableTime.ofNanos(System.nanoTime() - timeBeforeTest));
        for (Map.Entry<String, List<DataPoint>> entry : measurements.entrySet()) {
            List<DataPoint> dataPoints = entry.getValue();
            List<DataPoint> dataPointsToConsider = dataPoints.subList(firstXPercent(dataPoints.size()), dataPoints.size());  // cut off the first x% measurements
            DataPoint average = calculateAvg(dataPointsToConsider);
            resultsWriter.writeToMeasurementCsv("Initialize", dataPointsToConsider, "(measure idle power)");
            BigDecimal prev = defaults.putIfAbsent(entry.getKey(), average.isPowerSensor() ? average.getValue() : BigDecimal.ZERO); // add zero, if not a power sensor!
            if (prev == null) { // then the key was not present in the map => log entry.
                System.out.printf("(measured) %s in idle mode for %s is %s%n", average.isPowerSensor() ? "energy consumption" : "sensor value", entry.getKey(), average.getValue());
            }
        }
        return defaults;
    }

    private DataPoint calculateAvg(@NotNull List<DataPoint> dataPoints) {
        if (dataPoints.size() == 0) {
            return new DataPoint("No Datapoints for Average", BigDecimal.ZERO, Unit.NONE, LocalDateTime.now(), null);
        }
        DataPoint reference = dataPoints.get(0);
        BigDecimal avg = dataPoints.stream()
            .map(DataPoint::getValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(new BigDecimal(dataPoints.size()), mathContext)
            .setScale(2, mathContext.getRoundingMode());
        return new DataPoint(reference.getName(), avg, reference.getUnit(), LocalDateTime.now(), reference.getThreadId());
    }

    private String getTestName(ExtensionContext context) {
        String nameOfGrandParent = context.getParent().map(this::getParent).orElse("");
        String nameOfParent = getParent(context);
        return (nameOfGrandParent.startsWith("JUnit Jupiter") ? "" : nameOfGrandParent) + nameOfParent + context.getDisplayName();
    }

    @NotNull
    private String getParent(ExtensionContext x) {
        return x.getParent().map(y -> y.getDisplayName() + "->").orElse("");
    }

    private int firstXPercent(int size) {
        BigDecimal xPercent = new BigDecimal(String.valueOf(size)).multiply(measureMethod.getPercentageOfSamplesAtBeginningToDiscard()).divide(new BigDecimal("100"), mathContext);
        return xPercent.setScale(0, mathContext.getRoundingMode()).intValue();
    }
}
