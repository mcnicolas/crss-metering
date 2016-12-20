package com.pemc.crss.metering.resource.template;


import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.context.request.RequestContextHolder.getRequestAttributes;

public abstract class ResourceTemplate {

    private final RestTemplate restTemplate;

    public ResourceTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected <T> T get(String path, Class<T> type) {
        return restTemplate.exchange(getRequestUrl(path), GET, getHttpEntity(), type).getBody();
    }

    private HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) getRequestAttributes()).getRequest();
    }

    private HttpEntity<String> getHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(ACCEPT, APPLICATION_JSON_VALUE);
        headers.set(CONTENT_TYPE, APPLICATION_JSON_VALUE);
        headers.set(AUTHORIZATION, getRequest().getHeader(AUTHORIZATION));
        return new HttpEntity<>("parameters", headers);
    }

    private String getRequestUrl(String path) {
        return getRequest().getHeader(REFERER) + path;
    }

}
