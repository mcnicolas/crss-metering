package com.pemc.crss.metering.utils;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.LinkedHashMap;

public class SecurityUtils {
    private static final String USERNAME = "username";
    private static final String ID = "id";
    private static final String DEPARTMENT = "department";
//    private static final ObjectMapper MAPPER = new ObjectMapper().configure(MapperFeature.USE_GETTERS_AS_SETTERS, false);
    private static final String USER_AUTHENTICATION = "userAuthentication";
    private static final String PRINCIPAL = "principal";
//    private static final String DEFAULT_NO_PERMISSION_MESSAGE = "User has no permission.";
    private static final Logger LOG = LoggerFactory
            .getLogger(SecurityUtils.class);

    private SecurityUtils() {
    }

    public static LinkedHashMap<String, Object> getPrincipalMap() {
        // Various initial checking
        if (SecurityContextHolder.getContext() == null) {
            LOG.warn("Unable to find a valid security context: context=null", SecurityContextHolder.getContext());
            return null;
        }

        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            LOG.warn("Unable to find a valid authentication object: auth=null");
            return null;
        }

        final Object principal = auth.getPrincipal();
        if (principal == null) {
            LOG.warn("Unable to find a valid principal object: principal=null");
            return null;
        }

        if (auth instanceof OAuth2Authentication) {
            final OAuth2Authentication oauth = (OAuth2Authentication) auth;

            if (principal instanceof LinkedHashMap) {
                final LinkedHashMap<String, Object> oAuthPrincipal = (LinkedHashMap) auth.getPrincipal();
                if (oAuthPrincipal.get(USER_AUTHENTICATION) != null
                        && oAuthPrincipal.get(USER_AUTHENTICATION) instanceof LinkedHashMap) {
                    final LinkedHashMap<String, Object> userAuthentication =
                            (LinkedHashMap) oAuthPrincipal.get(USER_AUTHENTICATION);
                    return userAuthentication.get(PRINCIPAL) != null
                            && (userAuthentication.get(PRINCIPAL) instanceof LinkedHashMap)
                            ? (LinkedHashMap<String, Object>) userAuthentication.get(PRINCIPAL) : null;
                } else {
                    LOG.warn("No user authentication property from oauth object: oauth={}", oauth);
                }


            }
        }

        return null;
    }

    private static boolean checkContainsKey(LinkedHashMap<String, Object> principal, String key) {
        return MapUtils.isNotEmpty(principal) && principal.containsKey(key);
    }

    public static String getUsername() {
        final LinkedHashMap<String, Object> principal = getPrincipalMap();
        return checkContainsKey(principal, USERNAME)
                ? (String) principal.get(USERNAME) : null;
    }

    public static Integer getUserId() {
        final LinkedHashMap<String, Object> principal = getPrincipalMap();
        return checkContainsKey(principal, ID) ? (Integer) principal.get(ID) : null;
    }

    public static String getDepartment() {
        final LinkedHashMap<String, Object> principal = getPrincipalMap();
        return checkContainsKey(principal, DEPARTMENT)
                ? (String) principal.get(DEPARTMENT) : null;
    }

//
//    public static Set<String> getUserRoles() {
//        final CRSSPrincipal principal = getPrincipal();
//        return principal == null ? Sets.newHashSet() : principal.getStringRoles();
//    }
//
//    public static String getUserDepartment() {
//        final CRSSPrincipal principal = getPrincipal();
//        return principal == null ? null : principal.getDepartment();
//    }
//
//    public static boolean isDepartmentUser(final String deptCode) {
//
//        if (StringUtils.isBlank(deptCode)) {
//            return false;
//        }
//
//        final CRSSPrincipal principal = getPrincipal();
//
//        return principal != null
//                && StringUtils.isNotBlank(principal.getDepartment()) && principal.getDepartment().equals(deptCode);
//
//    }
//
//    public static boolean hasAuthorityAny(String privilege, String... otherPrivileges) {
//        Set<String> privilegeToCheck = new HashSet<>(Lists.asList(privilege, otherPrivileges));
//        final CRSSPrincipal principal = getPrincipal();
//        if (principal == null) {
//            return false;
//        }
//
//        for (String p : privilegeToCheck) {
//            if (principal.getPrivileges().contains(p)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static void checkAuthority(String privilege, String... otherPrivileges) {
//        checkAuthorityWithMessage(null, privilege, otherPrivileges);
//    }

//    public static void checkAuthorityWithMessage(String message, String privilege, String... otherPrivileges) {
//        if (!hasAuthorityAny(privilege, otherPrivileges)) {
//            throw new AccessDeniedException(message == null ? DEFAULT_NO_PERMISSION_MESSAGE : message);
//        }
//    }
}
