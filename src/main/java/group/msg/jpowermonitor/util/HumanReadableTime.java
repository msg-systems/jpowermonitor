package group.msg.jpowermonitor.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Class for formatting time in nanos or millis to a readable format.
 */
public class HumanReadableTime {
    private static final long MILLION = 1000 * 1000;
    private static final Map<TimeUnit, String> TIME_UNITS_NANOS = timeUnitsToNanos();
    private static final Map<TimeUnit, String> TIME_UNITS_MILLIS = timeUnitsToMillis();

    private static final BigDecimal factorNanosToHours = new BigDecimal("3600000000000");
    private static final MathContext mc = new MathContext(50, RoundingMode.HALF_UP);

    public static BigDecimal nanosToHours(long nanos) {
        return new BigDecimal(nanos).divide(factorNanosToHours, mc).setScale(20, RoundingMode.HALF_UP);
    }

    static Map<TimeUnit, String> timeUnitsToNanos() {
        Map<TimeUnit, String> numMap = new LinkedHashMap<>(timeUnitsToMillis());
        numMap.put(TimeUnit.NANOSECONDS, "ns");
        return Collections.unmodifiableMap(numMap);
    }

    static Map<TimeUnit, String> timeUnitsToMillis() {
        Map<TimeUnit, String> numMap = new LinkedHashMap<>(); // order is important
        numMap.put(TimeUnit.DAYS, "d");
        numMap.put(TimeUnit.HOURS, "h");
        numMap.put(TimeUnit.MINUTES, "m");
        numMap.put(TimeUnit.SECONDS, "s");
        numMap.put(TimeUnit.MILLISECONDS, "ms");
        return Collections.unmodifiableMap(numMap);
    }

    public static String ofNanos(long nanos) {
        StringBuilder builder = new StringBuilder();
        long acc = nanos;
        int cutOff = 0;
        Map<TimeUnit, String> reference = nanos < MILLION ? TIME_UNITS_NANOS : TIME_UNITS_MILLIS;
        for (Map.Entry<TimeUnit, String> e : reference.entrySet()) {
            long convert = e.getKey().convert(acc, TimeUnit.NANOSECONDS);
            if (convert > 0) {
                builder.append(convert).append(e.getValue()).append(" ");
                acc -= TimeUnit.NANOSECONDS.convert(convert, e.getKey());
                cutOff = 1;
            }
        }
        return builder.substring(0, builder.length() - cutOff);
    }

    public static String ofMillis(long millis) {
        StringBuilder builder = new StringBuilder();
        if (millis == 0) {
            return "0ms";
        }
        long acc = millis;
        int cutOff = 0;
        for (Map.Entry<TimeUnit, String> e : TIME_UNITS_MILLIS.entrySet()) {
            long convert = e.getKey().convert(acc, TimeUnit.MILLISECONDS);
            if (convert > 0) {
                builder.append(convert).append(e.getValue()).append(" ");
                acc -= TimeUnit.MILLISECONDS.convert(convert, e.getKey());
                cutOff = 1;
            }
        }
        return builder.substring(0, builder.length() - cutOff);
    }
}
