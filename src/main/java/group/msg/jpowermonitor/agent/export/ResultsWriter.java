package group.msg.jpowermonitor.agent.export;

import group.msg.jpowermonitor.dto.DataPoint;

import java.util.Map;

public interface ResultsWriter {
    void writePowerConsumptionPerMethod(Map<String, DataPoint> measurements);

    void writePowerConsumptionPerMethodFiltered(Map<String, DataPoint> measurements);

    void writeEnergyConsumptionPerMethod(Map<String, DataPoint> measurements);

    void writeEnergyConsumptionPerMethodFiltered(Map<String, DataPoint> measurements);
}
