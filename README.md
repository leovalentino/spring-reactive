# Spring Reactive Lab: Composition Power of WebFlux

This lab demonstrates the composition capabilities of Spring WebFlux using `Flux.combineLatest()` to merge multiple reactive streams with different emission rates.

## Overview

The application simulates a stock dashboard that combines:
1. **Price Stream**: Emits random stock prices every 500ms
2. **Sentiment Stream**: Emits random market sentiment ("Bullish", "Bearish", "Neutral") every 2 seconds

The key reactive operator used is `Flux.combineLatest()`, which automatically:
- Waits for updates from either stream
- Combines the latest values from both streams
- Emits a new `StockInfo` object whenever any input stream updates

## Project Structure

```
src/main/java/com/leolabs/reactive/dashboard/
├── SpringReactiveApplication.java    # Main Spring Boot application
├── ApplicationConfig.java            # Spring configuration
├── StockInfo.java                    # Record representing stock data
├── StockService.java                 # Reactive service with stream composition
└── StockController.java              # REST controller with SSE endpoint
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

3. **Access the dashboard:**
   - Open your browser or use a tool like `curl` to connect to the SSE endpoint:
   ```bash
   curl -N http://localhost:8080/dashboard/AAPL
   ```
   - Or navigate to `http://localhost:8080/dashboard/AAPL` in a browser that supports Server-Sent Events

4. **Health check:**
   ```bash
   curl http://localhost:8080/dashboard/health
   ```

## Key Features Demonstrated

### 1. **Reactive Stream Composition**
- `Flux.combineLatest()` merges streams with different emission frequencies
- The combined stream updates whenever either price (500ms) OR sentiment (2s) changes
- No blocking or manual synchronization required

### 2. **Server-Sent Events (SSE)**
- The endpoint produces `MediaType.TEXT_EVENT_STREAM_VALUE`
- Clients receive a continuous stream of stock updates
- Automatic reconnection support in compliant clients

### 3. **Logging and Observability**
- Each stream logs its emissions
- The combined stream logs the final `StockInfo` objects
- Subscription and cancellation events are tracked

## Expected Output

When you connect to the SSE endpoint, you'll see JSON objects like:
```json
{"symbol":"AAPL","price":156.78,"sentiment":"Bullish","time":"2024-01-30T10:15:30.123Z"}
```

In the console logs, you'll observe:
```
Price tick: 156.78
Sentiment tick: Bullish
Combined: AAPL $156.78 [Bullish]
```

Notice that price updates appear more frequently than sentiment updates, but `combineLatest` ensures you always get the latest of both.

## Learning Points

- **Reactive Composition**: How to combine multiple reactive streams efficiently
- **Backpressure Handling**: Reactor handles different emission rates automatically
- **Non-blocking I/O**: The entire pipeline runs without blocking threads
- **SSE with WebFlux**: How to serve continuous updates to HTTP clients

## Testing

Run the tests with:
```bash
./gradlew test
```
