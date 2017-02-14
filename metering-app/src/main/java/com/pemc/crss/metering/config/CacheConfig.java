package com.pemc.crss.metering.config;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Arrays;

@Configuration
@EnableAsync
public class CacheConfig {

    // TODO: Change implementation to ehcache 3.x JSR 107
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        Cache configCache = new ConcurrentMapCache("config");
        Cache mspCache = new ConcurrentMapCache("msp");
        cacheManager.setCaches(Arrays.asList(configCache, mspCache));

        return cacheManager;
    }

}
