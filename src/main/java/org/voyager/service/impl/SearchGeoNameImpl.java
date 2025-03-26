package org.voyager.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.voyager.config.GeoNameConfig;
import org.voyager.model.response.SearchResponseGeoNames;
import org.voyager.model.response.geonames.GeoName;
import org.voyager.service.SearchLocationService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class SearchGeoNameImpl implements SearchLocationService<SearchResponseGeoNames> {
    // TODO: http call to GeoNames Search API with searchText (pageable)
    // API Documentation: https://www.geonames.org/export/geonames-search.html
    // Best practices for consuming external APIs: https://hackernoon.com/5-best-practices-for-integrating-with-external-apis

    @Autowired
    GeoNameConfig geoNameConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public SearchResponseGeoNames search(String searchText, int startRow) {
        try {
            String encodedSearchText = URLEncoder.encode(searchText, StandardCharsets.UTF_8.toString());
            String requestURL = geoNameConfig.buildSearchURL(encodedSearchText,startRow);
            System.out.println("full request URL: " + requestURL);
            ResponseEntity<SearchResponseGeoNames> searchResponse = restTemplate.getForEntity(requestURL, SearchResponseGeoNames.class);
            SearchResponseGeoNames searchResponseGeoNames = searchResponse.getBody();
            for (GeoName geoName : searchResponseGeoNames.getGeoNames()) {
                String getURL = geoNameConfig.buildGetURL(geoName.getGeonameId());
                ResponseEntity<GeoName> getResponse = restTemplate.getForEntity(getURL,GeoName.class);
                GeoName fullGeoName = getResponse.getBody();
                geoName.setBoundingBox(fullGeoName.getBoundingBox());
                geoName.setContinentCode(fullGeoName.getContinentCode());
                geoName.setTimezone(fullGeoName.getTimezone());
            }
            return searchResponseGeoNames;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
