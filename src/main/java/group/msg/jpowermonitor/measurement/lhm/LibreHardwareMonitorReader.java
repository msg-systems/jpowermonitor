package group.msg.jpowermonitor.measurement.lhm;

import com.fasterxml.jackson.databind.ObjectMapper;
import group.msg.jpowermonitor.JPowerMonitorException;
import group.msg.jpowermonitor.MeasureMethod;
import group.msg.jpowermonitor.agent.Unit;
import group.msg.jpowermonitor.config.JPowerMonitorConfig;
import group.msg.jpowermonitor.config.LibreHardwareMonitorCfg;
import group.msg.jpowermonitor.config.PathElement;
import group.msg.jpowermonitor.dto.DataPoint;
import lombok.NonNull;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementation of the Libre Hardware Monitor measure method.
 *
 * @see MeasureMethod
 */
public class LibreHardwareMonitorReader implements MeasureMethod {
    HttpClient client;;
    LibreHardwareMonitorCfg lhmConfig;

    public LibreHardwareMonitorReader(JPowerMonitorConfig config) {
        Objects.requireNonNull(config.getMeasurement().getLhm(), "Libre Hardware Monitor config must be set!");
        this.lhmConfig = config.getMeasurement().getLhm();
        this.client = HttpClientBuilder.create().build();
    }

    @NotNull
    private List<DataPoint> getDataPoints(ClassicHttpResponse response, List<PathElement> paths) throws IOException {
        LocalDateTime time = LocalDateTime.now();
        ObjectMapper objectMapper = new ObjectMapper();
        DataElem root = objectMapper.readValue(response.getEntity().getContent(), DataElem.class);
        List<DataPoint> result = new ArrayList<>();
        for (PathElement pathElement : paths) {
            DataPoint dp = createDataPoint(root, pathElement, time);
            result.add(dp);
        }
        return result;
    }

    @Override
    public @NotNull List<DataPoint> measure() throws JPowerMonitorException {
        try {
            return client.execute(new HttpGet(lhmConfig.getUrl()), response -> getDataPoints(response, lhmConfig.getPaths()));
        } catch (IOException e) {
            throw new JPowerMonitorException("Unable to reach Libre Hardware Monitor at url: " + lhmConfig.getUrl() + "!", e);
        }
    }

    @NotNull
    private DataPoint getDataPoint(ClassicHttpResponse response, PathElement pathElement) throws IOException {
        LocalDateTime time = LocalDateTime.now();
        ObjectMapper objectMapper = new ObjectMapper();
        DataElem root = objectMapper.readValue(response.getEntity().getContent(), DataElem.class);
        return createDataPoint(root, pathElement, time);
    }

    @Override
    public @NotNull DataPoint measureFirstConfiguredPath() throws JPowerMonitorException {
        try {
            // the config assures that getPaths is not null and has at least one element!
            return client.execute(new HttpGet(lhmConfig.getUrl()), response -> getDataPoint(response, lhmConfig.getPaths().get(0)));
        } catch (IOException e) {
            throw new JPowerMonitorException("Unable to reach Libre Hardware Monitor at url: " + lhmConfig.getUrl() + "!", e);
        }
    }

    @NotNull
    private DataPoint createDataPoint(DataElem root, PathElement pathElement, LocalDateTime time) {
        DataElem elem = findElement(root, pathElement.getPath().toArray());
        if (elem == null) {
            throw new JPowerMonitorException("Unable to find element for path " + pathElement.getPath() + "!");
        }
        String[] valueAndUnit = elem.getValue().split("\\s+");// (( "5,4 W" ))
        BigDecimal value = new BigDecimal(valueAndUnit[0].replace(',', '.').trim());
        Unit unit = Unit.fromAbbreviation(valueAndUnit[1].trim());
        return new DataPoint(String.join("->", pathElement.getPath()), value, unit, time, null);
    }

    @Override
    public @NotNull List<String> configuredSensors() {
        return lhmConfig.getPaths()
            .stream()
            .map(p -> String.join("->", p.getPath()))
            .collect(Collectors.toList());
    }

    @Override
    public @NotNull Map<String, BigDecimal> defaultEnergyInIdleModeForMeasuredSensors() {
        Map<String, BigDecimal> energyInIdleModeForMeasuredSensors = new HashMap<>();
        lhmConfig.getPaths().stream()
            .filter(x -> x.getEnergyInIdleMode() != null)
            .forEach(p -> energyInIdleModeForMeasuredSensors.put(String.join("->", p.getPath()), p.getEnergyInIdleMode()));
        return energyInIdleModeForMeasuredSensors;
    }

    private DataElem findElement(DataElem root, Object[] path) {
        return findElementInTree(root, path, (String) path[path.length - 1], 0);
    }

    private DataElem findElementInTree(@NonNull DataElem elem, Object[] parentNodes, String name, int level) {
        if (elem.getText().equals(name)) {
            return elem;
        }
        DataElem result;
        for (DataElem child : elem.children) {
            if (parentNodes[level].equals(child.getText())) {
                result = findElementInTree(child, parentNodes, name, ++level);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}
