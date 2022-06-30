package group.msg.jpowermonitor.dto;

import java.time.LocalDateTime;

/**
 * Represents an act of work as part of a measured time frame.</br>
 * As an example, we take a time frame of 1,000ms. We look every 10ms
 * what kind of work our application does, e.g. which thread called which method.
 * In this case, an {@link Activity} represents <code>(10ms / 1,000ms) = 1/100</code> part of
 * the work done, and thus is entitled to the same percentage of a quantity based
 * on the time frame. Assuming it took the CPU package 31W to execute our example
 * (constantly over 1,000ms), an {@link Activity} would represent <code>(1/100 * 31W) = 0.31W</code>
 * power over the same period and should be treated as such.
 * */
public interface Activity {
    /**
     * @return
     *  ID of the process the activity was part of when measured
     * */
    Long getProcessID();

    /**
     * @return
     *  {@link LocalDateTime} when the activity was measured
     * */
    LocalDateTime getTime();

    /**
     * @param asFiltered
     *  if the filter configured in javaAgent.packageFilter should be applied, see jpowermonitor.yaml
     *
     * @return
     *  identifier of the kind of work measured, e.g. a method name
     * */
    String getIdentifier(boolean asFiltered);

    /**
     * @return
     *  the {@link Quantity} represented by this activity
     * */
    Quantity getRepresentedQuantity();

    /**
     * @return
     *  if the activity can be used for further processing, e.g. if a {@link Quantity}
     *  has been attributed already
     * */
    boolean isFinalized();
}
