package com.leolabs.reactive.dashboard;

import java.time.Instant;

public record StockInfo(String symbol, double price, String sentiment, Instant time) {
    public StockInfo {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be null or blank");
        }
        if (sentiment == null) {
            sentiment = "Neutral";
        }
        if (time == null) {
            time = Instant.now();
        }
    }
}
