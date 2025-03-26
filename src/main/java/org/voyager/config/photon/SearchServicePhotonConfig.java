package org.voyager.config.photon;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Setter
@Getter
public class SearchServicePhotonConfig {
//    https://photon.komoot.io/api?q=laie
//    Documentation: https://photon.komoot.io/
    String baseURL = "https://photon.komoot.io";
    String searchPath = "/api?";
    String reversePath ="/reverse?";
    String lonParam = "&lon=";
    String latParam = "&lat=";
    String queryParam = "q=";
    String languageParam = "&lang=en";
    String tagParam = "&osm_tag=";
    String placeFitler = "place";

    public String buildParamsWithSearchQuery(String searchText) {
        StringBuilder paramBuilder = new StringBuilder();
        paramBuilder.append(queryParam);
        paramBuilder.append(searchText);
        paramBuilder.append(tagParam);
        paramBuilder.append(placeFitler);
        paramBuilder.append(languageParam);
        return paramBuilder.toString();
    }
}
