package com.pemc.crss.metering.resource.template.impl;

import com.pemc.crss.metering.resource.template.ResourceTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ResourceTemplateImpl implements ResourceTemplate {

    private final RestTemplate restTemplate;

    public <T> T get(String path, Class<T> type) {
        return get(path, type, true);
    }

    public <T> T get(String path, Class<T> type, boolean useServerPath) {
        if (useServerPath) {
            path = getRequestUrl(path);
        }
        return restTemplate.exchange(path, GET, getHttpEntity(null), type).getBody();
    }

    public <T> T post(String path, Class<T> type, Object requestBody) {
        return restTemplate.exchange(getRequestUrl(path), POST, getHttpEntity(requestBody), type).getBody();
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return requestAttributes.getRequest();
    }

    private HttpEntity getHttpEntity(Object requestBody) {
        HttpServletRequest request = getRequest();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        if (request != null) {
            headers.set(AUTHORIZATION, request.getHeader(AUTHORIZATION));
        }
        return new HttpEntity<>(requestBody, headers);
    }

    private String getRequestUrl(String path) {
        return "http://app:8080" + path;
    }

}
