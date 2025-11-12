package com.rishabh.ecom.config;

// Temporarily disabled - bucket4j dependency commented out in pom.xml
// import io.github.bucket4j.BucketConfiguration;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// import java.time.Duration;

/**
 * Rate Limiting Configuration
 * 
 * Rate limiting is configured but not actively enforced in this version.
 * To enable rate limiting, create a filter/interceptor that uses Bucket4j.
 * 
 * Default limits:
 * - 100 requests per minute per user/IP
 * - Can be customized per endpoint or user role
 * 
 * NOTE: Currently disabled - uncomment bucket4j dependency in pom.xml to enable
 */
// @Configuration
public class RateLimitConfig {

    /**
     * Default bucket configuration for rate limiting
     * - Capacity: 100 requests
     * - Refill: 100 tokens per minute
     * - Initial tokens: 100
     */
    // @Bean
    // public BucketConfiguration defaultBucketConfig() {
    //     return BucketConfiguration.builder()
    //         .addLimit(limit -> limit
    //             .capacity(100)
    //             .refillIntervally(100, Duration.ofMinutes(1))
    //             .initialTokens(100))
    //         .build();
    // }
    
    /**
     * Admin bucket configuration (higher limits)
     * - Capacity: 500 requests
     * - Refill: 500 tokens per minute
     */
    // @Bean
    // public BucketConfiguration adminBucketConfig() {
    //     return BucketConfiguration.builder()
    //         .addLimit(limit -> limit
    //             .capacity(500)
    //             .refillIntervally(500, Duration.ofMinutes(1))
    //             .initialTokens(500))
    //         .build();
    // }
}
