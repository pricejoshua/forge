package forge.ai.profile;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Lightweight profiler for AI performance bottlenecks.
 * 
 * Usage:
 * 1. Run with -DAiProfiling=true JVM argument
 * 2. Play a game or run AI simulations
 * 3. Call AiPerformanceProfiler.printStats() to see results
 * 
 * Example output shows operations sorted by total time descending,
 * helping identify the biggest performance bottlenecks.
 */
public class AiPerformanceProfiler {
    private static final boolean ENABLED = Boolean.getBoolean("AiProfiling");
    private static final ConcurrentHashMap<String, OperationStats> stats = new ConcurrentHashMap<>();

    /**
     * Statistics for a single operation
     */
    private static class OperationStats {
        private final AtomicLong callCount = new AtomicLong(0);
        private final AtomicLong totalTimeNanos = new AtomicLong(0);

        void record(long nanos) {
            callCount.incrementAndGet();
            totalTimeNanos.addAndGet(nanos);
        }

        long getCallCount() {
            return callCount.get();
        }

        long getTotalTimeNanos() {
            return totalTimeNanos.get();
        }

        long getAverageTimeNanos() {
            long count = callCount.get();
            return count > 0 ? totalTimeNanos.get() / count : 0;
        }
    }

    /**
     * AutoCloseable profiler for a single operation
     */
    private static class ProfileScope implements AutoCloseable {
        private final String operation;
        private final long startTime;

        ProfileScope(String operation) {
            this.operation = operation;
            this.startTime = System.nanoTime();
        }

        @Override
        public void close() {
            try {
                long elapsed = System.nanoTime() - startTime;
                stats.computeIfAbsent(operation, k -> new OperationStats()).record(elapsed);
            } catch (Exception e) {
                // Swallow exceptions - profiling should never break game logic
            }
        }
    }

    /**
     * No-op profiler used when profiling is disabled
     */
    private static class NoOpProfileScope implements AutoCloseable {
        @Override
        public void close() {
            // No-op
        }
    }

    private static final AutoCloseable NO_OP = new NoOpProfileScope();

    /**
     * Start profiling an operation. Use with try-with-resources.
     * 
     * @param operation the name of the operation being profiled
     * @return an AutoCloseable that records the operation time when closed
     */
    public static AutoCloseable profile(String operation) {
        if (!ENABLED) {
            return NO_OP;
        }
        try {
            return new ProfileScope(operation);
        } catch (Exception e) {
            // If profiler creation fails, return no-op to avoid breaking game logic
            return NO_OP;
        }
    }

    /**
     * Check if profiling is currently enabled
     * 
     * @return true if profiling is enabled, false otherwise
     */
    public static boolean isEnabled() {
        return ENABLED;
    }

    /**
     * Print sorted statistics to console.
     * Operations are sorted by total time descending.
     */
    public static void printStats() {
        if (!ENABLED || stats.isEmpty()) {
            return;
        }

        System.out.println("=== AI Performance Profile ===");
        
        stats.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(
                Comparator.comparingLong(OperationStats::getTotalTimeNanos).reversed()
            ))
            .forEach(entry -> {
                String operation = entry.getKey();
                OperationStats opStats = entry.getValue();
                long calls = opStats.getCallCount();
                long avgNanos = opStats.getAverageTimeNanos();
                long totalNanos = opStats.getTotalTimeNanos();
                
                // Convert to milliseconds for display
                long avgMs = avgNanos / 1_000_000;
                long totalMs = totalNanos / 1_000_000;
                
                System.out.printf("%s: %d calls, avg %dms, total %dms%n",
                    operation, calls, avgMs, totalMs);
            });
    }

    /**
     * Clear all statistics
     */
    public static void reset() {
        stats.clear();
    }
}
