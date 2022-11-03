package group.msg.jpowermonitor.agent;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcurrentHashMapLongAdderTest {

    @Test
    void concurrentHashMapLongAdderTest() {
        ConcurrentMap<String, LongAdder> callsPerMethod = new ConcurrentHashMap<>();
        callsPerMethod.computeIfAbsent("test", callCount -> new LongAdder()).increment();
        assertThat(callsPerMethod.get("test").intValue()).isEqualTo(1);
        callsPerMethod.computeIfAbsent("test", callCount -> new LongAdder()).increment();
        assertThat(callsPerMethod.get("test").intValue()).isEqualTo(2);
        callsPerMethod.computeIfAbsent("test2", callCount -> new LongAdder()).increment();
        assertThat(callsPerMethod.get("test2").intValue()).isEqualTo(1);
    }
}
