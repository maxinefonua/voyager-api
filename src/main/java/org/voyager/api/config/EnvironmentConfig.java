package org.voyager.api.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class EnvironmentConfig {
    @Value("${runtime.environment}")
    @Getter
    private String runtimeEnvironment;

    public boolean isTestEnvironment() {
        return runtimeEnvironment.equalsIgnoreCase("stage")
                || runtimeEnvironment.equalsIgnoreCase("dev");
    }
}
