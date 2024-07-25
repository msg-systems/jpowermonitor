package group.msg.jpowermonitor.agent;

import com.sun.tools.attach.VirtualMachine;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

class JPowerMonitorAgentTest {
    @Test
    void premain() throws InterruptedException {
        //try {
        //    String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        //   // String pid = nameOfRunningVM.substring(0, nameOfRunningVM.indexOf('@'));
        //    long pid = ProcessHandle.current().pid();
        //    VirtualMachine vm = VirtualMachine.attach(String.valueOf(pid));
        //    vm.loadAgent("/build/libs/jpowermonitor-1.3.0-SNAPSHOT-all.jar", "JPowerMonitorAgentTest.yaml");
        //    vm.detach();
        //} catch (Exception e) {
        //    e.printStackTrace();
        //}
        JPowerMonitorAgent.premain("JPowerMonitorAgentTest.yaml", null);
        Thread t = new Thread(()-> {
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
