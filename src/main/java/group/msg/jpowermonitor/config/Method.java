package group.msg.jpowermonitor.config;

import group.msg.jpowermonitor.dto.DataPoint;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.LongAdder;

@Data
@RequiredArgsConstructor
public class Method {
    private final String name;
    private final LongAdder energy;
    private DataPoint power;
}
