package com.leolabs.reactive.dashboard;

import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StockService {
    private static final Logger logger = Logger.getLogger(StockService.class.getName());
    private static final Random random = new Random();
    
    private static final List<String> SENTIMENTS = List.of("Bullish", "Bearish", "Neutral");
    
    /**
     * Emits a random price between 100.0 and 200.0 every 500ms
     */
    public Flux<Double> getPriceStream() {
        return Flux.interval(Duration.ofMillis(500))
                .map(tick -> {
                    double price = 100.0 + (random.nextDouble() * 100.0);
                    price = Math.round(price * 100.0) / 100.0; // Round to 2 decimal places
                    logger.info("Price tick: " + price);
                    return price;
                })
                .doOnError(error -> logger.severe("Error in price stream: " + error.getMessage()))
                .doFinally(signal -> {
                    if (signal == SignalType.CANCEL) {
                        logger.info("Price stream was cancelled");
                    }
                });
    }
    
    /**
     * Emits a random sentiment every 2 seconds (slower than price)
     */
    public Flux<String> getSentimentStream() {
        return Flux.interval(Duration.ofSeconds(2))
                .map(tick -> {
                    String sentiment = SENTIMENTS.get(random.nextInt(SENTIMENTS.size()));
                    logger.info("Sentiment tick: " + sentiment);
                    return sentiment;
                })
                .doOnError(error -> logger.severe("Error in sentiment stream: " + error.getMessage()))
                .doFinally(signal -> {
                    if (signal == SignalType.CANCEL) {
                        logger.info("Sentiment stream was cancelled");
                    }
                });
    }
    
    /**
     * Combines the latest price and sentiment streams into a StockInfo object
     * Demonstrates the power of reactive composition with combineLatest
     */
    public Flux<StockInfo> getDashboardStream(String symbol) {
        Flux<Double> priceFlux = getPriceStream();
        Flux<String> sentimentFlux = getSentimentStream();
        
        return Flux.combineLatest(
                priceFlux,
                sentimentFlux,
                (price, sentiment) -> new StockInfo(symbol, price, sentiment, Instant.now())
        )
        .doOnNext(stockInfo -> 
            logger.info("Combined: " + stockInfo.symbol() + 
                       " $" + stockInfo.price() + 
                       " [" + stockInfo.sentiment() + "]")
        )
        .doOnError(error -> logger.severe("Error in dashboard stream: " + error.getMessage()))
        .doFinally(signal -> logger.info("Dashboard stream completed with signal: " + signal));
    }
}
