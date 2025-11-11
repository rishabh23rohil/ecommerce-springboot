package com.rishabh.ecom.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Aspect to help detect cache hits by measuring execution time.
 * Note: Spring's cache interceptor runs before method execution.
 * If method executes, it's a cache miss. If method doesn't execute, it's a cache hit.
 * This aspect logs execution time to help identify cache hits (very fast execution).
 */
@Aspect
@Component
public class CacheLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(CacheLoggingAspect.class);

    @Around("@annotation(cacheable)")
    public Object logCacheAccess(ProceedingJoinPoint joinPoint, Cacheable cacheable) throws Throwable {
        String cacheName = cacheable.value().length > 0 ? cacheable.value()[0] : "unknown";
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // Very short duration (< 10ms) suggests cache hit (though method still executed)
            // Longer duration suggests cache miss with DB query
            // Note: True cache hits don't execute the method, so this is a best-effort indicator
            if (duration < 10) {
                log.debug("⚡ Fast execution (possible cache): {} - method={}, duration={}ms", 
                    cacheName, methodName, duration);
            }
            
            return result;
        } catch (Throwable t) {
            log.error("❌ Cache operation failed: {} - method={}", cacheName, methodName, t);
            throw t;
        }
    }
}

