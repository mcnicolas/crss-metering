package com.pemc.crss.metering.service;

import com.pemc.crss.metering.dto.UserDetail;
import com.pemc.crss.metering.dto.mq.ParticipantUserDetail;
import com.pemc.crss.metering.resource.template.ResourceTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class CacheService {

    private final ResourceTemplate resourceTemplate;

    @Autowired
    public CacheService(ResourceTemplate resourceTemplate) {
        this.resourceTemplate = resourceTemplate;
    }

    @Cacheable("msp")
    public ParticipantUserDetail getParticipantUserDetail(String shortName) {
        return resourceTemplate.get("/reg/participants/mspUserDetail/" + shortName, ParticipantUserDetail.class);
    }

    @Cacheable("user")
    public UserDetail getUserDetail(String username) {
        if (SecurityContextHolder.getContext() == null || SecurityContextHolder.getContext().getAuthentication() == null) {
            return null;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth.getPrincipal() != null && auth.getPrincipal() instanceof LinkedHashMap) {
            LinkedHashMap<String, Object> principal = (LinkedHashMap) auth.getPrincipal();

            UserDetail userDetail = new UserDetail();
            try {
                BeanUtils.populate(userDetail, (Map<String, Object>)principal.get("principal"));

                return userDetail;
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error(e.getMessage(), e);
            }
        }

        return null;
    }

    @CachePut(value = "config", key = "#key")
    public String updateConfig(String key, String value) {
        return value;
    }

    @Cacheable("config")
    public String getConfig(String key) {
        return "";
    }

}
