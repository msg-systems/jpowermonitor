package group.msg.jpowermonitor.config;

import group.msg.jpowermonitor.JPowerMonitorException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

@Data
@Slf4j
public class JPowerMonitorConfig {

    protected static String DEFAULT_CONFIG = "jpowermonitor.yaml";
    private static final Charset DEFAULT_CONFIG_ENCODING = StandardCharsets.UTF_8;

    private static JPowerMonitorConfig cachedConfig;

    private Integer samplingIntervalInMs;
    private Integer samplingIntervalForInitInMs;
    private Integer initCycles;
    private Integer calmDownIntervalInMs;
    private BigDecimal percentageOfSamplesAtBeginningToDiscard;
    private OpenHardwareMonitor openHardwareMonitor;
    private CsvRecording csvRecording;
    private JavaAgent javaAgent;

    public static JPowerMonitorConfig readConfig(String extConfigFileName) {
        return readConfig(extConfigFileName, true);
    }

    public static synchronized JPowerMonitorConfig readConfig(String extConfigFileName, boolean useCache) {
        if (!useCache || cachedConfig == null) {
            final String configName = aquireConfigurationName(extConfigFileName);
            final Optional<Path> externalConfig = aquireExternalConfig(configName);
            final JPowerMonitorConfig config = externalConfig.isPresent()
                ? readConfigFromFile(externalConfig.get())
                : readConfigFromClasspath(configName);
            validateConfiguration(config);

            // set defaults
            setDefaultIfNotSet(config.getSamplingIntervalInMs(), config::setSamplingIntervalInMs, 300);
            setDefaultIfNotSet(config.getSamplingIntervalForInitInMs(), config::setSamplingIntervalForInitInMs, 1000);
            setDefaultIfNotSet(config.getInitCycles(), config::setInitCycles, 10);
            setDefaultIfNotSet(config.getCalmDownIntervalInMs(), config::setCalmDownIntervalInMs, 1000);
            setDefaultIfNotSet(config.getPercentageOfSamplesAtBeginningToDiscard(), config::setPercentageOfSamplesAtBeginningToDiscard, new BigDecimal("15"));
            setDefaultIfNotSet(config.getJavaAgent(), config::setJavaAgent, new JavaAgent());
            JavaAgent javaAgent = config.getJavaAgent();
            setDefaultIfNotSet(javaAgent.getPackageFilter(), javaAgent::setPackageFilter, Collections.emptySet());
            setDefaultIfNotSet(javaAgent.getMeasurementIntervalInMs(), javaAgent::setMeasurementIntervalInMs, 1000L);
            setDefaultIfNotSet(javaAgent.getGatherStatisticsIntervalInMs(), javaAgent::setGatherStatisticsIntervalInMs, 10L);
            setDefaultIfNotSet(javaAgent.getGatherStatisticsIntervalInMs(), javaAgent::setGatherStatisticsIntervalInMs, 0L);
            log.debug("Read config (including defaults): {}", config);
            cachedConfig = config;
        }
        return cachedConfig;
    }

    private static String aquireConfigurationName(String arguments) {
        if (arguments == null || arguments.isBlank() || JPowerMonitorConfig.class.getClassLoader().getResource(arguments) == null) {
            log.info(
                "No configuration name given by program arguments, using default name '{}'",
                DEFAULT_CONFIG);
            return DEFAULT_CONFIG;
        }

        return arguments;
    }

    private static Optional<Path> aquireExternalConfig(String configName) {
        Path potentialConfig = Paths.get(configName);
        if (!Files.isRegularFile(potentialConfig)) {
            log.info(
                "No external configuration found at '{}', using configuration from classpath",
                potentialConfig.toAbsolutePath().normalize());
            return Optional.empty();
        }

        return Optional.of(potentialConfig);
    }

    @Nullable
    private static JPowerMonitorConfig readConfigFromFile(Path file) {
        try (Reader reader = Files.newBufferedReader(file, DEFAULT_CONFIG_ENCODING)) {
            log.info(
                "Trying to read configuration from file '{}' using encoding {}",
                file.toAbsolutePath().normalize(),
                DEFAULT_CONFIG_ENCODING.displayName());
            return new Yaml().load(reader);
        } catch (Exception exc) {
            log.warn(
                "Cannot read configuration from file '{}', using default classpath configuration",
                file.toAbsolutePath().normalize(), exc);
        }

        return readConfigFromClasspath(DEFAULT_CONFIG);
    }

    @Nullable
    private static JPowerMonitorConfig readConfigFromClasspath(String configName) {
        ClassLoader cl = JPowerMonitorConfig.class.getClassLoader();
        try (InputStream input = cl.getResourceAsStream(configName)) {
            return new Yaml().load(input);
        }
        catch (Exception exc) {
            log.error("Cannot read configuration from classpath '{}'", configName, exc);
        }

        return null;
    }

    private static void validateConfiguration(JPowerMonitorConfig config) {
        if (config == null) {
            throw new JPowerMonitorException("No configuration available");
        }

        if (   config.getOpenHardwareMonitor() == null
            || config.getOpenHardwareMonitor().getUrl() == null) {
            throw new JPowerMonitorException("OpenHardwareMonitor REST endpoint URL must be configured");
        }

        config.getOpenHardwareMonitor().setUrl(
            config.getOpenHardwareMonitor().getUrl() + "/data.json");

        List<PathElement> pathElems = config.getOpenHardwareMonitor().getPaths();
        if (   pathElems == null
            || pathElems.isEmpty()
            || pathElems.get(0) == null
            || pathElems.get(0).getPath() == null
            || pathElems.get(0).getPath().isEmpty()) {
            throw new JPowerMonitorException(
                "At least one path to a sensor value must be configured under paths");
        }
    }

    private static <T> void setDefaultIfNotSet(T currentValue, Consumer<T> consumer, T defaultValue) {
        if (currentValue == null) {
            consumer.accept(defaultValue);
        }
    }

}
