package org.voyager.api.auth;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;
import org.voyager.api.config.AuthConfig;
import org.voyager.commons.constants.EnvVariableNames;
import org.voyager.commons.constants.Headers;
import org.voyager.api.error.MessageConstants;
import org.voyager.commons.utils.Environment;

import java.util.List;

@Service
public class AuthenticationService {
    private static AuthConfig authConfig;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    @Autowired
    public AuthenticationService(AuthConfig authConfig) {
        AuthenticationService.authConfig = authConfig;
    }

    @PostConstruct
    public void validate() {
        new Environment().validateEnvVars(List.of(EnvVariableNames.VOYAGER_API_KEY,
                EnvVariableNames.TESTS_API_KEY,EnvVariableNames.ADMIN_API_KEY));
    }

    public static Authentication getAuthentication(HttpServletRequest request) {
        String apiKey = request.getHeader(Headers.AUTH_TOKEN_HEADER_NAME);
        LOGGER.debug("Authenticating API key: {}", apiKey);
        if (apiKey == null) {
            throw new BadCredentialsException(MessageConstants.buildInvalidApiKey(apiKey));
        }
        List<GrantedAuthority> authorities = getAuthoritiesForApiKey(apiKey);
        return new ApiKeyAuthentication(apiKey, authorities);
    }

    private static List<GrantedAuthority> getAuthoritiesForApiKey(String apiKey) {
        if (authConfig.isAdminKey(apiKey)) {
            return AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER");
        }
        if (authConfig.isTestsKey(apiKey)) {
            return AuthorityUtils.createAuthorityList("ROLE_TEST", "ROLE_USER");
        }
        if (authConfig.isUserKey(apiKey)) {
            return AuthorityUtils.createAuthorityList("ROLE_USER");
        }
        throw new BadCredentialsException(MessageConstants.buildInvalidApiKey(apiKey));
    }
}