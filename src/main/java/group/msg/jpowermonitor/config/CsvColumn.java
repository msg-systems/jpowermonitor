package group.msg.jpowermonitor.config;

import lombok.Data;

/**
 * Data class for csv column config.
 *
 * @see CsvMeasurementCfg
 */
@Data
public class CsvColumn {
    private int index;
    private String name;
    private Double energyInIdleMode;
}
