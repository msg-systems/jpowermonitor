package group.msg.jpowermonitor.agent;

import group.msg.jpowermonitor.dto.Activity;
import group.msg.jpowermonitor.dto.DataPoint;
import group.msg.jpowermonitor.dto.MethodActivity;
import group.msg.jpowermonitor.dto.Quantity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PowerStatisticsTest {

    private static final DataPoint DP1 = new DataPoint("x", 0.0, Unit.WATT, LocalDateTime.now(), null);
    private static final DataPoint DP2 = new DataPoint("y", 1.0, Unit.WATT, LocalDateTime.now(), null);

    @Test
    void areAddableTest() {
        assertTrue(new PowerStatistics(0L, 0L, 0L, null, null).areDataPointsAddable(DP1, DP2));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void areNotAddableFailBecauseOfValueNullTest() {
        PowerStatistics testee = new PowerStatistics(0L, 0L, 0L, null, null);
        assertThatThrownBy(() -> testee.areDataPointsAddable(DP1, null)).isInstanceOf(Exception.class);
        assertThatThrownBy(() -> testee.areDataPointsAddable(null, DP2)).isInstanceOf(Exception.class);
    }

    @Test
    void areNotAddableBecauseOfValueNullTest() {
        DataPoint dp2 = new DataPoint("y", null, Unit.WATT, LocalDateTime.now(), null);
        PowerStatistics testee = new PowerStatistics(0L, 0L, 0L, null, null);
        assertThat(testee.areDataPointsAddable(DP1, dp2)).isFalse();
        assertThat(testee.areDataPointsAddable(dp2, DP1)).isFalse();
    }

    @Test
    void areNotAddableBecauseOfUnitNullTest() {
        DataPoint dp2 = new DataPoint("y", 0.0, null, LocalDateTime.now(), null);
        PowerStatistics testee = new PowerStatistics(0L, 0L, 0L, null, null);
        assertThat(testee.areDataPointsAddable(DP1, dp2)).isFalse();
        assertThat(testee.areDataPointsAddable(dp2, DP1)).isFalse();
    }

    @Test
    void areNotAddableBecauseOfDifferentUnitsTest() {
        PowerStatistics testee = new PowerStatistics(0L, 0L, 0L, null, null);
        DataPoint dp2 = testee.cloneDataPointWithNewUnit(DP2, Unit.WATTHOURS);
        assertThat(testee.areDataPointsAddable(DP1, dp2)).isFalse();
    }

    @Test
    void addTwoDataPointsTest() {
        PowerStatistics testee = new PowerStatistics(0L, 0L, 0L, null, null);
        DataPoint dpSum = testee.addDataPoint(DP1, DP2);
        assertThat(dpSum.getValue()).isEqualTo(DP1.getValue() + DP2.getValue());
    }

    @Test
    void addMultipleDataPointsTest() {
        PowerStatistics testee = new PowerStatistics(0L, 0L, 0L, null, null);
        DataPoint dp3 = new DataPoint("x", 10.0, Unit.WATT, LocalDateTime.now(), null);
        DataPoint dp4 = new DataPoint("x", 100.0, Unit.WATT, LocalDateTime.now(), null);
        DataPoint dpSum = testee.addDataPoint(DP1, DP2, dp3, dp4);
        assertThat(dpSum.getValue()).isEqualTo(111.0);
    }

    @Test
    void addMultipleDataPointsWithDifferentUnitsTest() {
        PowerStatistics testee = new PowerStatistics(0L, 0L, 0L, null, null);
        DataPoint dp3 = new DataPoint("x", 10.0, Unit.WATT, LocalDateTime.now(), null);
        DataPoint dp4 = new DataPoint("x", 100.0, Unit.WATTHOURS, LocalDateTime.now(), null);
        DataPoint dpSum = testee.addDataPoint(DP1, DP2, dp3, dp4);
        assertThat(dpSum.getValue()).isEqualTo(11.0);
    }

    @Test
    void cloneWithNewUnitTest() {
        PowerStatistics testee = new PowerStatistics(0L, 0L, 0L, null, null);
        DataPoint dp3 = testee.cloneDataPointWithNewUnit(DP1, Unit.WATTHOURS);
        assertNotEquals(dp3, DP1);
        assertEquals(Unit.WATTHOURS, dp3.getUnit());
    }

    @Test
    void aggregateActivityTest() {
        PowerStatistics testee = new PowerStatistics(0L, 0L, 0L, null, null);

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
