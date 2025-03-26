package org.voyager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.voyager.model.response.geonames.FeatureClass;

@Component
@ConfigurationProperties(prefix = "geonames")
@Setter @Getter
public class GeoNameConfig {
    String host;
    String protocol;
    String searchPath;
    String getPath;
    int maxRows;
    String username;
    Boolean isNameRequired;
    FeatureClass featureClass;

    private static final String GEONAME_KEY = "geonameId";
    private static final String USERNAME_KEY = "username";
    private static final String QUERY_KEY = "q";
    private static final String NAME_REQUIRED_KEY = "isNameRequired";
    private static final String MAX_ROWS_KEY = "maxRows";
    private static final String START_ROW_KEY = "startRow";
    private static final String FEATURE_CLASS_KEY = "featureClass";

    private UriComponentsBuilder searchUriBuilder;
    private UriComponentsBuilder getUriBuilder;

    public String buildSearchURL(String encodedQuery, Integer startRow) {
        if (searchUriBuilder == null) {
            searchUriBuilder = UriComponentsBuilder
                    .newInstance().scheme(protocol)
                    .host(host)
                    .path(searchPath);
        }
        return searchUriBuilder
                .queryParam(QUERY_KEY,encodedQuery)
                .queryParam(START_ROW_KEY,startRow)
                .queryParam(NAME_REQUIRED_KEY,isNameRequired)
                .queryParam(FEATURE_CLASS_KEY,featureClass)
                .queryParam(MAX_ROWS_KEY,maxRows)
                .queryParam(USERNAME_KEY,username)
                .toUriString();
    }

    public String buildGetURL(Long geoNameId) {
        if (getUriBuilder == null) {
            getUriBuilder = UriComponentsBuilder
                    .newInstance().scheme(protocol)
                    .host(host)
                    .path(getPath);
        }
        return getUriBuilder
                .queryParam(GEONAME_KEY,geoNameId)
                .queryParam(USERNAME_KEY,username)
                .toUriString();
    }
}
