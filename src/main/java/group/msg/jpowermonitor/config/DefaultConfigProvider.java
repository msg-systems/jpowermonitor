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
 * Default configuration provider preferring file system to resources.
 * <p>
 * This configuration provider uses caching (e.g. reads only once per provider instance) and has two
 * basic modi: If a source is given, it tries to read this source from the filesystem first. If not
 * possible, it tries to read it from the resources. The second modus has no valid source (see
 * {@link #isValidSource(String)}) and simply uses {@link #DEFAULT_CONFIG} with the same approach;
 * So file system first then resource.
 * <p>
 * Generally speaking, it always tries the file system first.
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
            final String actualSource;
            if (isValidSource(source)) {
                log.info("Reading JPowerMonitor configuration from given source '{}'", source);
                actualSource = source;
            } else {
                log.info("Reading JPowerMonitor configuration from default '{}'", DEFAULT_CONFIG);
                actualSource = DEFAULT_CONFIG;
            }
            cachedConfig = acquireConfigFromSource(actualSource);
        }

        if (cachedConfig == null) {
            throw new JPowerMonitorException("No configuration available");
        }
        return cachedConfig;
    }

    private JPowerMonitorConfig acquireConfigFromSource(String source) {
        Optional<JPowerMonitorConfig> cfg = tryReadingFromFileSystem(source);
        cfg.ifPresent(JPowerMonitorConfig::initializeConfiguration);

        if (cfg.isEmpty()) {
            log.debug(
                "Could not read JPowerMonitor configuration from filesystem, trying resources now");
            cfg = tryReadingFromResources(source);
            cfg.ifPresent(JPowerMonitorConfig::initializeConfiguration);
        }

        return cfg.orElseThrow(() -> new JPowerMonitorException(
            String.format("Cannot read JPowerMonitor configuration from source '%s'", source)));
    }

    private Optional<JPowerMonitorConfig> tryReadingFromFileSystem(String source) {
        Path potentialConfig = Paths.get(source);
        if (!Files.isRegularFile(potentialConfig)) {
            log.warn("'{}' is no regular file, we won't read it from filesystem", source);
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

    private Optional<JPowerMonitorConfig> tryReadingFromResources(String source) {
        if (resourceLoader.getResource(source) == null) {
            log.warn("'{}' is not available as resource", source);
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


}
