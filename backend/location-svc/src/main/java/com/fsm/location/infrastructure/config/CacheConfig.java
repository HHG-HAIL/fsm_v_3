package com.fsm.location.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caching configuration for the Location Service.
 * Uses Caffeine cache for in-memory caching to reduce database load.
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    /**
     * Cache name for all technician locations
     */
    public static final String ALL_LOCATIONS_CACHE = "allTechnicianLocations";
    
    /**
     * Configures Caffeine cache manager with specific settings.
     * Cache entries expire after 30 seconds to ensure reasonably fresh data
     * while still providing performance benefits.
     * 
     * @return configured cache manager
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(ALL_LOCATIONS_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS) // Cache for 30 seconds
                .maximumSize(100) // Max 100 entries (reasonable for location data)
                .recordStats()); // Enable statistics for monitoring
        return cacheManager;
    }
}
