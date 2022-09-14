package group.msg.jpowermonitor.config;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

/**
 * Data class for configuration for csv recording tools.
 */
@Data
public class CsvRecording {
    @Nullable
    private String resultCsv;
    @Nullable
    private String measurementCsv;
}
