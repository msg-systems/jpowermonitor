package group.msg.jpowermonitor;

import group.msg.jpowermonitor.config.JPowerMonitorConfig;
import group.msg.jpowermonitor.config.JPowerMonitorConfigProvider;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

public class ConfigProviderForTests implements JPowerMonitorConfigProvider {

    public JPowerMonitorConfig readConfig(Class<?> testClass) throws JPowerMonitorException {
        return readConfig(testClass.getSimpleName() + ".yaml");
    }

    @Override
    public JPowerMonitorConfig getCachedConfig() throws JPowerMonitorException {
        throw new JPowerMonitorException("Cache not implemented for " + ConfigProviderForTests.class);
    }

    @Override
    public JPowerMonitorConfig readConfig(String source) throws JPowerMonitorException {
        ClassLoader cl = ConfigProviderForTests.class.getClassLoader();
        try (InputStream input = cl.getResourceAsStream(source)) {
            return new Yaml().load(input);
        } catch (Exception exc) {
            throw new JPowerMonitorException(String.format("Cannot load config for tests from '%s'", source), exc);
        }
    }
}
