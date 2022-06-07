package group.msg.jpowermonitor.ohwm;

import com.fasterxml.jackson.databind.ObjectMapper;
import group.msg.jpowermonitor.JPowerMonitorException;
import group.msg.jpowermonitor.MeasureMethod;
import group.msg.jpowermonitor.agent.Unit;
import group.msg.jpowermonitor.config.DefaultConfigProvider;
import group.msg.jpowermonitor.config.JPowerMonitorConfig;
import group.msg.jpowermonitor.config.PathElement;
import group.msg.jpowermonitor.dto.DataPoint;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Slf4j
public class MeasureOpenHwMonitor implements MeasureMethod {

    HttpClient client;
    JPowerMonitorConfig config;

    @Override
    public void init(String configFile) throws JPowerMonitorException {
        config = new DefaultConfigProvider().readConfig(configFile);
        client = HttpClientBuilder.create().build();
    }


    @Override
    public @NotNull List<DataPoint> measure() throws JPowerMonitorException {
        try {
            LocalDateTime time = LocalDateTime.now();
            HttpResponse response = client.execute(
                new HttpGet(config.getOpenHardwareMonitor().getUrl()));
            ObjectMapper objectMapper = new ObjectMapper();
            DataElem root = objectMapper.readValue(response.getEntity().getContent(),
                DataElem.class);
            List<DataPoint> result = new ArrayList<>();
            for (PathElement pathElement : config.getOpenHardwareMonitor().getPaths()) {
                DataPoint dp = createDataPoint(root, pathElement, time);
                result.add(dp);
            }
            return result;
        } catch (IOException e) {
            throw new JPowerMonitorException(
                "Unable to reach hardware monitor at url: " + config.getOpenHardwareMonitor()
                    .getUrl() + "!", e);
        }
    }

    @Override
    public @NotNull DataPoint measureFirst() throws JPowerMonitorException {
        try {
            LocalDateTime time = LocalDateTime.now();
            HttpResponse response = client.execute(
                new HttpGet(config.getOpenHardwareMonitor().getUrl()));
            ObjectMapper objectMapper = new ObjectMapper();
            DataElem root = objectMapper.readValue(response.getEntity().getContent(),
                DataElem.class);
            // config assures that getPaths is not null and has at least one element!
            PathElement pathElement = config.getOpenHardwareMonitor().getPaths().get(0);
            return createDataPoint(root, pathElement, time);
        } catch (IOException e) {
            throw new JPowerMonitorException(
                "Unable to reach hardware monitor at url: " + config.getOpenHardwareMonitor()
                    .getUrl() + "!", e);
        }
    }

    @NotNull
    private DataPoint createDataPoint(DataElem root, PathElement pathElement, LocalDateTime time) {
        DataElem elem = findElement(root, pathElement.getPath().toArray());
        if (elem == null) {
            throw new JPowerMonitorException(
                "Unable to find element for path " + pathElement.getPath() + "!");
        }
        log.trace("Value: {} -> {}", pathElement, elem);
        String[] valueAndUnit = elem.getValue().split("\\s+");// (( "5,4 W" ))
        BigDecimal value = new BigDecimal(valueAndUnit[0].replace(',', '.').trim());
        Unit unit = Unit.fromAbbreviation(valueAndUnit[1].trim());
        return new DataPoint(String.join("->", pathElement.getPath()), value, unit, time, null);
    }

    @Override
    public @NotNull List<String> configuredSensors() {
        return config.getOpenHardwareMonitor().getPaths().stream()
            .map(p -> String.join("->", p.getPath())).collect(Collectors.toList());
    }

    @Override
    public @NotNull Map<String, BigDecimal> defaultEnergyInIdleModeForMeasuredSensors() {
        Map<String, BigDecimal> energyInIdleModeForMeasuredSensors = new HashMap<>();
        config.getOpenHardwareMonitor().getPaths().stream()
            .filter(x -> x.getEnergyInIdleMode() != null)
            .forEach(p -> energyInIdleModeForMeasuredSensors.put(String.join("->", p.getPath()),
                p.getEnergyInIdleMode()));
        return energyInIdleModeForMeasuredSensors;
    }

    @Override
    public int getSamplingInterval() {
        return config.getSamplingIntervalInMs();
    }

    @Override
    public int initCycles() {
        return config.getInitCycles();
    }

    @Override
    public int getSamplingIntervalForInit() {
        return config.getSamplingIntervalForInitInMs();
    }

    @Override
    public int getCalmDownIntervalInMs() {
        return config.getCalmDownIntervalInMs();
    }

    @Override
    public @Nullable Path getPathToResultCsv() {
        return config.getCsvRecording().getResultCsv() != null ? Paths.get(
            config.getCsvRecording().getResultCsv()) : null;
    }

    @Override
    public @Nullable Path getPathToMeasurementCsv() {
        return config.getCsvRecording().getMeasurementCsv() != null ? Paths.get(
            config.getCsvRecording().getMeasurementCsv()) : null;
    }

    @Override
    public @NotNull BigDecimal getPercentageOfSamplesAtBeginningToDiscard() {
        return config.getPercentageOfSamplesAtBeginningToDiscard();
    }

    private DataElem findElement(DataElem root, Object[] path) {
        return findElementInTree(root, path, (String) path[path.length - 1], 0);
    }

    private DataElem findElementInTree(@NonNull DataElem elem, Object[] parentNodes, String name,
        int level) {
        log.trace("looking at element '{}'", elem.getText());
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
