package group.msg.jpowermonitor.util;

import java.math.BigDecimal;

public class Converter {
    private static final BigDecimal JOULE_TO_WATT_HOURS_FACTOR = new BigDecimal("3600");
    private static final BigDecimal WATT_HOURS_TO_KWH_FACTOR = new BigDecimal("1000");

    public static BigDecimal convertJouleToWattHours(BigDecimal joule) {
        return joule.divide(JOULE_TO_WATT_HOURS_FACTOR, Constants.MATH_CONTEXT);
    }

    public static BigDecimal convertWattHoursToJoule(BigDecimal wattHours) {
        return wattHours.multiply(JOULE_TO_WATT_HOURS_FACTOR, Constants.MATH_CONTEXT);
    }

    public static BigDecimal convertJouleToKiloWattHours(BigDecimal joule) {
        return convertJouleToWattHours(joule).divide(WATT_HOURS_TO_KWH_FACTOR, Constants.MATH_CONTEXT);
    }

    public static BigDecimal convertKiloWattHoursToCarbonDioxideGrams(BigDecimal kWh, BigDecimal energyMix) {
        return kWh.multiply(energyMix, Constants.MATH_CONTEXT);
    }

    public static BigDecimal convertJouleToCarbonDioxideGrams(BigDecimal joule, BigDecimal energyMix) {
        return convertKiloWattHoursToCarbonDioxideGrams(convertJouleToKiloWattHours(joule), energyMix);
    }
}
