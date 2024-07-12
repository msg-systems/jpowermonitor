package group.msg.jpowermonitor.util;

public class Converter {
    private static final double JOULE_TO_WATT_HOURS_FACTOR = 3600;
    private static final double WATT_HOURS_TO_KWH_FACTOR = 1000;

    public static double convertJouleToWattHours(double joule) {
        return joule / JOULE_TO_WATT_HOURS_FACTOR;
    }

    public static double convertWattHoursToJoule(double wattHours) {
        return wattHours * JOULE_TO_WATT_HOURS_FACTOR;
    }

    public static double convertJouleToKiloWattHours(double joule) {
        return convertJouleToWattHours(joule) / WATT_HOURS_TO_KWH_FACTOR;
    }

    public static double convertKiloWattHoursToCarbonDioxideGrams(double kWh, double energyMix) {
        return kWh * energyMix;
    }

    public static double convertJouleToCarbonDioxideGrams(double joule, double energyMix) {
        return convertKiloWattHoursToCarbonDioxideGrams(convertJouleToKiloWattHours(joule), energyMix);
    }
}
