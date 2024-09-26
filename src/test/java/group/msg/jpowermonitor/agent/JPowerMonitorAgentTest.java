package group.msg.jpowermonitor.agent;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

class JPowerMonitorAgentTest {
    @Test
    void premain() throws InterruptedException {
        JPowerMonitorAgent.premain("JPowerMonitorAgentTest.yaml", null);
        Thread t = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t.start();
        t.join();
    }
}
