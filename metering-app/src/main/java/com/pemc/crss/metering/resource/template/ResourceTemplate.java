package com.pemc.crss.metering.resource.template;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
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

    public <T> T post(String path, Class<T> type, Object requestBody) {
        return restTemplate.exchange(getRequestUrl(path), POST, getHttpEntity(requestBody), type).getBody();
    }

    private HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) getRequestAttributes()).getRequest();
    }

    private HttpEntity getHttpEntity(Object requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.set(AUTHORIZATION, getRequest().getHeader(AUTHORIZATION));
        return new HttpEntity<>(requestBody, headers);
    }

    private String getRequestUrl(String path) {
        return getRequest().getHeader(X_FORWARDED_PROTO) + "://"
                + getRequest().getHeader(X_FORWARDED_HOST) + path;
    }

}
