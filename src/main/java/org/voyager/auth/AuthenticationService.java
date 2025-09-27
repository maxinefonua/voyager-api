package org.voyager.auth;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
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
        if (apiKey == null || !authConfig.getApprovedTokens().contains(apiKey)) {
            throw new BadCredentialsException(MessageUtils.getInvalidApiKeyMessage());
        }

        return new ApiKeyAuthentication(apiKey,AuthorityUtils.NO_AUTHORITIES);
    }
}