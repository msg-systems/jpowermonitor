package group.msg.jpowermonitor.agent;

import group.msg.jpowermonitor.dto.Activity;
import group.msg.jpowermonitor.dto.DataPoint;
import group.msg.jpowermonitor.dto.MethodActivity;
import group.msg.jpowermonitor.dto.Quantity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PowerStatisticsTest {

    private static final DataPoint DP1 = new DataPoint("x", BigDecimal.ZERO, Unit.WATT, LocalDateTime.now(), null);
    private static final DataPoint DP2 = new DataPoint("y", BigDecimal.ONE, Unit.WATT, LocalDateTime.now(), null);

    @Test
    void areAddableTest() {
        assertTrue(new PowerStatistics(0L, 0L, 0L, null, null).areDataPointsAddable(DP1, DP2));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void areNotAddableFailBecauseOfValueNullTest() {
        PowerStatistics testee = new PowerStatistics(0L, 0L, 0L, null, null);
        assertThrows(Exception.class, () -> testee.areDataPointsAddable(DP1, null));
        Exception ex = assertThrows(Exception.class, () -> testee.areDataPointsAddable(null, DP2));
    }

    @Test
    void areNotAddableBecauseOfValueNullTest() {
        DataPoint dp2 = new DataPoint("y", null, Unit.WATT, LocalDateTime.now(), null);
        PowerStatistics testee = new PowerStatistics(0L, 0L, 0L, null, null);
        assertFalse(testee.areDataPointsAddable(DP1, dp2));
        assertFalse(testee.areDataPointsAddable(dp2, DP1));
    }

    @Test
    void areNotAddableBecauseOfUnitNullTest() {
        DataPoint dp2 = new DataPoint("y", BigDecimal.ZERO, null, LocalDateTime.now(), null);
        PowerStatistics testee = new PowerStatistics(0L, 0L, 0L, null, null);
        assertFalse(testee.areDataPointsAddable(DP1, dp2));
        assertFalse(testee.areDataPointsAddable(dp2, DP1));
    }

    @Test
    void areNotAddableBecauseOfDifferentUnitsTest() {
        PowerStatistics testee = new PowerStatistics(0L, 0L, 0L, null, null);
        DataPoint dp2 = testee.cloneDataPointWithNewUnit(DP2, Unit.WATTHOURS);
        assertFalse(testee.areDataPointsAddable(DP1, dp2));
    }

    @Test
    void addTwoDataPointsTest() {
        PowerStatistics testee = new PowerStatistics(0L, 0L, 0L, null, null);
        DataPoint dpSum = testee.addDataPoint(DP1, DP2);
        assertEquals(DP1.getValue().add((DP2.getValue())), dpSum.getValue());
    }

    @Test
    void addMultipleDataPointsTest() {
        PowerStatistics testee = new PowerStatistics(0L, 0L, 0L, null, null);
        DataPoint dp3 = new DataPoint("x", BigDecimal.TEN, Unit.WATT, LocalDateTime.now(), null);
        DataPoint dp4 = new DataPoint("x", BigDecimal.valueOf(100), Unit.WATT, LocalDateTime.now(), null);
        DataPoint dpSum = testee.addDataPoint(DP1, DP2, dp3, dp4);
        assertEquals(BigDecimal.valueOf(111), dpSum.getValue());
    }

    @Test
    void addMultipleDataPointsWithDifferentUnitsTest() {
        PowerStatistics testee = new PowerStatistics(0L, 0L, 0L, null, null);
        DataPoint dp3 = new DataPoint("x", BigDecimal.TEN, Unit.WATT, LocalDateTime.now(), null);
        DataPoint dp4 = new DataPoint("x", BigDecimal.valueOf(100), Unit.WATTHOURS, LocalDateTime.now(), null);
        DataPoint dpSum = testee.addDataPoint(DP1, DP2, dp3, dp4);
        assertEquals(BigDecimal.valueOf(11), dpSum.getValue());
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
        ma1.setRepresentedQuantity(Quantity.of(BigDecimal.ZERO, Unit.JOULE));

        MethodActivity ma2 = new MethodActivity();
        ma2.setMethodQualifier("no.filter.method.Either");
        ma2.setRepresentedQuantity(Quantity.of(BigDecimal.ONE, Unit.JOULE));

        MethodActivity ma3 = new MethodActivity();
        ma3.setFilteredMethodQualifier("a.filter.method");
        ma3.setRepresentedQuantity(Quantity.of(BigDecimal.TEN, Unit.JOULE));

        List<Activity> activities = List.of(ma1, ma2, ma3);
        Map<String, DataPoint> unfiltered = testee.aggregateActivityToDataPoints(activities, false);
        assertEquals(3, unfiltered.size());

        Map<String, DataPoint> filtered = testee.aggregateActivityToDataPoints(activities, true);
        assertEquals(1, filtered.size());
    }
}
