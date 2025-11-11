package com.rishabh.ecom.config;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.CacheManager;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Tag(name = "Cache", description = "Cache statistics and management")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/v1/cache")
public class CacheStatsController {

    private final CacheManager cacheManager;

    public CacheStatsController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Operation(
        summary = "Get cache statistics",
        description = "Returns cache statistics including cache names and approximate sizes. Requires ADMIN role."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    public Map<String, Object> cacheStats() {
        Map<String, Object> stats = new HashMap<>();
        Map<String, Object> cacheDetails = new HashMap<>();

        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Map<String, Object> cacheInfo = new HashMap<>();
                cacheInfo.put("name", cacheName);
                
                // Try to get native cache for size estimation
                Object nativeCache = cache.getNativeCache();
                if (nativeCache instanceof org.springframework.data.redis.cache.RedisCache) {
                    // Redis cache - we can't easily get size without Redis connection
                    cacheInfo.put("type", "Redis");
                    cacheInfo.put("size", "N/A (check Redis directly)");
                } else {
                    cacheInfo.put("type", nativeCache != null ? nativeCache.getClass().getSimpleName() : "Unknown");
                    cacheInfo.put("size", "N/A");
                }
                
                cacheDetails.put(cacheName, cacheInfo);
            }
        });

        stats.put("caches", cacheDetails);
        stats.put("totalCaches", cacheManager.getCacheNames().size());
        stats.put("cacheNames", cacheManager.getCacheNames());

        return stats;
    }
}

