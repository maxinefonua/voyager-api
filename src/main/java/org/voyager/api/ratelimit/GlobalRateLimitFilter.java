package org.voyager.api.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.voyager.api.auth.ApiKeyAuthentication;
import org.voyager.commons.constants.Path;

import java.io.IOException;
import java.io.PrintWriter;

import static org.voyager.api.auth.AuthenticationFilter.PUBLIC_LIMITED_PREFIXES;

@Component
public class GlobalRateLimitFilter extends GenericFilterBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalRateLimitFilter.class);

    @Autowired
    GlobalRateLimitService globalRateLimitService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String path = request.getRequestURI();
        String method = request.getMethod();
        if (isGlobalRateLimitedPrefix(path, method)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAuthenticated = isUserAuthenticated(authentication);

            if (!isAuthenticated) {
                if (!globalRateLimitService.tryConsumeForPublicUser()) {
                    int remaining = globalRateLimitService.getRemainingTokens();
                    LOGGER.warn("Global public rate limit exceeded. Remaining: {}", remaining);
                    sendGlobalRateLimitExceededResponse(response);
                    return;
                }
                // Add global rate limit headers for public users
                addGlobalRateLimitHeaders(response);
            } else {
                LOGGER.debug("Authenticated user - bypassing global rate limit");
                // Authenticated users get unlimited access
                response.setHeader("X-RateLimit-Status", "unlimited");
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean isUserAuthenticated(Authentication authentication) {
        return authentication != null &&
                authentication.isAuthenticated() &&
                authentication instanceof ApiKeyAuthentication;
    }

    private boolean isGlobalRateLimitedPrefix(String path, String method) {
        if (!method.equalsIgnoreCase("GET")) {
            return false;
        }
        for (String prefix : PUBLIC_LIMITED_PREFIXES) {
            if (path.startsWith(prefix)) return true;
        }
        return  false;
    }

    private void sendGlobalRateLimitExceededResponse(HttpServletResponse response)
            throws IOException {

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("X-RateLimit-Reset", "daily");

        PrintWriter writer = response.getWriter();
        writer.println(String.format(
                "{\"error\": \"Public request rate limit exceeded\", " +
                        "\"message\": \"Daily global limit of %d public requests reached. " +
                        "Further requests require an authenticated API key.\", " +
                        "\"remaining\": 0, " +
                        "\"limit\": %d}",
                globalRateLimitService.getGlobalDailyLimit(),
                globalRateLimitService.getGlobalDailyLimit()
        ));
        writer.flush();
    }

    private void addGlobalRateLimitHeaders(HttpServletResponse response) {
        int remaining = globalRateLimitService.getRemainingTokens();
        int limit = globalRateLimitService.getGlobalDailyLimit();
        LOGGER.info("{}/{} remaining requests of global rate limit after public request",remaining,limit);

        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Reset", "daily");
        response.setHeader("X-RateLimit-Scope", "global-public");
        response.setHeader("X-RateLimit-User-Type", "public");
    }
}