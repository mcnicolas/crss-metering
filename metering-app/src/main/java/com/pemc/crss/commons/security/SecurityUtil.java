package com.pemc.crss.commons.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.LinkedHashMap;

public class SecurityUtil {
    private static final String ANONYMOUS = "anonymous";
    public static String getCurrentUser(Authentication auth) {
        final Object principal = auth.getPrincipal();
        if (principal == null) {
            return null;
        }
        if (auth instanceof OAuth2Authentication) {
            final LinkedHashMap<String, Object> oAuthPrincipal = (LinkedHashMap) auth.getPrincipal();
            return (String) oAuthPrincipal.get("name");
        }
        return ANONYMOUS;
    }
}