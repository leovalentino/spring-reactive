package com.leolabs.reactive.dashboard;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@RestController
@RequestMapping("/backpressure")
public class BackpressureController {
    private static final Logger logger = Logger.getLogger(BackpressureController.class.getName());
    private final AtomicInteger overflowCounter = new AtomicInteger(0);
    private final AtomicInteger dropCounter = new AtomicInteger(0);
    private final AtomicInteger bufferCounter = new AtomicInteger(0);

    /**
     * Fast producer without any backpressure handling
     * Produces 1000 events per second (every 1ms)
     * This will demonstrate the default behavior when downstream can't keep up
     */
    @GetMapping(value = "/overflow", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> overflow() {
        return Flux.interval(Duration.ofMillis(1))
                .map(i -> {
                    int count = overflowCounter.incrementAndGet();
                    if (count % 100 == 0) {
                        logger.info("Produced event #" + count + " (overflow endpoint)");
                    }
                    return "Event " + i + " (overflow)";
                })
                .doOnError(error -> logger.severe("Error in overflow stream: " + error.getMessage()))
                .doFinally(signal -> {
                    if (signal == SignalType.CANCEL) {
                        logger.info("Overflow stream cancelled");
                    }
                });
    }

    /**
     * Backpressure strategy: Drop
     * When downstream is too slow, extra events are dropped
     * This prevents memory overflow but loses data
     */
    @GetMapping(value = "/drop", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> drop() {
        return Flux.interval(Duration.ofMillis(1))
                .onBackpressureDrop(dropped -> {
                    int droppedCount = dropCounter.incrementAndGet();
                    if (droppedCount % 100 == 0) {
                        logger.warning("Dropped " + droppedCount + " events so far");
                    }
                })
                .map(i -> {
                    // Simulate slow consumer by adding delay in processing
                    // In real scenario, the consumer would be slow
                    return "Event " + i + " (drop)";
                })
                .doOnNext(event -> {
                    // Simulate slow processing (100ms per event)
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                })
                .doOnError(error -> logger.severe("Error in drop stream: " + error.getMessage()))
                .doFinally(signal -> {
                    if (signal == SignalType.CANCEL) {
                        logger.info("Drop stream cancelled. Total dropped: " + dropCounter.get());
                    }
                });
    }

    /**
     * Backpressure strategy: Buffer with limited capacity
     * Keeps up to 10 items in memory, fails when buffer is full
     */
    @GetMapping(value = "/buffer", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> buffer() {
        return Flux.interval(Duration.ofMillis(1))
                .onBackpressureBuffer(10,
                        buffer -> {
                            int bufferedCount = bufferCounter.incrementAndGet();
                            logger.info("Buffer overflow: " + bufferedCount + " items buffered");
                        },
                        reactor.core.publisher.BufferOverflowStrategy.ERROR)
                .map(i -> {
                    // Simulate slow consumer
                    return "Event " + i + " (buffer)";
                })
                .doOnNext(event -> {
                    // Simulate slow processing (100ms per event)
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                })
                .doOnError(error -> {
                    logger.severe("Buffer overflow error: " + error.getMessage());
                    logger.severe("Total buffered before error: " + bufferCounter.get());
                })
                .doFinally(signal -> {
                    if (signal == SignalType.CANCEL) {
                        logger.info("Buffer stream cancelled");
                    }
                });
    }

    /**
     * Helper endpoint to get statistics about backpressure handling
     */
    @GetMapping("/stats")
    public String getStats() {
        return String.format(
                "Backpressure Statistics:%n" +
                "Overflow events produced: %d%n" +
                "Drop events dropped: %d%n" +
                "Buffer items buffered: %d%n",
                overflowCounter.get(),
                dropCounter.get(),
                bufferCounter.get()
        );
    }

    /**
     * Reset all counters
     */
    @GetMapping("/reset")
    public String reset() {
        overflowCounter.set(0);
        dropCounter.set(0);
        bufferCounter.set(0);
        return "Counters reset";
    }
}
