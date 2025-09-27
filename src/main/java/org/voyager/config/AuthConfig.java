package org.voyager.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.voyager.auth.AuthenticationService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "auth")
public class AuthConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthConfig.class);
    private Set<String> approvedTokens;

    public boolean isApprovedToken(String token) {
        LOGGER.debug(String.format("'%s' is %san approved token",token,approvedTokens.contains(token)?"":"NOT "));
        return approvedTokens.contains(token);
    }

    public void setApprovedTokens(List<String> approvedTokens) {
        approvedTokens.forEach(token -> LOGGER.info(String.format("approved token: '%s'",token)));
        this.approvedTokens = new HashSet<>(approvedTokens);
    }
}
