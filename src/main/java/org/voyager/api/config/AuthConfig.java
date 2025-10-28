package org.voyager.api.config;

import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "auth")
public class AuthConfig {
    @Setter
    private String adminApiKey;
    @Setter
    private String testsApiKey;
    @Setter
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
