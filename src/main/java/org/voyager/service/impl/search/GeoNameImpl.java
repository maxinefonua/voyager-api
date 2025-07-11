package org.voyager.service.impl.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.voyager.config.GeoNameConfig;
import org.voyager.error.ExternalExceptions;
import org.voyager.model.location.Source;
import org.voyager.model.location.Status;
import org.voyager.model.result.LookupAttribution;
import org.voyager.model.result.ResultSearch;
import org.voyager.model.external.geonames.SearchResponseGeoNames;
import org.voyager.model.response.SearchResult;
import org.voyager.model.external.geonames.GeoName;
import org.voyager.service.LocationService;
import org.voyager.service.SearchLocationService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service @Primary
public class GeoNameImpl implements SearchLocationService {
    @Autowired
    GeoNameConfig geoNameConfig;

    @Autowired
    LocationService locationService;

    private static final RestTemplate restTemplate = new RestTemplate();
    private static final Logger LOGGER = LoggerFactory.getLogger(GeoNameImpl.class);

    @Override
    public SearchResult<ResultSearch> search(String query, int startRow, int limit) {
        SearchResponseGeoNames searchResponseGeoNames = getSearchResponse(query,startRow,limit);
        List<ResultSearch> resultList = searchResponseGeoNames.getGeoNames().stream()
                .flatMap(this::buildResultSearch).toList();
          return SearchResult.<ResultSearch>builder().resultCount(searchResponseGeoNames.getTotalResultsCount()).results(resultList).build();
    }

    private SearchResponseGeoNames getSearchResponse(String query, int startRow, int limit) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String requestURL = geoNameConfig.buildSearchURL(encodedQuery, startRow);
        LOGGER.info(String.format("Fetching uncached geoname results from search URL: %s",requestURL));
        ResponseEntity<SearchResponseGeoNames> searchResponse = restTemplate.getForEntity(requestURL, SearchResponseGeoNames.class);
        ExternalExceptions.validateExternalResponse(searchResponse,requestURL);
        return searchResponse.getBody();
    }

     @Override
     public LookupAttribution attribution() {
        return LookupAttribution.builder().name(geoNameConfig.getSourceName()).link(geoNameConfig.getSourceLink()).build();
     }

    @Override
    public List<ResultSearch> augmentLocationStatus(List<ResultSearch> cachedResults) {
        List<String> sourceIds = cachedResults.stream().map(ResultSearch::getSourceId).toList();
        Map<String,Status> locationIdToStatusDB = locationService.getSourceIdsToStatusMap(Source.valueOf(geoNameConfig.getSourceName().toUpperCase()),sourceIds);
        cachedResults.forEach(resultSearch -> resultSearch.setStatus(locationIdToStatusDB.getOrDefault(resultSearch.getSourceId(),Status.NEW)));
        return cachedResults;
    }

    private Stream<ResultSearch> buildResultSearch(GeoName geoName){
        String getURL = geoNameConfig.buildGetURL(geoName.getGeonameId());
        LOGGER.debug(String.format("Fetching geoname details from get URL: %s",getURL));
        ResponseEntity<GeoName> getResponse = restTemplate.getForEntity(getURL, GeoName.class);
        ExternalExceptions.validateExternalResponse(getResponse,getURL);
        GeoName fullGeoName = getResponse.getBody();
        assert fullGeoName != null;
        if (fullGeoName.getBoundingBox() == null) return Stream.empty();
        return Stream.of(ResultSearch.builder()
                .source(Source.valueOf(geoNameConfig.getSourceName().toUpperCase()))
                .sourceId(String.valueOf(geoName.getGeonameId()))
                .name(geoName.getName()).subdivision(geoName.getAdminName1())
                .countryCode(fullGeoName.getCountryCode().toUpperCase())
                .countryName(geoName.getCountryName()).type(geoName.getFclName())
                .bounds(new Double[]{
                        fullGeoName.getBoundingBox().getWest().doubleValue(),
                        fullGeoName.getBoundingBox().getSouth().doubleValue(),
                        fullGeoName.getBoundingBox().getEast().doubleValue(),
                        fullGeoName.getBoundingBox().getNorth().doubleValue()
                }).longitude(geoName.getLng().doubleValue())
                .latitude(geoName.getLat().doubleValue()).build());
    }

     private Stream<ResultSearch> buildResultSearch(GeoName geoName, Status status){
        String getURL = geoNameConfig.buildGetURL(geoName.getGeonameId());
        LOGGER.debug(String.format("Fetching geoname details from get URL: %s",getURL));
        ResponseEntity<GeoName> getResponse = restTemplate.getForEntity(getURL, GeoName.class);
        ExternalExceptions.validateExternalResponse(getResponse,getURL);
        GeoName fullGeoName = getResponse.getBody();
        assert fullGeoName != null;
        if (fullGeoName.getBoundingBox() == null) return Stream.empty();
        return Stream.of(ResultSearch.builder()
                .status(status)
                .source(Source.valueOf(geoNameConfig.getSourceName().toUpperCase()))
                .sourceId(String.valueOf(geoName.getGeonameId()))
                .name(geoName.getName()).subdivision(geoName.getAdminName1())
                .countryCode(fullGeoName.getCountryCode().toUpperCase())
                .countryName(geoName.getCountryName()).type(geoName.getFclName())
                .bounds(new Double[]{
                        fullGeoName.getBoundingBox().getWest().doubleValue(),
                        fullGeoName.getBoundingBox().getSouth().doubleValue(),
                        fullGeoName.getBoundingBox().getEast().doubleValue(),
                        fullGeoName.getBoundingBox().getNorth().doubleValue()
                }).longitude(geoName.getLng().doubleValue())
                .latitude(geoName.getLat().doubleValue()).build());
    }
}
