# Spring Reactive Lab: Composition and Backpressure in WebFlux

This lab demonstrates two key aspects of reactive programming in Spring WebFlux:
1. **Composition** using `Flux.combineLatest()` to merge multiple reactive streams with different emission rates
2. **Backpressure** handling strategies when producers are faster than consumers

## Part 1: Composition Power

### Overview
The application simulates a stock dashboard that combines:
1. **Price Stream**: Emits random stock prices every 500ms
2. **Sentiment Stream**: Emits random market sentiment ("Bullish", "Bearish", "Neutral") every 2 seconds

The key reactive operator used is `Flux.combineLatest()`, which automatically:
- Waits for updates from either stream
- Combines the latest values from both streams
- Emits a new `StockInfo` object whenever any input stream updates

## Part 2: Backpressure Handling

### Overview
This lab demonstrates how to handle situations where a producer generates data faster than a consumer can process it.

### Scenarios:
1. **Fast Producer**: Generates 1000 events per second (every 1ms)
2. **Slow Consumer**: Can only process 1 event every 100ms

### Backpressure Strategies Demonstrated:

#### 1. **No Strategy (Overflow)**
   - Endpoint: `GET /backpressure/overflow`
   - Shows what happens without any backpressure handling
   - Producer runs at full speed without consideration for consumer capacity

#### 2. **Drop Strategy**
   - Endpoint: `GET /backpressure/drop`
   - Uses `.onBackpressureDrop()` to discard excess events when downstream can't keep up
   - Prevents memory overflow but loses data

#### 3. **Buffer Strategy**
   - Endpoint: `GET /backpressure/buffer`
   - Uses `.onBackpressureBuffer(10)` to keep up to 10 items in memory
   - Fails with an error when buffer is full (BufferOverflowStrategy.ERROR)

## Project Structure

```
src/main/java/com/leolabs/reactive/
├── dashboard/                          # Composition lab
│   ├── SpringReactiveApplication.java
│   ├── ApplicationConfig.java
│   ├── StockInfo.java
│   ├── StockService.java
│   └── StockController.java
└── backpressure/                       # Backpressure lab
    └── BackpressureController.java
```

## How to Run

1. **Build the project:**
   ```bash
   ./gradlew build
   ```

2. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```

3. **Test Composition Lab:**
   ```bash
   curl -N http://localhost:8080/dashboard/AAPL
   ```

4. **Test Backpressure Lab:**

   **Without backpressure handling:**
   ```bash
   curl -N http://localhost:8080/backpressure/overflow
   ```

   **With drop strategy:**
   ```bash
   curl -N http://localhost:8080/backpressure/drop
   ```

   **With buffer strategy:**
   ```bash
   curl -N http://localhost:8080/backpressure/buffer
   ```

   **View statistics:**
   ```bash
   curl http://localhost:8080/backpressure/stats
   ```

   **Reset counters:**
   ```bash
   curl http://localhost:8080/backpressure/reset
   ```

## Simulating a Slow Consumer

Since standard HTTP clients may read data quickly, the backpressure endpoints include built-in delays to simulate a slow consumer:
- Each event processing includes a 100ms delay
- This creates the backpressure scenario where producer (1ms) is faster than consumer (100ms)

## Expected Observations

1. **Overflow Endpoint**: Will produce events as fast as possible, potentially overwhelming the system
2. **Drop Endpoint**: Will drop events when consumer can't keep up (check logs for dropped counts)
3. **Buffer Endpoint**: Will buffer up to 10 events, then fail with an error when buffer is full

## Learning Points

### Composition:
- How to combine multiple reactive streams efficiently
- Handling streams with different emission rates
- Using `Flux.combineLatest()` for real-time data merging

### Backpressure:
- Understanding producer-consumer speed mismatches
- Implementing different backpressure strategies
- Choosing between data loss (drop) and memory usage (buffer)
- Monitoring backpressure events through logging

## Testing

Run the tests with:
```bash
./gradlew test
```
