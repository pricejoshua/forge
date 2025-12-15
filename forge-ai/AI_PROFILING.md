# AI Performance Profiling Infrastructure

## Overview

The AI Performance Profiler is a lightweight, thread-safe profiling system designed to identify performance bottlenecks in the Forge AI. It provides actionable data to optimize AI decision-making processes.

## Features

- **Enable/disable via system property**: Control profiling with `-DAiProfiling=true`
- **Zero overhead when disabled**: No performance impact in production
- **Thread-safe**: Uses concurrent data structures for multi-threaded AI operations
- **Try-with-resources support**: Clean, automatic profiling with `AutoCloseable`
- **Sorted output**: Results sorted by total time (descending) to quickly identify bottlenecks

## Usage

### 1. Enable Profiling

Add the JVM argument when running Forge:

```bash
java -DAiProfiling=true -jar forge.jar
```

### 2. Run AI Operations

Play a game or run AI simulations. The profiler will automatically track:

- Token generation (`TokenAi.spawnToken`)
- Phase restrictions checking (`TokenAi.checkPhaseRestrictions`)
- API logic evaluation (`TokenAi.checkApiLogic`)
- Trigger handling (`TokenAi.doTriggerNoCost`)
- Creature evaluation (`ComputerUtilCard.evaluateCreature`)
- Static P/T application (`ComputerUtilCard.applyStaticContPT`)

### 3. View Statistics

Call `AiPerformanceProfiler.printStats()` to see results:

```
=== AI Performance Profile ===
TokenAi.spawnToken: 450 calls, avg 2ms, total 900ms
TokenAi.checkApiLogic: 450 calls, avg 1ms, total 450ms
ComputerUtilCard.evaluateCreature: 1200 calls, avg 0ms, total 300ms
TokenAi.checkPhaseRestrictions: 450 calls, avg 0ms, total 200ms
ComputerUtilCard.applyStaticContPT: 450 calls, avg 0ms, total 150ms
```

### 4. Reset Statistics (Optional)

Clear accumulated statistics:

```java
AiPerformanceProfiler.reset();
```

## Adding Profiling to New Methods

To profile additional methods, wrap the implementation with profiling:

```java
public ReturnType methodName(params) {
    try (AutoCloseable p = AiPerformanceProfiler.profile("ClassName.methodName")) {
        return methodNameImpl(params);
    } catch (Exception e) {
        // Profiler exceptions shouldn't affect game logic
        return methodNameImpl(params);
    }
}

private ReturnType methodNameImpl(params) {
    // existing method body
}
```

## Implementation Details

### Core Components

1. **AiPerformanceProfiler**: Main profiler class
   - `profile(String operation)`: Start profiling an operation
   - `printStats()`: Print sorted statistics
   - `reset()`: Clear all statistics
   - `isEnabled()`: Check if profiling is active

2. **OperationStats**: Thread-safe statistics storage
   - Uses `AtomicLong` for call counts and time tracking
   - Calculates average time per operation

3. **ProfileScope**: AutoCloseable profiling scope
   - Uses `System.nanoTime()` for high-resolution timing
   - Handles exceptions gracefully

### Thread Safety

- Uses `ConcurrentHashMap` for statistics storage
- `AtomicLong` for thread-safe counters
- Safe for concurrent AI operations

### Performance

- **When disabled (default)**: Zero overhead (no-op implementation)
- **When enabled**: Minimal overhead from `System.nanoTime()` calls

## Profiled Methods

### TokenAi
- `spawnToken()` - Token creation and initialization
- `checkPhaseRestrictions()` - Phase timing validation
- `checkApiLogic()` - Ability logic evaluation
- `doTriggerNoCost()` - Trigger decision making

### ComputerUtilCard
- `evaluateCreature()` - Creature value calculation
- `applyStaticContPT()` - Static ability P/T calculations

## Example Output Interpretation

```
TokenAi.spawnToken: 450 calls, avg 2ms, total 900ms
```

This means:
- `spawnToken()` was called 450 times
- Average execution time: 2 milliseconds
- Total time spent: 900 milliseconds (45% of total AI time in this example)

Use this data to:
1. Identify the biggest time consumers (sorted by total time)
2. Prioritize optimization efforts
3. Measure the impact of optimizations

## Notes

- Profiling data is accumulated across the entire session
- Statistics are in-memory only (not persisted)
- Console output only (no UI integration)
- Designed for development/debugging, not production use
