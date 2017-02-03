package com.pemc.crss.commons.web.dto;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class WebDtoUtils {

    private WebDtoUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> wrapDto(List<?> sources, Class<T> targetClass) {
        List<T> result = new ArrayList<T>(sources.size());
        for (Object source : sources) {
            result.add(wrapDto(source, targetClass));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<T> wrapDto(Set<?> sources, Class<T> targetClass) {
        Set<T> result = new LinkedHashSet<T>(sources.size());
        for (Object source : sources) {
            result.add(wrapDto(source, targetClass));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <K, T> Map<K, T> wrapDto(Map<K, ?> sources, Class<T> targetClass) {
        Map<K, T> result = new LinkedHashMap<K, T>(sources.size());
        for (Map.Entry<K, ?> source : sources.entrySet()) {
            result.put(source.getKey(), wrapDto(source.getValue(), targetClass));
        }
        return result;
    }

    public static <T> T wrapDto(Object source, Class<T> targetClass) {
        try {
            Constructor<T> ctr = targetClass.getConstructor(source.getClass());
            return ctr.newInstance(source);

        } catch (Exception e) {
            throw new RuntimeException("Failed to wrap mav dto: source=" + source + "; targetClass=" + targetClass, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> unwrapDto(List<? extends WebDto<T>> sources) {
        List<T> result = new ArrayList<T>(sources.size());
        for (WebDto<T> source : sources) {
            result.add(source.target());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<T> unwrapDto(Set<? extends WebDto<T>> sources) {
        Set<T> result = new LinkedHashSet<T>(sources.size());
        for (WebDto<T> source : sources) {
            result.add(source.target());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <K, T> Map<K, T> unwrapDto(Map<K, ? extends WebDto<T>> sources) {
        Map<K, T> result = new LinkedHashMap<K, T>(sources.size());
        for (Map.Entry<K, ? extends WebDto<T>> source : sources.entrySet()) {
            result.put(source.getKey(), source.getValue().target());
        }
        return result;
    }

    public static <T> T unwrapDto(WebDto<T> source) {
        return source.target();
    }
}

