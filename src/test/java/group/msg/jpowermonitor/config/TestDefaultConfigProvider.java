package group.msg.jpowermonitor.config;

public class TestDefaultConfigProvider extends DefaultConfigProvider {
    @Override
    protected String getDefaultConfig() {
        return "jpowermonitor_test.yaml";
    }
}
