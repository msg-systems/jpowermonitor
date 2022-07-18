package com.msg.myapplication;

import group.msg.jpowermonitor.junit.JPowerMonitorExtension;
import group.msg.jpowermonitor.dto.SensorValue;
import group.msg.jpowermonitor.dto.SensorValues;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

@ExtendWith({JPowerMonitorExtension.class})
@Slf4j
public class MyTest {
    @SensorValues
    List<SensorValue> sensorValueList;

    @RepeatedTest(1)
    void myPowerConsumingSuperTest() {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < 1_000_000; i++) {
            BigDecimal a = new BigDecimal("7.488").add(new BigDecimal(i));
            BigDecimal sqrt = a.sqrt(new MathContext(100, RoundingMode.HALF_UP));
            sum = sum.add(sqrt).setScale(2, RoundingMode.HALF_UP);
        }
        log.info("Sum is {}", sum);
        Assertions.assertEquals(new BigDecimal("666673641.02"), sum);
    }

    @AfterEach
    void myMethodAfterEachTest() {
        // @SensorValues annotated fields of type List<SensorValue> are accessible after each test
        log.info("sensorvalues: {}", sensorValueList);
    }

    @RepeatedTest(1)
    void myPowerConsumingSuperTestDifferentAlgo() {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < 1_000_000; i++) {
            BigDecimal a = new BigDecimal("7.488").add(new BigDecimal(i));
            BigDecimal sqrt = a.sqrt(new MathContext(100, RoundingMode.HALF_UP)).divide(new BigDecimal("3.14"), new MathContext(200, RoundingMode.HALF_UP));
            sum = sum.add(sqrt).setScale(2, RoundingMode.HALF_UP);
        }
        log.info("Sum is {}", sum);
        Assertions.assertEquals(new BigDecimal("212316445.91"), sum);
    }

    @Test
    void myPowerConsumingSuperTestLong() {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < 1_000_000; i++) {
            BigDecimal a = new BigDecimal("7.488").add(new BigDecimal(i));
            BigDecimal sqrt = a.sqrt(new MathContext(100, RoundingMode.HALF_UP));
            sum = sum.add(sqrt).setScale(2, RoundingMode.HALF_UP);
        }
        log.info("Sum is {}", sum);
        Assertions.assertEquals(new BigDecimal("666673641.02"), sum);
    }

}
