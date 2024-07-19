package group.msg.jpowermonitor.config.dto;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

/**
 * Data class for configuration for csv recording tools.
 */
@Data
public class CsvRecordingCfg {
    @Nullable
    private String resultCsv;
    @Nullable
    private String measurementCsv;
}
