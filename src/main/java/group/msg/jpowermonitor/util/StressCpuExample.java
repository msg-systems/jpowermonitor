package group.msg.jpowermonitor.util;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Example program to set CPU under (full) load and get comparable energy measurement results.
 *
 * @author deinerj
 */
public class StressCpuExample {

    public static final short DEFAULT_SECONDS_TO_RUN = 15;

    public static void main(String[] args) {
        CmdLineArgs cmdLineArgs = parseCmdLineArgs(args);
        long sequentialLoopCounter = runSequentialEnergyMeasurementAndBenchmarkUsingOneCpuThread(cmdLineArgs);
        runPercentagedEnergyMeasurementAndBenchmarkUsingOneCpuThread(cmdLineArgs, sequentialLoopCounter);
        runParallelEnergyMeasurementAndBenchmarkUsingMultipleCpuThreads(cmdLineArgs, sequentialLoopCounter);
        System.exit(0); // Important to exit properly since JavaAgent will not exit gracefully without
    }

    private static long runSequentialEnergyMeasurementAndBenchmarkUsingOneCpuThread(CmdLineArgs cmdLineArgs) {
        long start = System.currentTimeMillis();
        logStart("sequential  ", cmdLineArgs.getSecondsToRun(), 1);
        long sequentialLoopCounter = runMeasurement(cmdLineArgs.getSecondsToRun(), 1, StressCpuExample::iAm100Percent);
        logEnd("End   sequential  ", start, sequentialLoopCounter, sequentialLoopCounter);
        return sequentialLoopCounter;
    }

    private static void runPercentagedEnergyMeasurementAndBenchmarkUsingOneCpuThread(CmdLineArgs cmdLineArgs, long sequentialLoopCounter) {
        logStart("percentaged ", cmdLineArgs.getSecondsToRun(), 1);
        long percentagedLoopCounter = 0;
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

    private static void runParallelEnergyMeasurementAndBenchmarkUsingMultipleCpuThreads(CmdLineArgs cmdLineArgs, long sequentialLoopCounter) {
        logStart("parallel    ", cmdLineArgs.getSecondsToRun(), cmdLineArgs.getCpuThreads());
        long start = System.currentTimeMillis();
        long parallelLoopCounter = runParallelEndlessLoopCpuStressTest(cmdLineArgs.getCpuThreads(), cmdLineArgs.getSecondsToRun());
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
                System.out.printf("Could not parse argument, using default of %s seconds and %s CPU threads: %s%n", DEFAULT_SECONDS_TO_RUN, cmdLineArgs.getCpuThreads(), ex.getMessage());
            }
        } else {
            System.out.printf("No arguments, using default of %s seconds and %s CPU threads%n", DEFAULT_SECONDS_TO_RUN, cmdLineArgs.getCpuThreads());
        }
        return cmdLineArgs;
    }

    private static void logStart(String logPrefix, short secondsToRun, int usedCpuThreads) {
        System.out.printf("Start %sEndlessLoopCpuStressTest   for %s seconds using %s CPU thread(s)...%n", logPrefix, secondsToRun, usedCpuThreads);
    }

    private static void logEnd(String logPrefix, long start, long loopCounter, long sequentialLoopCounter) {
        System.out.printf("%s EndlessLoopCpuStressTest, took %s seconds, ran loop %s times, %s %% of sequential%n", logPrefix, calcDurationSec(start), loopCounter, calcProgressPercentaged(loopCounter, sequentialLoopCounter));
    }

    private static long calcDurationSec(long start) {
        return (System.currentTimeMillis() - start) / 1000;
    }

    private static short calcProgressPercentaged(long actual, long benchmark) {
        return (short) ((double) actual / benchmark * 100);
    }

    public static long runMeasurement(short secondsToRun, float factor, Function<Long, Long> runWorkload) {
        float secondsProportionally = secondsToRun * factor;
        long runUntil = System.currentTimeMillis() + (long) (secondsProportionally * 1000);
        return runWorkload.apply(runUntil);
    }

    public static long runParallelEndlessLoopCpuStressTest(int parallelThreads, short secondsToRun) {
        LongAdder sumLoopCounter = new LongAdder();
        IntStream.range(0,parallelThreads)
            .parallel()
            .forEach(k -> sumLoopCounter.add(runMeasurement(secondsToRun, 1, StressCpuExample::iAm100PercentParallel)));
        return sumLoopCounter.longValue();
    }

    public static long iAm100PercentParallel(long runUntil) {
        long loopCounter = 0;
        while (System.currentTimeMillis() < runUntil) {
            loopCounter++;
        }
        return loopCounter;
    }

    public static long iAm100Percent(long runUntil) {
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

    @Data
    private static class CmdLineArgs {
        private short secondsToRun;
        private int cpuThreads;
    }
}
