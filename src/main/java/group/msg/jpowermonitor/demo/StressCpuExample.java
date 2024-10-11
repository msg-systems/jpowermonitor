package group.msg.jpowermonitor.demo;

import group.msg.jpowermonitor.util.CmdLineArgs;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Example program to set CPU under (full) load and get comparable energy measurement results.
 *
 * @author deinerj
 */
@Slf4j
public class StressCpuExample {

    public static final short DEFAULT_SECONDS_TO_RUN = 15;

    private static CmdLineArgs cmdLineArgs;
    private static long sequentialLoopCounter = 0;
    private static long percentagedLoopCounter = 0;
    private static long parallelLoopCounter = 0;

    public static void main(String[] args) {
        cmdLineArgs = parseCmdLineArgs(args);
        runSequentialEnergyMeasurementAndBenchmarkUsingOneCpuThread();
        runPercentagedEnergyMeasurementAndBenchmarkUsingOneCpuThread();
        runParallelEnergyMeasurementAndBenchmarkUsingMultipleCpuThreads();
        System.exit(0); // Important to exit properly since JavaAgent will not exit gracefully without
    }

    private static void runSequentialEnergyMeasurementAndBenchmarkUsingOneCpuThread() {
        long start = System.currentTimeMillis();
        logStart("sequential  ", cmdLineArgs.getSecondsToRun(), 1);
        sequentialLoopCounter = runMeasurement(cmdLineArgs.getSecondsToRun(), 1, StressCpuExample::iAm100Percent);
        logEnd("End   sequential  ", start, sequentialLoopCounter, sequentialLoopCounter);
    }

    private static void runPercentagedEnergyMeasurementAndBenchmarkUsingOneCpuThread() {
        logStart("percentaged  ", cmdLineArgs.getSecondsToRun(), 1);
        long start = System.currentTimeMillis();
        percentagedLoopCounter += runMeasurement(cmdLineArgs.getSecondsToRun(), 0.5f, StressCpuExample::iNeed50Percent);
        logEnd("50%   percentaged ", start, percentagedLoopCounter, sequentialLoopCounter);
        percentagedLoopCounter += runMeasurement(cmdLineArgs.getSecondsToRun(), 0.3f, StressCpuExample::iNeed30Percent);
        logEnd("80%   percentaged ", start, percentagedLoopCounter, sequentialLoopCounter);
        percentagedLoopCounter += runMeasurement(cmdLineArgs.getSecondsToRun(), 0.1f, StressCpuExample::iNeed10Percent);
        logEnd("90%   percentaged ", start, percentagedLoopCounter, sequentialLoopCounter);
        percentagedLoopCounter += runMeasurement(cmdLineArgs.getSecondsToRun(), 0.05f, StressCpuExample::iNeed5Percent);
        logEnd("95%   percentaged ", start, percentagedLoopCounter, sequentialLoopCounter);
        percentagedLoopCounter += runMeasurement(cmdLineArgs.getSecondsToRun(), 0.02f, StressCpuExample::iNeed2Percent);
        logEnd("97%   percentaged ", start, percentagedLoopCounter, sequentialLoopCounter);
        percentagedLoopCounter += runMeasurement(cmdLineArgs.getSecondsToRun(), 0.02f, StressCpuExample::iNeed2PercentToo);
        logEnd("99%   percentaged ", start, percentagedLoopCounter, sequentialLoopCounter);
        percentagedLoopCounter += runMeasurement(cmdLineArgs.getSecondsToRun(), 0.01f, StressCpuExample::iNeed1Percent);
        logEnd("End   percentaged ", start, percentagedLoopCounter, sequentialLoopCounter);
    }

    private static void runParallelEnergyMeasurementAndBenchmarkUsingMultipleCpuThreads() {
        logStart("parallel     ", cmdLineArgs.getSecondsToRun(), cmdLineArgs.getCpuThreads());
        long start = System.currentTimeMillis();
        parallelLoopCounter = runParallelEndlessLoopCpuStressTest(cmdLineArgs.getCpuThreads(), cmdLineArgs.getSecondsToRun());
        logEnd("End   parallel    ", start, parallelLoopCounter, sequentialLoopCounter);
    }

    @NotNull
    private static CmdLineArgs parseCmdLineArgs(String[] args) {
        CmdLineArgs cmdLineArgs = new CmdLineArgs();
        cmdLineArgs.setSecondsToRun(DEFAULT_SECONDS_TO_RUN);
        cmdLineArgs.setCpuThreads(Runtime.getRuntime().availableProcessors());
        if (args != null && args.length > 0) {
            try {
                cmdLineArgs.setSecondsToRun(Short.parseShort(args[0]));
                if (args.length > 1) {
                    cmdLineArgs.setCpuThreads(Integer.parseInt(args[1]));
                }
            } catch (NumberFormatException ex) {
                log.info("Could not parse argument, using default of {} seconds and {} CPU threads: {}", DEFAULT_SECONDS_TO_RUN, cmdLineArgs.getCpuThreads(), ex.getMessage());
            }
        } else {
            log.info("No arguments, using default of {} seconds and {} CPU threads", DEFAULT_SECONDS_TO_RUN, cmdLineArgs.getCpuThreads());
        }
        return cmdLineArgs;
    }

    private static void logStart(String logPrefix, short secondsToRun, int usedCpuThreads) {
        log.info("Start {}EndlessLoopCpuStressTest   for {} seconds using {} CPU thread(s)...", logPrefix, secondsToRun, usedCpuThreads);
    }

    private static void logEnd(String logPrefix, long start, long loopCounter, long sequentialLoopCounter) {
        log.info("{} EndlessLoopCpuStressTest, took {} seconds, ran loop {} times, {} % of sequential", logPrefix, calcDurationSec(start), loopCounter, calcProgressPercentaged(loopCounter, sequentialLoopCounter));
    }

    private static long calcDurationSec(long start) {
        return (System.currentTimeMillis() - start) / 1000;
    }

    private static short calcProgressPercentaged(long actual, long benchmark) {
        return (short) ((double) actual / benchmark * 100);
    }

    /**
     * Run single endless loop CPU stress test sequentially
     *
     * @param secondsToRun seconds to run
     * @param factor       factor to multiply secondsToRun
     * @param runWorkload  workload to run
     * @return loop counter
     */
    public static long runMeasurement(short secondsToRun, float factor, Function<Long, Long> runWorkload) {
        float secondsProportionally = secondsToRun * factor;
        long runUntil = System.currentTimeMillis() + (long) (secondsProportionally * 1000);
        return runWorkload.apply(runUntil);
    }

    /**
     * Run endless loop CPU stress test in parallel
     *
     * @param parallelThreads number of parallel threads
     * @param secondsToRun    seconds to run
     * @return sum of all loop counters
     */
    public static long runParallelEndlessLoopCpuStressTest(int parallelThreads, short secondsToRun) {
        LongAdder sumLoopCounter = new LongAdder();
        IntStream.range(0, parallelThreads)
            .parallel()
            .forEach(k -> sumLoopCounter.add(runMeasurement(secondsToRun, 1, StressCpuExample::iAm100PercentParallel)));
        return sumLoopCounter.longValue();
    }

    /**
     * Baseline for percentaged and parallel workload, runs 100% CPU load
     * sequentially for given time
     *
     * @param runUntil run until this time
     * @return loop counter
     */
    public static long iAm100Percent(long runUntil) {
        long loopCounter = 0;
        while (System.currentTimeMillis() < runUntil) {
            loopCounter++;
        }
        return loopCounter;
    }

    /**
     * @return true if this is a benchmark run
     */
    public static boolean isBenchmarkRun() {
        return sequentialLoopCounter > 0 || percentagedLoopCounter > 0 || parallelLoopCounter > 0;
    }

    /**
     * @return sum of all loop counters
     */
    public static long getBenchmarkResult() {
        return sequentialLoopCounter + percentagedLoopCounter + parallelLoopCounter;
    }

    // --------------------------------------------------------------------------------------------------------------
    // The following methods contain duplicated code.
    // This is on purpose, otherwise the measurements with jPowerMonitor Agent cannot determine the correct method.
    // If, for example, the code were outsourced to a separate method, this method would always be displayed in
    // the measurement results.
    // --------------------------------------------------------------------------------------------------------------

    private static long iAm100PercentParallel(long runUntil) {
        long loopCounter = 0;
        while (System.currentTimeMillis() < runUntil) {
            loopCounter++;
        }
        return loopCounter;
    }

    private static long iNeed50Percent(long runUntil) {
        long loopCounter = 0;
        while (System.currentTimeMillis() < runUntil) {
            loopCounter++;
        }
        return loopCounter;
    }

    private static long iNeed30Percent(long runUntil) {
        long loopCounter = 0;
        while (System.currentTimeMillis() < runUntil) {
            loopCounter++;
        }
        return loopCounter;
    }

    private static long iNeed10Percent(long runUntil) {
        long loopCounter = 0;
        while (System.currentTimeMillis() < runUntil) {
            loopCounter++;
        }
        return loopCounter;
    }

    private static long iNeed5Percent(long runUntil) {
        long loopCounter = 0;
        while (System.currentTimeMillis() < runUntil) {
            loopCounter++;
        }
        return loopCounter;
    }

    private static long iNeed2Percent(long runUntil) {
        long loopCounter = 0;
        while (System.currentTimeMillis() < runUntil) {
            loopCounter++;
        }
        return loopCounter;
    }

    private static long iNeed2PercentToo(long runUntil) {
        long loopCounter = 0;
        while (System.currentTimeMillis() < runUntil) {
            loopCounter++;
        }
        return loopCounter;
    }

    private static long iNeed1Percent(long runUntil) {
        long loopCounter = 0;
        while (System.currentTimeMillis() < runUntil) {
            loopCounter++;
        }
        return loopCounter;
    }
}
