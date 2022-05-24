package group.msg.jpowermonitor.config;

import group.msg.jpowermonitor.JPowerMonitorException;

/**
 * Interface for reading JPowerMonitor configuration.
 * <p>
 * Any specific reading is part of the implementation, this might include finding the source as well
 * as caching.
 */
@FunctionalInterface
public interface JPowerMonitorConfigProvider {

    /**
     * Reads a JPowerMonitor configuration using the given source.
     *
     * @param source Source to read from, it strongly depends on the implementation what #source
     *               should be, can be a resource, a file or whatever
     * @return a valid configuration
     * @throws JPowerMonitorException On any error that occurs during reading the configuration
     */
    JPowerMonitorConfig readConfig(String source) throws JPowerMonitorException;

}
