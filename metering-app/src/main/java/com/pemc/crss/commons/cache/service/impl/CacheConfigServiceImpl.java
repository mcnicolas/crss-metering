package com.pemc.crss.commons.cache.service.impl;

import com.pemc.crss.commons.cache.service.CacheConfigService;
import com.pemc.crss.metering.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class CacheConfigServiceImpl implements CacheConfigService {

    private final CacheService cacheService;

    @Autowired
    public CacheConfigServiceImpl(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public long getLongValueForKey(final String key, final long defaultValue) {
        Optional<String> valueString = Optional.ofNullable(cacheService.getConfig(key));

        long returnVal = defaultValue;
        if (valueString.isPresent()) {
            try {
                returnVal = Long.parseLong(valueString.get());
            } catch (NumberFormatException nfe) {
                log.error("Error parsing system config value {} given key {}", valueString.get(), key);
            }
        }

        return returnVal;
    }

    @Override
    public String getStringValueForKey(final String key, final String defaultValue) {
        Optional<String> valueString = Optional.ofNullable(cacheService.getConfig(key));

        return valueString.isPresent() ? valueString.get() : defaultValue;
    }

    @Override
    public Integer getIntegerValueForKey(String key, Integer defaultValue) {
        Optional<String> valueString = Optional.ofNullable(cacheService.getConfig(key));
        Integer returnValue = defaultValue;

        if (valueString.isPresent()) {
            try {
                returnValue = Integer.valueOf(valueString.get());
            } catch (NumberFormatException nfe) {
                log.error("Error parsing system config value {} given key {}", valueString.get(), key);
            }
        }

        return returnValue;
    }

    @Override
    public String getValueForKey(final String key) {
        return cacheService.getConfig(key);
    }

}
