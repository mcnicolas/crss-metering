package com.pemc.crss.commons.logger;

import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MDCLoggingSupport implements Filter {

    public static final String USERNAME = "username";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // DO NOTHING
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();

            if (principal != null) {
                Object username = "system";

                if (principal instanceof UserDetails) {
                    UserDetails tmp = (UserDetails) principal;
                    username = tmp.getUsername();
                } else if (principal instanceof HashMap) {
                    Map map = (Map) principal;
                    username = map.get("name");
                }

                MDC.put(USERNAME, String.valueOf(username));
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
        MDC.remove(USERNAME);
    }

    @Override
    public void destroy() {
        // DO NOTHING
    }
}
