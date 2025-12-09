package org.voyager.api.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Component
@ConfigurationProperties(prefix = "auth")
public class AuthConfig {
    private String adminApiKey;
    private String testsApiKey;
    private String userApiKey;

    public boolean isAdminKey(String token) {
        return adminApiKey.equals(token);
    }
    public boolean isUserKey(String token) {
        return userApiKey.equals(token);
    }
    public boolean isTestsKey(String token) {
        return testsApiKey.equals(token);
    }
}
