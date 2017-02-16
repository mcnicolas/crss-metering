package com.pemc.crss.commons.cache.service;

public interface CacheConfigService {

    long getLongValueForKey(String key, long defaultValue);

    String getStringValueForKey(String key, String defaultValue);

    String getValueForKey(String key);

    Integer getIntegerValueForKey(String key, Integer defaultValue);

}
