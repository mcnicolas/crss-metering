package com.pemc.crss.metering.resource.template;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.context.request.RequestContextHolder.getRequestAttributes;

@Component
public class ResourceTemplate {

    private static final String X_FORWARDED_PROTO = "x-forwarded-proto";
    private static final String X_FORWARDED_HOST = "x-forwarded-host";

    private final RestTemplate restTemplate;

    @Autowired
    public ResourceTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public <T> T get(String path, Class<T> type) {
        return restTemplate.exchange(getRequestUrl(path), GET, getHttpEntity(null), type).getBody();
    }

    public <T> T post(String path, Class<T> type, MultiValueMap<String, Object> body) {
        return restTemplate.exchange(getRequestUrl(path), POST, getHttpEntity(body), type).getBody();
    }

    private HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) getRequestAttributes()).getRequest();
    }

    private HttpEntity getHttpEntity(MultiValueMap<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(ACCEPT, APPLICATION_JSON_VALUE);
        headers.set(CONTENT_TYPE, APPLICATION_JSON_VALUE);
        headers.set(AUTHORIZATION, getRequest().getHeader(AUTHORIZATION));
        return new HttpEntity<>(body, headers);
    }

    private String getRequestUrl(String path) {
        return getRequest().getHeader(X_FORWARDED_PROTO) + "://"
                + getRequest().getHeader(X_FORWARDED_HOST) + path;
    }

}
