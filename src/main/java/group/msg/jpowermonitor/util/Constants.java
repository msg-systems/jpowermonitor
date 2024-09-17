package group.msg.jpowermonitor.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public interface Constants {
    double ONE_HUNDRED = 100;
    double ONE_THOUSAND = 1000;
    String APP_TITLE = "jPowerMonitor";
    String LOG_PREFIX = "jPowerMonitor: ";
    String SEPARATOR = "-----------------------------------------------------------------------------------------";
    String NEW_LINE = System.lineSeparator();
    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd'T'HH:mm:ss-SSS");
    DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###0.#####", DecimalFormatSymbols.getInstance(Locale.getDefault()));
}
