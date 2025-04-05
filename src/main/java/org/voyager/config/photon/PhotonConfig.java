package org.voyager.config.photon;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Setter @Getter
public class PhotonConfig {
//    Documentation: https://photon.komoot.io/
    String protocol = "https";
    String host = "photon.komoot.io";
    String searchPath = "/api";
    private static final String QUERY_PARAM = "q";
    private static final String LANG_PARAM = "lang";
    private static final String LIMIT_PARAM = "limit";
    private static final String TAG_PARAM = "osm_tag";
    private static final String PLACE_VALUE = "place";
    private static final String ENGLISH_VALUE = "en";
    private UriComponentsBuilder searchUriBuilder;

    @PostConstruct
    public void loadEndpoint() {
        searchUriBuilder = UriComponentsBuilder
                .newInstance().scheme(protocol)
                .host(host)
                .path(searchPath)
                .queryParam(LANG_PARAM,ENGLISH_VALUE)
                .queryParam(TAG_PARAM,PLACE_VALUE);
    }

    public String buildSearchURL(String query, int limit) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        searchUriBuilder.replaceQueryParam(QUERY_PARAM,encodedQuery);
        searchUriBuilder.replaceQueryParam(LIMIT_PARAM,limit);
        return searchUriBuilder.toUriString();
    }
}
