package org.voyager.auth;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;
import org.voyager.config.AuthConfig;
import org.voyager.utils.ConstantsUtils;
import org.voyager.utils.MessageUtils;

import java.util.List;

@Service
public class AuthenticationService {
    private static AuthConfig authConfig;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    @Autowired
    public AuthenticationService(AuthConfig authConfig) {
        this.authConfig = authConfig;
    }

    @PostConstruct
    public void validate() {
        ConstantsUtils.validateEnvironVars(List.of(ConstantsUtils.VOYAGER_API_KEY,"TESTS_API_KEY"));
    }

    public static Authentication getAuthentication(HttpServletRequest request) {
        String apiKey = request.getHeader(ConstantsUtils.AUTH_TOKEN_HEADER_NAME);
        LOGGER.debug(String.format("getAuthentication with apiKey: '%s'",apiKey));
        if (apiKey == null || !authConfig.isApprovedToken(apiKey)) {
            throw new BadCredentialsException(MessageUtils.getInvalidApiKeyMessage());
        }
        return new ApiKeyAuthentication(apiKey,AuthorityUtils.NO_AUTHORITIES);
    }
}