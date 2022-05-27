package group.msg.jpowermonitor.config;

import group.msg.jpowermonitor.JPowerMonitorException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

/**
 * Default configuration provider preferring resources over file system.
 * <p>
 * This configuration provider uses caching (e.g. reads only once per provider instance) and first
 * tries to read the configuration file from Java resources. If that fails, it tries to read from
 * the file system. If that also fails, it falls back reading the default configuration {@link
 * #DEFAULT_CONFIG} from resources.
 */
@Slf4j
public class DefaultConfigProvider implements JPowerMonitorConfigProvider {

    private static final String DEFAULT_CONFIG = "jpowermonitor.yaml";

    private final Charset yamlFileEncoding;
    private final ClassLoader resourceLoader;
    private JPowerMonitorConfig cachedConfig = null;


    public DefaultConfigProvider() {
        this.yamlFileEncoding = StandardCharsets.UTF_8;
        this.resourceLoader = DefaultConfigProvider.class.getClassLoader();
    }

    @Override
    public synchronized JPowerMonitorConfig readConfig(String source)
        throws JPowerMonitorException {
        if (cachedConfig == null) {
            Optional<JPowerMonitorConfig> config = tryReadingFromResources(source);
            if (config.isEmpty()) {
                log.info("Could not find '{}' in resources, trying file system", source);
                config = tryReadingFromFileSystem(source);
            }
            if (config.isEmpty()) {
                log.info(
                    "Could not find '{}' in filesystem, falling back to default configuration",
                    source);
                config = tryReadingFromFileSystem(getDefaultConfig());
            }
            if (config.isEmpty()) {
                log.info(
                    "Could not find '{}' in filesystem, falling back to default configuration in resources",
                    getDefaultConfig());
                config = tryReadingFromResources(getDefaultConfig());
            }

            config.ifPresent(JPowerMonitorConfig::initializeConfiguration);
            cachedConfig = config.orElse(null);
        }

        if (cachedConfig == null) {
            throw new JPowerMonitorException("No configuration available");
        }
        return cachedConfig;
    }

    private Optional<JPowerMonitorConfig> tryReadingFromResources(String source) {
        if (source == null || resourceLoader.getResource(source) == null) {
            return Optional.empty();
        }

        try (InputStream input = resourceLoader.getResourceAsStream(source)) {
            JPowerMonitorConfig config = new Yaml().load(input);
            return Optional.of(config);
        } catch (Exception exc) {
            log.warn("Cannot read '{}' from resources", source, exc);
        }

        return Optional.empty();
    }

    private Optional<JPowerMonitorConfig> tryReadingFromFileSystem(String source) {
        if (source == null) {
            return Optional.empty();
        }

        Path potentialConfig = Paths.get(source);
        if (!Files.isRegularFile(potentialConfig)) {
            log.debug("'{}' is no regular file, we won't read it from filesystem", source);
            return Optional.empty();
        }

        potentialConfig = potentialConfig.toAbsolutePath().normalize();
        try (Reader reader = Files.newBufferedReader(potentialConfig, yamlFileEncoding)) {
            log.info(
                "Trying to read '{}' from filesystem using encoding {}",
                potentialConfig,
                yamlFileEncoding.displayName());
            JPowerMonitorConfig config = new Yaml().load(reader);
            return Optional.of(config);
        } catch (Exception exc) {
            log.warn("Cannot read '{}' from filesystem", potentialConfig, exc);
        }

        return Optional.empty();
    }

    protected String getDefaultConfig() {
        return DEFAULT_CONFIG;
    }

}
