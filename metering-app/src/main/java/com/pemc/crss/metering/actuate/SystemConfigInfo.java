package com.pemc.crss.metering.actuate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SystemConfigInfo implements InfoContributor {

    private final CacheManager cacheManager;

    @Autowired
    public SystemConfigInfo(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void contribute(Info.Builder builder) {
        Cache configCache = cacheManager.getCache("config");

        if (configCache != null) {
            log.debug("Displaying system configuration from cache.");

            builder.withDetail("System Configuration", configCache.getNativeCache());
        }

    }

}
