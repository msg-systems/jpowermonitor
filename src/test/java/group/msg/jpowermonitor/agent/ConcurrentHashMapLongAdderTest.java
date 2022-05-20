package group.msg.jpowermonitor.agent;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConcurrentHashMapLongAdderTest {

    @Test
     void concurrentHashMapLongAdderTest() {
        ConcurrentMap<String, LongAdder> callsPerMethod = new ConcurrentHashMap<>();
        callsPerMethod.computeIfAbsent("test", callCount -> new LongAdder()).increment();
        assertEquals(1, callsPerMethod.get("test").intValue());
        callsPerMethod.computeIfAbsent("test", callCount -> new LongAdder()).increment();
        assertEquals(2, callsPerMethod.get("test").intValue());
        callsPerMethod.computeIfAbsent("test2", callCount -> new LongAdder()).increment();
        assertEquals(1, callsPerMethod.get("test2").intValue());
    }
}
