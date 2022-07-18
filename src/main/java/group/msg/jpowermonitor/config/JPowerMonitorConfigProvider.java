package group.msg.jpowermonitor.config;

import group.msg.jpowermonitor.*;

/**
 * Interface for reading JPowerMonitor configuration.
 * <p>
 * Any specific reading is part of the implementation, this might include finding the source as well
 * as caching.
 */
public interface JPowerMonitorConfigProvider {

    JPowerMonitorConfig getCachedConfig() throws JPowerMonitorException;

    /**
     * Reads a JPowerMonitor configuration using the given source.
     *
     * @param source Source to read from, it strongly depends on the implementation what #source
     *               should be, can be a resource, a file or whatever
     * @return a valid configuration
     * @throws JPowerMonitorException On any error that occurs during reading the configuration
     */
    JPowerMonitorConfig readConfig(String source) throws JPowerMonitorException;

    /**
     * Checks wether the given source name is a valid configuration source.
     * <p>
     * The default implementations assumes any non-empty and non-null string as a valid source.
     * Other implementations can overwrite this to implement different logic.
     *
     * @param source Source name that has to be checked
     * @return <code>true</code> if the given source is a valid source name
     */
    default boolean isValidSource(String source) {
        return source != null && !source.isEmpty();
    }
}
