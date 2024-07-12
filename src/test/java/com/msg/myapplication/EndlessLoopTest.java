package com.msg.myapplication;

import group.msg.jpowermonitor.dto.SensorValue;
import group.msg.jpowermonitor.dto.SensorValues;
import group.msg.jpowermonitor.junit.JPowerMonitorExtension;
import group.msg.jpowermonitor.util.StressCpuExample;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static group.msg.jpowermonitor.agent.Unit.WATT;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({JPowerMonitorExtension.class})
public class EndlessLoopTest {
    @SensorValues
    private List<SensorValue> valueList;

    @AfterEach
    void printValues() {
        assertThat(valueList).isNotNull();
        //
        // as we use a fix csv file, the outcome is fix for this test:
        valueList.forEach(x -> {
            assertThat(x.getValue()).isCloseTo(5.05, Offset.offset(0.01));
            assertThat(x.getPowerInIdleMode()).isEqualTo(2.01);
            assertThat(x.getName()).isEqualTo("CPU Power");
            assertThat(x.getUnit()).isEqualTo(WATT);
        });
    }

    @Test
    void endlessLoopCPUStressTest() {
        long ranSecs = StressCpuExample.runMeasurement(StressCpuExample.DEFAULT_SECONDS_TO_RUN, 1, StressCpuExample::iAm100Percent);
        assertThat(StressCpuExample.DEFAULT_SECONDS_TO_RUN <= ranSecs).isTrue();
    }

    @Test
    void parallelEndlessLoopCpuStressTest() {
        StressCpuExample.runParallelEndlessLoopCpuStressTest(Runtime.getRuntime().availableProcessors(), StressCpuExample.DEFAULT_SECONDS_TO_RUN);
    }

}
