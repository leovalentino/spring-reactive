package com.leolabs.reactive.dashboard;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    
    @Bean
    public StockService stockService() {
        return new StockService();
    }
}
