package org.voyager.api.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import org.voyager.api.auth.service.AuthenticationService;
import org.voyager.commons.constants.Path;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

public class AuthenticationFilter extends GenericFilterBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);

    public static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/",
            "/index",
            "/index.html",
            "/error",
            "/test",
            "/error.html",
            "/actuator",
            "/actuator/health",
            "/swagger-ui",
            "/swagger-ui.html",
            "/swagger-ui/",
            "/v3/api-docs",
            "/v3/api-docs/swagger-config",
            "/favicon.ico"
    );

    public static final Set<String> PUBLIC_LIMITED_PREFIXES = Set.of(
            Path.COUNTRIES,
            Path.AIRPORTS,
            Path.AIRLINES,
            Path.FLIGHTS,
            Path.IATA,
            Path.PATH
    );

    private static final Set<String> PUBLIC_PATH_PREFIXES = Set.of(
            "/actuator/",
            "/swagger-ui/",
            "/v3/api-docs/",
            "/.well-known/",
            "/css/",
            "/js/",
            "/images/"
    );

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String path = request.getRequestURI();
        String method = request.getMethod();
        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        boolean isGlobalRateLimited = isGlobalRateLimitedPrefix(path,method);

        try {
            Authentication authentication = AuthenticationService.getAuthentication(request);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            if (isGlobalRateLimited) {
                LOGGER.debug("Authentication failed for countries endpoint, allowing public access: {}", path);
                SecurityContextHolder.clearContext();
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
                httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
                PrintWriter writer = httpServletResponse.getWriter();
                writer.println(e.getMessage());
                writer.flush();
                writer.close();
        }
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

    private boolean isPublicEndpoint(String path) {
        if (PUBLIC_ENDPOINTS.contains(path)) {
            return true;
        }

        for (String prefix : PUBLIC_PATH_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }

        // Match file extensions
        if (path.matches(".*\\.(css|js|png|jpg|jpeg|gif|ico|svg)$")) {
            return true;
        }
        return false;
    }
}