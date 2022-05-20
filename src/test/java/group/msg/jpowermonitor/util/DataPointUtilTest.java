package group.msg.jpowermonitor.util;

import group.msg.jpowermonitor.dto.DataPoint;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataPointUtilTest {

    private static final DataPoint DP1 = new DataPoint("x", BigDecimal.ZERO, "w", LocalDateTime.now());
    private static final DataPoint DP2 = new DataPoint("y", BigDecimal.ONE, "w", LocalDateTime.now());

    @Test
    void areAddableTest() {
        assertTrue(DataPointUtil.areAddable(DP1, DP2));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
     void areNotAddableFailBecauseOfValueNullTest() {
        assertThrows(Exception.class, () -> DataPointUtil.areAddable(DP1, null));
        Exception ex = assertThrows(Exception.class, () -> DataPointUtil.areAddable(null, DP2));
    }

    @Test
    void areNotAddableBecauseOfValueNullTest() {
        DataPoint dp2 = new DataPoint("y", null, "w", LocalDateTime.now());
        assertFalse(DataPointUtil.areAddable(DP1, dp2));
        assertFalse(DataPointUtil.areAddable(dp2, DP1));
    }

    @Test
    void areNotAddableBecauseOfUnitNullTest() {
        DataPoint dp2 = new DataPoint("y", BigDecimal.ZERO, null, LocalDateTime.now());
        assertFalse(DataPointUtil.areAddable(DP1, dp2));
        assertFalse(DataPointUtil.areAddable(dp2, DP1));
    }

    @Test
    void areNotAddableBecauseOfDifferentUnitsTest() {
        DataPoint dp2 = DataPointUtil.cloneWithNewUnit(DP2, "wh");
        assertFalse(DataPointUtil.areAddable(DP1, dp2));
    }

    @Test
    void addTwoDataPointsTest() {
        DataPoint dpSum = DataPointUtil.add(DP1, DP2);
        assertEquals(DP1.getValue().add((DP2.getValue())), dpSum.getValue());
    }

    @Test
    void addMultipleDataPointsTest() {
        DataPoint dp3 = new DataPoint("x", BigDecimal.TEN, "w", LocalDateTime.now());
        DataPoint dp4 = new DataPoint("x", BigDecimal.valueOf(100), "w", LocalDateTime.now());
        DataPoint dpSum = DataPointUtil.add(DP1, DP2, dp3, dp4);
        assertEquals(BigDecimal.valueOf(111), dpSum.getValue());
    }

    @Test
    void addMultipleDataPointsWithDifferentUnitsTest() {
        DataPoint dp3 = new DataPoint("x", BigDecimal.TEN, "w", LocalDateTime.now());
        DataPoint dp4 = new DataPoint("x", BigDecimal.valueOf(100), "wh", LocalDateTime.now());
        DataPoint dpSum = DataPointUtil.add(DP1, DP2, dp3, dp4);
        assertEquals(BigDecimal.valueOf(11), dpSum.getValue());
    }

    @Test
    void cloneWithNewUnitTest() {
        DataPoint dp3 = DataPointUtil.cloneWithNewUnit(DP1, "wh");
        assertNotEquals(dp3, DP1);
        assertEquals("wh", dp3.getUnit());
    }
}
