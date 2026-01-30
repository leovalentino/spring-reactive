package com.leolabs.reactive.dashboard;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/dashboard")
public class StockController {
    
    private final StockService stockService;
    
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }
    
    @GetMapping(value = "/{symbol}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<StockInfo> getDashboard(@PathVariable String symbol) {
        return stockService.getDashboardStream(symbol)
                .doOnSubscribe(subscription -> 
                    System.out.println("Client subscribed to dashboard for symbol: " + symbol)
                )
                .doOnCancel(() -> 
                    System.out.println("Client unsubscribed from dashboard for symbol: " + symbol)
                );
    }
    
    @GetMapping("/health")
    public String health() {
        return "Stock Dashboard is running!";
    }
}
