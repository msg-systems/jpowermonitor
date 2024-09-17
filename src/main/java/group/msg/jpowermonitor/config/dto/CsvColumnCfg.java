package group.msg.jpowermonitor.config.dto;

import lombok.Data;

/**
 * Data class for csv column config.
 *
 * @see CsvMeasurementCfg
 */
@Data
public class CsvColumnCfg {
    private int index;
    private String name;
    private Double energyInIdleMode;
}
