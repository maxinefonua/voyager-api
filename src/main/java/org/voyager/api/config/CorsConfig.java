package org.voyager.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "cors")
@Setter @Getter
public class CorsConfig {
    private List<String> allowedOriginPatterns;
}