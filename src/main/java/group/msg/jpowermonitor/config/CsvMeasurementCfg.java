package group.msg.jpowermonitor.config;

import lombok.Data;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Data
public class CsvMeasurementCfg {
    private String inputFile;
    private List<CsvColumn> columns;
    private String delimiter = ",";
    private String encoding;

    public Charset getEncodingAsCharset() {
        if (encoding == null) {
            return StandardCharsets.ISO_8859_1;
        }
        return Charset.forName(encoding);
    }

    public Path getInputFileAsPath() {
        return Paths.get(inputFile);
    }
}
