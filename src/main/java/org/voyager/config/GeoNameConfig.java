package org.voyager.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.voyager.model.response.geonames.FeatureClass;
import org.voyager.utls.ConstantsUtil;
import java.util.List;

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

    @PostConstruct
    public void validate() {
        ConstantsUtil.validateEnvironVars(List.of(ConstantsUtil.GEONAMES_API_USERNAME));
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
    private UriComponentsBuilder getUriBuilder;

    public String buildSearchURL(String encodedQuery, Integer startRow) {

        return searchUriBuilder
                .replaceQueryParam(QUERY_KEY,encodedQuery)
                .replaceQueryParam(START_ROW_KEY,startRow)
                .toUriString();
    }

    public String buildGetURL(Long geoNameId) {
        return getUriBuilder
                .replaceQueryParam(GEONAME_KEY,geoNameId)
                .toUriString();
    }
}
