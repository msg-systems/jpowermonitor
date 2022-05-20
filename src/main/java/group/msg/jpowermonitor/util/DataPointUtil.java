package group.msg.jpowermonitor.util;

import group.msg.jpowermonitor.dto.DataPoint;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility for <code>DataPoint</code> DTO
 */
@Slf4j
public class DataPointUtil {

    /**
     * Checks if two <code>DataPoint</code> instances are addable
     *
     * @param dp1 <code>DataPoint</code>
     * @param dp2 <code>DataPoint</code>
     * @return <code>true</code>, if addable
     */
    public static boolean areAddable(@NotNull DataPoint dp1, @NotNull DataPoint dp2) {
        log.trace("dp1 = {}, dp2 = {}", dp1, dp2);
        if (dp1.getUnit() == null || dp2.getUnit() == null
            || dp1.getValue() == null || dp2.getValue() == null
            || !dp1.getUnit().equals(dp2.getUnit())) {
            log.error("not addable: dp1 = {}, dp2 = {}", dp1, dp2);
            return false;
        }
        return true;
    }

    /**
     * Add the values of multiple <code>DataPoint</code> instances - if units match<br>
     * First <code>DataPoint</code> in <code>dataPoints</code> is reference for unit
     *
     * @param dataPoints <code>DataPoint</code> instances to sum up values
     * @return sum of all given <code>dataPoints</code> with same unit
     */
    public static DataPoint add(@NotNull DataPoint... dataPoints) {
        if (dataPoints == null || dataPoints.length < 2) {
            throw new IllegalArgumentException("dataPoints must contain at least two elements!");
        }
        DataPoint reference = dataPoints[0];
        AtomicReference<BigDecimal> sum = new AtomicReference<>(BigDecimal.ZERO);
        Arrays.stream(dataPoints).filter(dp -> areAddable(reference, dp)).forEach(dp -> sum.getAndAccumulate(dp.getValue(), BigDecimal::add));
        return new DataPoint(reference.getName(), sum.get(), reference.getUnit(), LocalDateTime.now());
    }

    /**
     * Clones <code>DataPoint</code> but with new <code>unit</code>
     *
     * @param dp <code>DataPoint</code> to clone
     * @param unit new unit
     * @return <code>DataPoint</code> with new <code>unit</code>
     */
    public static DataPoint cloneWithNewUnit(@NotNull DataPoint dp, @NotNull String unit) {
        return new DataPoint(dp.getName(), dp.getValue(), unit, dp.getTime());
    }
}
