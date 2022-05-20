package group.msg.jpowermonitor.agent;

import group.msg.jpowermonitor.dto.Activity;
import group.msg.jpowermonitor.dto.DataPoint;
import group.msg.jpowermonitor.dto.MethodActivity;
import group.msg.jpowermonitor.dto.Quantity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResultsWriterTest {

    @Test
    void aggregateActivityTest() {
        MethodActivity ma1 = new MethodActivity();
        ma1.setMethodQualifier("no.filter.Method");
        ma1.setRepresentedQuantity(new Quantity(BigDecimal.ZERO, "j"));
        MethodActivity ma2 = new MethodActivity();
        ma2.setMethodQualifier("no.filter.method.Either");
        ma2.setRepresentedQuantity(new Quantity(BigDecimal.ONE, "j"));
        MethodActivity ma3 = new MethodActivity();
        ma3.setFilteredMethodQualifier("a.filter.method");
        ma3.setRepresentedQuantity(new Quantity(BigDecimal.TEN, "j"));
        List<Activity> activities = List.of(ma1, ma2, ma3);
        Map<String, DataPoint> unfiltered = ResultsWriter.aggregateActivity(activities, false);
        assertEquals(3, unfiltered.size());
        Map<String, DataPoint> filtered = ResultsWriter.aggregateActivity(activities, true);
        assertEquals(1, filtered.size());
    }

}
