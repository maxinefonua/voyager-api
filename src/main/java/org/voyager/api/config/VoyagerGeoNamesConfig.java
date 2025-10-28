package org.voyager.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "geonames")
@Setter @Getter
public class VoyagerGeoNamesConfig {
    String username;
    String sourceName;
    String sourceLink;
}
