package group.msg.jpowermonitor.config;

import group.msg.jpowermonitor.JPowerMonitorException;
import group.msg.jpowermonitor.agent.JPowerMonitorAgent;
import group.msg.jpowermonitor.config.dto.JPowerMonitorCfg;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static group.msg.jpowermonitor.util.Constants.APP_TITLE;

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
@Slf4j
public class DefaultCfgProvider implements JPowerMonitorCfgProvider {
    private static final String DEFAULT_CONFIG = APP_TITLE + ".yaml";
    private final Charset yamlFileEncoding;
    private static JPowerMonitorCfg cachedConfig = null;

    public DefaultCfgProvider() {
        this.yamlFileEncoding = StandardCharsets.UTF_8;
    }

    @Override
    public synchronized JPowerMonitorCfg getCachedConfig() throws JPowerMonitorException {
        if (cachedConfig == null) {
            cachedConfig = acquireConfigFromSource(DEFAULT_CONFIG);
        }
        return cachedConfig;
    }

    @Override
    public synchronized JPowerMonitorCfg readConfig(String source) throws JPowerMonitorException {
        if (cachedConfig == null) {
            cachedConfig = acquireConfigFromSource(isValidSource(source) ? source : DEFAULT_CONFIG);
        }
        return cachedConfig;
    }

    @NotNull
    private JPowerMonitorCfg acquireConfigFromSource(String source) {
        JPowerMonitorCfg cfg = Stream.of(
                (Supplier<JPowerMonitorCfg>) () -> this.tryReadingFromFileSystem(source),
                () -> this.tryReadingFromResources(source),
                () -> {
                    Path conf = findFileIgnoringCase(Path.of("."), DEFAULT_CONFIG);
                    return this.readConfigFromPath(conf);
                })
            .map(Supplier::get)
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new JPowerMonitorException(String.format("Unable to read %s configuration from source '%s'", APP_TITLE, source)));
        cfg.initializeConfiguration();
        return cfg;
    }

    public Path findFileIgnoringCase(Path path, String fileName) {
        log.info("Reading {} configuration from given source '{}' on path {}", APP_TITLE, fileName, path);
        if (!JPowerMonitorAgent.isSlf4jLoggerImplPresent()) {
            System.out.println("Reading " + APP_TITLE + " configuration from given source '" + fileName + "' on path " + path);
        }
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a directory!");
        }
        try (Stream<Path> walk = Files.walk(path)) {
            return walk
                .filter(Files::isReadable)
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().equalsIgnoreCase(fileName)).findFirst().orElse(null);
        } catch (IOException e) {
            return null;
        }
    }

    private JPowerMonitorCfg tryReadingFromFileSystem(String source) {
        log.info("Reading " + APP_TITLE + " configuration from filesystem: '" + source + "'");
        Path path = Paths.get(source);
        if (!Files.isRegularFile(path)) {
            log.error("'" + source + "' is not a regular file, it will not be read from filesystem");
            return null;
        }
        return readConfigFromPath(path);
    }

    private JPowerMonitorCfg readConfigFromPath(Path path) {
        try (Reader reader = Files.newBufferedReader(path, yamlFileEncoding)) {
            return new Yaml().loadAs(reader, JPowerMonitorCfg.class);
        } catch (Exception e) {
            log.error("Cannot read '{}' from filesystem: {}", path, e.getMessage());
            if (!JPowerMonitorAgent.isSlf4jLoggerImplPresent()) {
                System.err.println("Cannot read '" + path + "' from filesystem: " + e.getMessage());
            }
        }
        return null;
    }

    private JPowerMonitorCfg tryReadingFromResources(String source) {
        log.info("Reading " + APP_TITLE + " configuration from resources: '" + source + "'");
        if (DefaultCfgProvider.class.getClassLoader().getResource(source) == null) {
            log.info("'" + source + "' is not available as resource");
            return null;
        }
        return readConfigFromResource(source);
    }

    private JPowerMonitorCfg readConfigFromResource(String source) {
        try (InputStream input = DefaultCfgProvider.class.getClassLoader().getResourceAsStream(source)) {
            return new Yaml().loadAs(input, JPowerMonitorCfg.class);
        } catch (Exception e) {
            log.error("Cannot read '{}' from resources: {}", source, e.getMessage());
            if (!JPowerMonitorAgent.isSlf4jLoggerImplPresent()) {
                System.err.println("Cannot read '" + source + "' from resources: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * For testing need to invalidate the static internally cached config in order to re-read it.
     */
    public static void invalidateCachedConfig() {
        cachedConfig = null;
    }

}
