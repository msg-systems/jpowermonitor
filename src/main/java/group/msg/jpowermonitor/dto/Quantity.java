package group.msg.jpowermonitor.dto;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class Quantity {
    BigDecimal value;
    String unit;
}
