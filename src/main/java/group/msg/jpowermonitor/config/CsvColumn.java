package group.msg.jpowermonitor.config;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CsvColumn {
    private int index;
    private String name;
    private BigDecimal energyInIdleMode;
}
