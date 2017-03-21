package com.pemc.crss.metering.resource.template;


public interface ResourceTemplate {

    <T> T get(String path, Class<T> type);
    <T> T get(String path, Class<T> type, boolean useServerPath);
    <T> T post(String path, Class<T> type, Object requestBody);

}
