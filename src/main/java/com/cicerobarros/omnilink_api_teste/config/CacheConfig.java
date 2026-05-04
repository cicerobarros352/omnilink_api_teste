package com.cicerobarros.omnilink_api_teste.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String VEHICLES_CACHE = "vehicles";
    public static final String CUSTOMERS_CACHE = "customers";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(VEHICLES_CACHE, CUSTOMERS_CACHE);
    }
}
