package org.voyager.auth;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import static org.voyager.utls.ConstantsUtil.*;
import static org.voyager.utls.MessageUtil.EMPTY_ENV_VAR;
import static org.voyager.utls.MessageUtil.INVALID_API_KEY;

@Component
@ConfigurationProperties(prefix = "auth")
public class AuthenticationService {

    private static String AUTH_TOKEN;

    @Value("${VOYAGER_API_KEY}")
    public void setAuthToken(String authToken) {
        AUTH_TOKEN = authToken;
    }

    @PostConstruct
    public void validate() {
        if (invalidEnvironmentVar(VOYAGER_API_KEY,AUTH_TOKEN)) {
            throw new IllegalArgumentException(String.format(EMPTY_ENV_VAR,VOYAGER_API_KEY));
        }
    }

    public static Authentication getAuthentication(HttpServletRequest request) {
        String apiKey = request.getHeader(AUTH_TOKEN_HEADER_NAME);
        if (apiKey == null || !apiKey.equals(AUTH_TOKEN)) {
            throw new BadCredentialsException(INVALID_API_KEY);
        }

        return new ApiKeyAuthentication(apiKey, AuthorityUtils.NO_AUTHORITIES);
    }
}