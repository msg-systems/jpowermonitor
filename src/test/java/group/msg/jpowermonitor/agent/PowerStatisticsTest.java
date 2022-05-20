package group.msg.jpowermonitor.agent;

import group.msg.jpowermonitor.dto.DataPoint;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PowerStatisticsTest {

    private static final DataPoint DP1 = new DataPoint("x", BigDecimal.ZERO, Unit.WATT, LocalDateTime.now());
    private static final DataPoint DP2 = new DataPoint("y", BigDecimal.ONE, Unit.WATT, LocalDateTime.now());

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
        DataPoint dp2 = new DataPoint("y", null, Unit.WATT, LocalDateTime.now());
        PowerStatistics testee = new PowerStatistics(0L, 0L, 0L, null, null);
        assertFalse(testee.areDataPointsAddable(DP1, dp2));
        assertFalse(testee.areDataPointsAddable(dp2, DP1));
    }

    @Test
    void areNotAddableBecauseOfUnitNullTest() {
        DataPoint dp2 = new DataPoint("y", BigDecimal.ZERO, null, LocalDateTime.now());
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
        DataPoint dp3 = new DataPoint("x", BigDecimal.TEN, Unit.WATT, LocalDateTime.now());
        DataPoint dp4 = new DataPoint("x", BigDecimal.valueOf(100), Unit.WATT, LocalDateTime.now());
        DataPoint dpSum = testee.addDataPoint(DP1, DP2, dp3, dp4);
        assertEquals(BigDecimal.valueOf(111), dpSum.getValue());
    }

    @Test
    void addMultipleDataPointsWithDifferentUnitsTest() {
        PowerStatistics testee = new PowerStatistics(0L, 0L, 0L, null, null);
        DataPoint dp3 = new DataPoint("x", BigDecimal.TEN, Unit.WATT, LocalDateTime.now());
        DataPoint dp4 = new DataPoint("x", BigDecimal.valueOf(100), Unit.WATTHOURS, LocalDateTime.now());
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
}
