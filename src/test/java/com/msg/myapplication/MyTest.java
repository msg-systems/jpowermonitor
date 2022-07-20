package com.msg.myapplication;

import group.msg.jpowermonitor.config.DefaultConfigProvider;
import group.msg.jpowermonitor.dto.SensorValue;
import group.msg.jpowermonitor.dto.SensorValues;
import group.msg.jpowermonitor.junit.JPowerMonitorExtension;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

import static group.msg.jpowermonitor.agent.Unit.WATT;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({JPowerMonitorExtension.class})
@Slf4j
public class MyTest {
    @SensorValues
    List<SensorValue> sensorValueList;

    @BeforeAll
    static void resetConfig() {
        DefaultConfigProvider.invalidateCachedConfig();
    }

    @RepeatedTest(1)
    void myPowerConsumingSuperTest() {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < 1_000_000; i++) {
            BigDecimal a = new BigDecimal("7.488").add(new BigDecimal(i));
            BigDecimal sqrt = a.sqrt(new MathContext(100, RoundingMode.HALF_UP));
            sum = sum.add(sqrt).setScale(2, RoundingMode.HALF_UP);
        }
        log.info("Sum is {}", sum);
        assertThat(sum).isEqualTo(new BigDecimal("666673641.02"));
    }

    @AfterEach
    void myMethodAfterEachTest() {
        // @SensorValues annotated fields of type List<SensorValue> are accessible after each test
        log.info("sensorvalues: {}", sensorValueList);
        assertThat(sensorValueList).isNotNull();
        sensorValueList.forEach(x -> {
            assertThat(x.getValue()).isEqualTo(new BigDecimal("5.05"));
            assertThat(x.getPowerInIdleMode()).isEqualTo(new BigDecimal("2.01"));
            assertThat(x.getName()).isEqualTo("CPU Power");
            assertThat(x.getUnit()).isEqualTo(WATT);
        });
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
        assertThat(sum).isEqualTo(new BigDecimal("212316445.91"));
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
        assertThat(sum).isEqualTo(new BigDecimal("666673641.02"));
    }

}
