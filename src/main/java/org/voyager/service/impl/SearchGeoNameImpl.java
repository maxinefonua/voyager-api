package org.voyager.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.config.GeoNameConfig;
import org.voyager.error.MessageConstants;
import org.voyager.model.response.SearchResponseGeoNames;
import org.voyager.model.response.VoyagerListResponse;
import org.voyager.model.response.geonames.GeoName;
import org.voyager.service.SearchLocationService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Service
public class SearchGeoNameImpl implements SearchLocationService<GeoName> {
    // TODO: http call to GeoNames Search API with searchText (pageable)
    // API Documentation: https://www.geonames.org/export/geonames-search.html
    // Best practices for consuming external APIs: https://hackernoon.com/5-best-practices-for-integrating-with-external-apis

    @Autowired
    GeoNameConfig geoNameConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public VoyagerListResponse<GeoName> search(String searchText, int startRow) {
        String encodedSearchText = URLEncoder.encode(searchText, StandardCharsets.UTF_8);
        String requestURL = geoNameConfig.buildSearchURL(encodedSearchText, startRow);
        // TODO: Logger
        System.out.println("full request URL: " + requestURL);
        ResponseEntity<SearchResponseGeoNames> searchResponse = restTemplate.getForEntity(requestURL, SearchResponseGeoNames.class);
        SearchResponseGeoNames searchResponseGeoNames = searchResponse.getBody();
        if (searchResponseGeoNames == null) {
            return VoyagerListResponse.<GeoName>builder().results(Collections.emptyList()).resultCount(0).build();
        } else {
            searchResponse.getBody().getGeoNames().forEach(geoName -> {
                String getURL = geoNameConfig.buildGetURL(geoName.getGeonameId());
                ResponseEntity<GeoName> getResponse = restTemplate.getForEntity(getURL, GeoName.class);
                GeoName fullGeoName = getResponse.getBody();
                if (fullGeoName == null) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            MessageConstants.buildGetGeonameErrorMessage(geoName.getName(),geoName.getGeonameId()));
                } else {
                    geoName.setBoundingBox(fullGeoName.getBoundingBox());
                    geoName.setContinentCode(fullGeoName.getContinentCode());
                    geoName.setTimezone(fullGeoName.getTimezone());
                }
            });
            return VoyagerListResponse.<GeoName>builder().resultCount(searchResponse.getBody().getTotalResultsCount()).results(searchResponseGeoNames.getGeoNames()).build();
        }
    }
}
