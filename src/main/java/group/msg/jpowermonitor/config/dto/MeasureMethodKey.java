package group.msg.jpowermonitor.config.dto;

import group.msg.jpowermonitor.JPowerMonitorException;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum MeasureMethodKey {
    LHM("lhm", "Libre Hardware Monitor"),
    CSV("csv", "Comma Separated Values File"),
    EST("est", "Estimated values according to Etsy's Cloud Jewels method");

    private final String key;
    private final String name;

    MeasureMethodKey(String k, String n) {
        this.key = k;
        this.name = n;
    }

    public static MeasureMethodKey of(String providedKey) {
        return Arrays.stream(values())
            .filter(v -> v.getKey().equalsIgnoreCase(providedKey))
            .findFirst()
            .orElseThrow(() -> new JPowerMonitorException("Unable to recognize MeasureMethod with key " + providedKey));
    }
}
