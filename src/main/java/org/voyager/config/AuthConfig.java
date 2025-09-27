package org.voyager.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "auth")
public class AuthConfig {
    private List<String> approvedTokens;

    public List<String> getApprovedTokens() {
        return approvedTokens;
    }

    public void setApprovedTokens(List<String> approvedTokens) {
        this.approvedTokens = approvedTokens;
    }
}
