package group.msg.jpowermonitor.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class Constants {
    public static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    public static final BigDecimal ONE_THOUSAND = new BigDecimal("1000");
    public final static MathContext MATH_CONTEXT = new MathContext(25, RoundingMode.HALF_UP);
    public static String APP_TITLE = "jPowerMonitor";
}
