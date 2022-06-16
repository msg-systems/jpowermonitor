package com.msg.myapplication;

import group.msg.jpowermonitor.dto.SensorValue;
import group.msg.jpowermonitor.dto.SensorValues;
import group.msg.jpowermonitor.junit.JPowerMonitorExtension;

import group.msg.jpowermonitor.util.StressCpuExample;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

@EnabledOnOs(OS.WINDOWS)
@ExtendWith({JPowerMonitorExtension.class})
@Slf4j
public class EndlessLoopTest {
    @SensorValues
    private List<SensorValue> valueList;

    @AfterEach
    void printValues() {
        if (valueList != null) {
            valueList.forEach(x -> log.info("Value:{}", x));
        }
    }
    @RepeatedTest(1)
    void endlessLoopCPUStressTest() {
        long ranSecs = StressCpuExample.runMeasurement(StressCpuExample.DEFAULT_SECONDS_TO_RUN, 1, StressCpuExample::iAm100Percent);
        Assertions.assertTrue(StressCpuExample.DEFAULT_SECONDS_TO_RUN <= ranSecs);
    }

    @RepeatedTest(1)
    void parallelEndlessLoopCpuStressTest() {
        StressCpuExample.runParallelEndlessLoopCpuStressTest(Runtime.getRuntime().availableProcessors(), StressCpuExample.DEFAULT_SECONDS_TO_RUN);
    }

}
