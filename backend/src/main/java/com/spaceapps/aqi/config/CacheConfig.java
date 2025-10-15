// src/main/java/com/spaceapps/aqi/config/CacheConfig.java
package com.spaceapps.aqi.config;

import java.util.List;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
public class CacheConfig {

    @Bean
    CacheManager cacheManager() {
        var aqiByCity = new CaffeineCache(
            "aqiByCity",
            Caffeine.newBuilder()
                    .maximumSize(500)             // cap entries
                    .expireAfterWrite(java.time.Duration.ofMinutes(5))
                    .build()
        );

        var forecastByCity = new CaffeineCache(
            "forecastByCity",
            Caffeine.newBuilder()
                    .maximumSize(500)
                    .expireAfterWrite(java.time.Duration.ofMinutes(10))
                    .build()
        );

        var mgr = new SimpleCacheManager();
        mgr.setCaches(List.of(aqiByCity, forecastByCity));
        return mgr;
    }
}
