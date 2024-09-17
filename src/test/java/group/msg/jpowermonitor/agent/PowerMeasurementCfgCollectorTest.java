package group.msg.jpowermonitor.agent;

import group.msg.jpowermonitor.config.dto.JavaAgentCfg;
import group.msg.jpowermonitor.config.dto.MonitoringCfg;
import group.msg.jpowermonitor.dto.Activity;
import group.msg.jpowermonitor.dto.DataPoint;
import group.msg.jpowermonitor.dto.MethodActivity;
import group.msg.jpowermonitor.dto.Quantity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PowerMeasurementCfgCollectorTest {

    private static final DataPoint DP1 = new DataPoint("x", 0.0, Unit.WATT, LocalDateTime.now(), null);
    private static final DataPoint DP2 = new DataPoint("y", 1.0, Unit.WATT, LocalDateTime.now(), null);

    @Test
    void areAddableTest() {
        JavaAgentCfg javaAgentCfg = new JavaAgentCfg(new HashSet<>(), 0, 0, 0, new MonitoringCfg());
        assertTrue(new PowerMeasurementCollector(0, null, javaAgentCfg).areDataPointsAddable(DP1, DP2));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void areNotAddableFailBecauseOfValueNullTest() {
        JavaAgentCfg javaAgentCfg = new JavaAgentCfg(new HashSet<>(), 0, 0, 0, new MonitoringCfg());
        PowerMeasurementCollector testee = new PowerMeasurementCollector(0L, null, javaAgentCfg);
        assertThatThrownBy(() -> testee.areDataPointsAddable(DP1, null)).isInstanceOf(Exception.class);
        assertThatThrownBy(() -> testee.areDataPointsAddable(null, DP2)).isInstanceOf(Exception.class);
    }

    @Test
    void areNotAddableBecauseOfValueNullTest() {
        JavaAgentCfg javaAgentCfg = new JavaAgentCfg(new HashSet<>(), 0, 0, 0, new MonitoringCfg());
        PowerMeasurementCollector testee = new PowerMeasurementCollector(0L, null, javaAgentCfg);

        DataPoint dp2 = new DataPoint("y", null, Unit.WATT, LocalDateTime.now(), null);
        assertThat(testee.areDataPointsAddable(DP1, dp2)).isFalse();
        assertThat(testee.areDataPointsAddable(dp2, DP1)).isFalse();
    }

    @Test
    void areNotAddableBecauseOfUnitNullTest() {
        JavaAgentCfg javaAgentCfg = new JavaAgentCfg(new HashSet<>(), 0, 0, 0, new MonitoringCfg());
        PowerMeasurementCollector testee = new PowerMeasurementCollector(0L, null, javaAgentCfg);
        DataPoint dp2 = new DataPoint("y", 0.0, null, LocalDateTime.now(), null);
        assertThat(testee.areDataPointsAddable(DP1, dp2)).isFalse();
        assertThat(testee.areDataPointsAddable(dp2, DP1)).isFalse();
    }

    @Test
    void areNotAddableBecauseOfDifferentUnitsTest() {
        JavaAgentCfg javaAgentCfg = new JavaAgentCfg(new HashSet<>(), 0, 0, 0, new MonitoringCfg());
        PowerMeasurementCollector testee = new PowerMeasurementCollector(0L, null, javaAgentCfg);
        DataPoint dp2 = testee.cloneAndCalculateDataPoint(DP2, Unit.WATTHOURS, x -> x);
        assertThat(testee.areDataPointsAddable(DP1, dp2)).isFalse();
    }

    @Test
    void addTwoDataPointsTest() {
        JavaAgentCfg javaAgentCfg = new JavaAgentCfg(new HashSet<>(), 0, 0, 0, new MonitoringCfg());
        PowerMeasurementCollector testee = new PowerMeasurementCollector(0L, null, javaAgentCfg);
        DataPoint dpSum = testee.addDataPoint(DP1, DP2);
        assertThat(dpSum.getValue()).isEqualTo(DP1.getValue() + DP2.getValue());
    }

    @Test
    void addMultipleDataPointsTest() {
        JavaAgentCfg javaAgentCfg = new JavaAgentCfg(new HashSet<>(), 0, 0, 0, new MonitoringCfg());
        PowerMeasurementCollector testee = new PowerMeasurementCollector(0L, null, javaAgentCfg);
        DataPoint dp3 = new DataPoint("x", 10.0, Unit.WATT, LocalDateTime.now(), null);
        DataPoint dp4 = new DataPoint("x", 100.0, Unit.WATT, LocalDateTime.now(), null);
        DataPoint dpSum = testee.addDataPoint(DP1, DP2, dp3, dp4);
        assertThat(dpSum.getValue()).isEqualTo(111.0);
    }

    @Test
    void addMultipleDataPointsWithDifferentUnitsTest() {
        JavaAgentCfg javaAgentCfg = new JavaAgentCfg(new HashSet<>(), 0, 0, 0, new MonitoringCfg());
        PowerMeasurementCollector testee = new PowerMeasurementCollector(0L, null, javaAgentCfg);
        DataPoint dp3 = new DataPoint("x", 10.0, Unit.WATT, LocalDateTime.now(), null);
        DataPoint dp4 = new DataPoint("x", 100.0, Unit.WATTHOURS, LocalDateTime.now(), null);
        DataPoint dpSum = testee.addDataPoint(DP1, DP2, dp3, dp4);
        assertThat(dpSum.getValue()).isEqualTo(11.0);
    }

    @Test
    void cloneWithNewUnitTest() {
        JavaAgentCfg javaAgentCfg = new JavaAgentCfg(new HashSet<>(), 0, 0, 0, new MonitoringCfg());
        PowerMeasurementCollector testee = new PowerMeasurementCollector(0L, null, javaAgentCfg);
        DataPoint dp3 = testee.cloneAndCalculateDataPoint(DP1, Unit.WATTHOURS, x -> x);
        assertNotEquals(dp3, DP1);
        assertEquals(Unit.WATTHOURS, dp3.getUnit());
    }

    @Test
    void aggregateActivityTest() {
        JavaAgentCfg javaAgentCfg = new JavaAgentCfg(new HashSet<>(), 0, 0, 0, new MonitoringCfg());
        PowerMeasurementCollector testee = new PowerMeasurementCollector(0L, null, javaAgentCfg);

        MethodActivity ma1 = new MethodActivity();
        ma1.setMethodQualifier("no.filter.Method");
        ma1.setRepresentedQuantity(Quantity.of(0.0, Unit.JOULE));

        MethodActivity ma2 = new MethodActivity();
        ma2.setMethodQualifier("no.filter.method.Either");
        ma2.setRepresentedQuantity(Quantity.of(1.0, Unit.JOULE));

        MethodActivity ma3 = new MethodActivity();
        ma3.setFilteredMethodQualifier("a.filter.method");
        ma3.setRepresentedQuantity(Quantity.of(10.0, Unit.JOULE));

        List<Activity> activities = List.of(ma1, ma2, ma3);
        Map<String, DataPoint> unfiltered = testee.aggregateActivityToDataPoints(activities, false);
        assertEquals(3, unfiltered.size());

        Map<String, DataPoint> filtered = testee.aggregateActivityToDataPoints(activities, true);
        assertEquals(1, filtered.size());
    }
}
