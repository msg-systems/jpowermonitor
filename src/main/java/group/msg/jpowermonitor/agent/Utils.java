package group.msg.jpowermonitor.agent;

import group.msg.jpowermonitor.dto.DataPoint;
import group.msg.jpowermonitor.ResultCsvWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;

@Slf4j
class Utils {

    private static final double JOULE_TO_WATT_HOURS_FACTOR = 3600.0d;
    private static final double WATT_HOURS_TO_KWH_FACTOR = 1000.0d;
    protected static final String UNIT_JOULE = "j";
    protected static final String UNIT_WATTS = "w";

    protected static final String FILE_NAME_PREFIX = JPowerMonitorAgent.class.getSimpleName() + "_";
    protected static final String SEPARATOR = "-----------------------------------------------------------------------------------------";
    protected static final MathContext MATH_CONTEXT = new MathContext(30, RoundingMode.HALF_UP);

    protected static void createCsvAndWriteToFile(Map<String, DataPoint> measurements, String fileName) {
        writeToFile(createCsv(measurements), fileName);
    }

    protected static String createCsv(Map<String, DataPoint> measurements) {
        StringBuilder csv = new StringBuilder();
        measurements.forEach((method, energy) -> csv.append(ResultCsvWriter.createCsvEntryForDataPoint(energy, method, "")));
        return csv.toString();
    }

    protected static void writeToFile(String csv, String fileName) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, false))) {
            bw.write(csv);
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage(), ex);
        }
    }

    protected static double convertJouleToWattHours(double joule) {
        return joule / JOULE_TO_WATT_HOURS_FACTOR;
    }

    protected static double convertJouleToKiloWattHours(double joule) {
        return convertJouleToWattHours(joule) / WATT_HOURS_TO_KWH_FACTOR;
    }
}
