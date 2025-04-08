package org.voyager.config.nominatim;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Setter @Getter
public class NominatimConfig {
    // https://nominatim.openstreetmap.org/search?format=geojson&layer=address&addressdetails=1&q=itami&limit=10&accept-language=en-US
    //    Documentation: https://nominatim.org/release-docs/develop/api/Search/
    String protocol = "https";
    String host = "nominatim.openstreetmap.org";
    String searchPath = "/search";
    String sourceName = "Nominatim";
    String sourceLink = "https://nominatim.org/";
    private static final String QUERY_PARAM = "q";
    private static final String LIMIT_PARAM = "limit";
    private static final String FORMAT_PARAM = "format";
    private static final String FORMAT_VALUE = "geojson";
    private static final String ADDRESS_PARAM = "addressdetails";
    private static final int ADDRESS_VALUE = 1;
    private static final String LAYER_PARAM = "layer";
    private static final String LAYER_VALUE = "address";
    private static final String LANGUAGE_PARAM = "accept-language";
    private static final String LANGUAGE_VALUE = "en-US";
    private UriComponentsBuilder searchUriBuilder;

    @PostConstruct
    public void loadEndpoint() {
        searchUriBuilder = UriComponentsBuilder
                .newInstance().scheme(protocol)
                .host(host)
                .path(searchPath)
                .queryParam(FORMAT_PARAM,FORMAT_VALUE)
                .queryParam(LAYER_PARAM,LAYER_VALUE)
                .queryParam(LANGUAGE_PARAM,LANGUAGE_VALUE)
                .queryParam(ADDRESS_PARAM,ADDRESS_VALUE);
    }

    public String buildSearchURL(String query, int limit) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        searchUriBuilder.replaceQueryParam(QUERY_PARAM,encodedQuery);
        searchUriBuilder.replaceQueryParam(LIMIT_PARAM,limit);
        return searchUriBuilder.toUriString();
    }
}
