package group.msg.jpowermonitor.config;

import group.msg.jpowermonitor.JPowerMonitorException;
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

import static group.msg.jpowermonitor.agent.JPowerMonitorAgent.APP_TITLE;

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
    private static final String DEFAULT_CONFIG = APP_TITLE + ".yaml";
    private final Charset yamlFileEncoding;
    private final ClassLoader resourceLoader;
    private static JPowerMonitorConfig cachedConfig = null;

    public DefaultConfigProvider() {
        this.yamlFileEncoding = StandardCharsets.UTF_8;
        this.resourceLoader = DefaultConfigProvider.class.getClassLoader();
    }

    @Override
    public synchronized JPowerMonitorConfig readConfig(String source) throws JPowerMonitorException {
        if (cachedConfig == null) {
            cachedConfig = acquireConfigFromSource(isValidSource(source) ? source : DEFAULT_CONFIG);
        }
        return cachedConfig;
    }

    @NotNull
    private JPowerMonitorConfig acquireConfigFromSource(String source) {
        JPowerMonitorConfig cfg = Stream.of(
                (Supplier<JPowerMonitorConfig>) () -> this.tryReadingFromFileSystem(source),
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
        System.out.println("Reading " + APP_TITLE + " configuration from given source '" + fileName + "' on path " + path);
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

    private JPowerMonitorConfig tryReadingFromFileSystem(String source) {
        System.out.println("Reading " + APP_TITLE + " configuration from filesystem: '" + source + "'");
        Path path = Paths.get(source);
        if (!Files.isRegularFile(path)) {
            System.out.println("'" + source + "' is no regular file, we won't read it from filesystem");
            return null;
        }
        return readConfigFromPath(path);
    }

    private JPowerMonitorConfig readConfigFromPath(Path path) {
        try (Reader reader = Files.newBufferedReader(path, yamlFileEncoding)) {
            return new Yaml().load(reader);
        } catch (Exception e) {
            System.err.println("Cannot read '" + path + "' from filesystem: " + e.getMessage());
        }
        return null;
    }

    private JPowerMonitorConfig tryReadingFromResources(String source) {
        System.out.println("Reading " + APP_TITLE + " configuration from resources: '" + source + "'");
        if (resourceLoader.getResource(source) == null) {
            System.out.println("'" + source + "' is not available as resource");
            return null;
        }
        return readConfigFromResource(source);
    }

    private JPowerMonitorConfig readConfigFromResource(String source) {
        try (InputStream input = resourceLoader.getResourceAsStream(source)) {
            return new Yaml().load(input);
        } catch (Exception exc) {
            System.out.println("Cannot read '" + source + "' from resources:" + exc.getMessage());
        }
        return null;
    }

    /**
     * For testing need to reset the internally cached config.
     */
    public static void resetCachedConfig() {
        cachedConfig = null;
    }

}
