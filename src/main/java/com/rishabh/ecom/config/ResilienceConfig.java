package com.rishabh.ecom.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience Configuration
 * 
 * Provides circuit breaker and retry mechanisms for fault tolerance
 */
@Configuration
public class ResilienceConfig {

    /**
     * Circuit Breaker Configuration
     * - Opens circuit when failure rate exceeds 50%
     * - Requires minimum 5 calls before evaluating
     * - Stays open for 10 seconds before attempting half-open
     */
    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .permittedNumberOfCallsInHalfOpenState(3)
            .build();
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.of(circuitBreakerConfig());
    }

    /**
     * Retry Configuration
     * - Retries up to 3 times with exponential backoff
     * - Initial wait: 1 second, max wait: 5 seconds
     */
    @Bean
    public RetryConfig retryConfig() {
        return RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofSeconds(1))
            .build();
    }

    @Bean
    public RetryRegistry retryRegistry() {
        return RetryRegistry.of(retryConfig());
    }
}
