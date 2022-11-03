package group.msg.jpowermonitor.config;

import lombok.Data;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
/**
 * Data class for csv measurement config.
 * @see CsvColumn
 */
@Data
public class CsvMeasurementCfg {
    private String inputFile;
    // this is set during initialization of CommaSeparatedValuesReader.
    private Path inputFileAsPath;
    private String lineToRead = "last";
    private List<CsvColumn> columns;
    private String delimiter = ",";
    private String encoding;

    public Charset getEncodingAsCharset() {
        if (encoding == null) {
            return StandardCharsets.ISO_8859_1;
        }
        return Charset.forName(encoding);
    }
}
