package group.msg.jpowermonitor;

import group.msg.jpowermonitor.config.dto.JPowerMonitorCfg;
import group.msg.jpowermonitor.config.JPowerMonitorConfigProvider;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

public class ConfigProviderForTests implements JPowerMonitorConfigProvider {

    public JPowerMonitorCfg readConfig(Class<?> testClass) throws JPowerMonitorException {
        return readConfig(testClass.getSimpleName() + ".yaml");
    }

    @Override
    public JPowerMonitorCfg getCachedConfig() throws JPowerMonitorException {
        throw new JPowerMonitorException("Cache not implemented for " + ConfigProviderForTests.class);
    }

    @Override
    public JPowerMonitorCfg readConfig(String source) throws JPowerMonitorException {
        ClassLoader cl = ConfigProviderForTests.class.getClassLoader();
        try (InputStream input = cl.getResourceAsStream(source)) {
            return new Yaml().loadAs(input, JPowerMonitorCfg.class);
        } catch (Exception exc) {
            throw new JPowerMonitorException(String.format("Cannot load config for tests from '%s'", source), exc);
        }
    }
}
