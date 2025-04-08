package org.voyager.auth;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;
import org.voyager.utils.ConstantsUtils;
import org.voyager.utils.MessageUtils;
import java.util.List;

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
        ConstantsUtils.validateEnvironVars(List.of(ConstantsUtils.VOYAGER_API_KEY));
    }

    public static Authentication getAuthentication(HttpServletRequest request) {
        String apiKey = request.getHeader(ConstantsUtils.AUTH_TOKEN_HEADER_NAME);
        if (apiKey == null || !apiKey.equals(AUTH_TOKEN)) {
            throw new BadCredentialsException(MessageUtils.getInvalidApiKeyMessage());
        }

        return new ApiKeyAuthentication(apiKey,AuthorityUtils.NO_AUTHORITIES);
    }
}