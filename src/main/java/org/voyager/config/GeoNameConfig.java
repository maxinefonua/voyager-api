package org.voyager.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.voyager.model.external.geonames.FeatureClass;
import org.voyager.utils.ConstantsUtils;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "geonames")
@Setter @Getter
public class GeoNameConfig {
    String host;
    String protocol;
    String searchPath;
    String getPath;
    String sourceName;
    String sourceLink;
    int maxRows;
    String username;
    Boolean isNameRequired;
    FeatureClass featureClass;
    FeatureClass featureClassAdmin;

    @PostConstruct
    public void validate() {
        ConstantsUtils.validateEnvironVars(List.of(ConstantsUtils.GEONAMES_API_USERNAME));
        searchUriBuilder = UriComponentsBuilder
                .newInstance().scheme(protocol)
                .host(host)
                .path(searchPath)
                .queryParam(NAME_REQUIRED_KEY,isNameRequired)
                .queryParam(FEATURE_CLASS_KEY,featureClass)
                .queryParam(MAX_ROWS_KEY,maxRows)
                .queryParam(USERNAME_KEY,username);

        getUriBuilder = UriComponentsBuilder
                .newInstance().scheme(protocol)
                .host(host)
                .path(getPath)
                .queryParam(USERNAME_KEY,username);
    }

    private static final String GEONAME_KEY = "geonameId";
    private static final String USERNAME_KEY = "username";
    private static final String QUERY_KEY = "q";
    private static final String NAME_REQUIRED_KEY = "isNameRequired";
    private static final String MAX_ROWS_KEY = "maxRows";
    private static final String START_ROW_KEY = "startRow";
    private static final String FEATURE_CLASS_KEY = "featureClass";

    private UriComponentsBuilder searchUriBuilder;
    private UriComponentsBuilder searchAdminUriBuilder;
    private UriComponentsBuilder getUriBuilder;

    public String buildSearchURL(String encodedQuery, Integer startRow, int limit) {
        return searchUriBuilder
                .replaceQueryParam(QUERY_KEY,encodedQuery)
                .replaceQueryParam(START_ROW_KEY,startRow)
                .replaceQueryParam(FEATURE_CLASS_KEY,featureClass)
                .replaceQueryParam(MAX_ROWS_KEY,limit)
                .toUriString();
    }

    public String buildSearchAdminURL(String encodedQuery, Integer startRow, int limit) {
        return searchUriBuilder
                .replaceQueryParam(QUERY_KEY,encodedQuery)
                .replaceQueryParam(START_ROW_KEY,startRow)
                .replaceQueryParam(FEATURE_CLASS_KEY,featureClassAdmin)
                .replaceQueryParam(MAX_ROWS_KEY,limit)
                .toUriString();
    }

    public String buildGetURL(Long geoNameId) {
        return getUriBuilder
                .replaceQueryParam(GEONAME_KEY,geoNameId)
                .toUriString();
    }
}
