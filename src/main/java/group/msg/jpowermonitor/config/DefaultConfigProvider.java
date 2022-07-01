package group.msg.jpowermonitor.config;

import group.msg.jpowermonitor.JPowerMonitorException;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Default configuration provider preferring file system to resources.
 * <p>
 * This configuration provider uses caching (e.g. reads only once per provider instance) and reads
 * the configuration as a YAML file (see <code>resources/jpowermonitor-template.yaml</code> for
 * example). In order to find a configuration, it uses the following sequence:
 * <ul>
 * <li>If a source is given, try reading from the file system.</li>
 * <li>If file system fails (for any reason), try reading with source from the resources.</li>
 * <li>If no source is given (or couldn't be read), fall back to using
 * <code>jpowermonitor.yaml</code> (see {@link #DEFAULT_CONFIG}).</li>
 * <li>Try finding that default source in the file system.</li>
 * <li>If that fails, try finding it in the resources.</li>
 * <li>If nothing was found, throw an exception.</li>
 * </ul>
 */
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
            if (isValidSource(source)) {
                System.out.println("Reading JPowerMonitor configuration from given source '" + source + "'");
                cachedConfig = acquireConfigFromSource(source);
            }

            if (cachedConfig == null) {
                System.out.println("Reading JPowerMonitor configuration from default '" + DEFAULT_CONFIG + "'");
                cachedConfig = acquireConfigFromSource(DEFAULT_CONFIG);
            }
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
            cfg = tryReadingFromResources(source);
            cfg.ifPresent(JPowerMonitorConfig::initializeConfiguration);
        }

        if (cfg.isEmpty() && !DEFAULT_CONFIG.equals(source)) {
            cfg = tryReadingFromFileSystem(DEFAULT_CONFIG);
            cfg.ifPresent(JPowerMonitorConfig::initializeConfiguration);
        }

        return cfg.orElseThrow(() -> new JPowerMonitorException(
            String.format("Cannot read JPowerMonitor configuration from source '%s'", source)));
    }

    private Optional<JPowerMonitorConfig> tryReadingFromFileSystem(String source) {
        Path potentialConfig = Paths.get(source);
        if (!Files.isRegularFile(potentialConfig)) {
            System.out.println("'" + source + "' is no regular file, we won't read it from filesystem");
            return Optional.empty();
        }

        potentialConfig = potentialConfig.toAbsolutePath().normalize();
        try (Reader reader = Files.newBufferedReader(potentialConfig, yamlFileEncoding)) {
            System.out.println(
                "Trying to read '" + potentialConfig + "' from filesystem using encoding " + yamlFileEncoding.displayName());
            JPowerMonitorConfig config = new Yaml().load(reader);
            return Optional.of(config);
        } catch (Exception exc) {
            System.err.println("Cannot read '" + potentialConfig + "' from filesystem" + exc.getMessage());
        }

        return Optional.empty();
    }

    private Optional<JPowerMonitorConfig> tryReadingFromResources(String source) {
        if (resourceLoader.getResource(source) == null) {
            System.out.println("'" + source + "' is not available as resource");
            return Optional.empty();
        }

        try (InputStream input = resourceLoader.getResourceAsStream(source)) {
            JPowerMonitorConfig config = new Yaml().load(input);
            return Optional.of(config);
        } catch (Exception exc) {
            System.out.println("Cannot read '" + source + "' from resources:" + exc.getMessage());
        }

        return Optional.empty();
    }


}
