package org.voyager.service.impl.search;

import org.apache.commons.lang3.StringUtils;
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
import org.voyager.model.location.Location;
import org.voyager.model.location.Source;
import org.voyager.model.location.Status;
import org.voyager.model.result.LookupAttribution;
import org.voyager.model.result.ResultSearch;
import org.voyager.model.external.geonames.SearchResponseGeoNames;
import org.voyager.model.response.SearchResult;
import org.voyager.model.external.geonames.GeoName;
import org.voyager.model.result.ResultSearchFull;
import org.voyager.service.LocationService;
import org.voyager.service.SearchLocationService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
        LOGGER.debug(String.format("fetching uncached search q = '%s', skipRowCount = %d",query,startRow));
        SearchResponseGeoNames searchResponseGeoNames = getSearchResponse(query,startRow,limit);
        List<ResultSearch> resultList = new ArrayList<>();
        if (searchResponseGeoNames.getGeoNames().isEmpty()) {
            searchResponseGeoNames = getSearchResponseAdmin(query,startRow,limit);
        }
        for (GeoName geoName : searchResponseGeoNames.getGeoNames()) {
            resultList.add(ResultSearch.builder()
                    .source(Source.valueOf(geoNameConfig.getSourceName().toUpperCase()))
                    .sourceId(String.valueOf(geoName.getGeonameId()))
                    .name(geoName.getName()).subdivision(geoName.getAdminName1())
                    .countryCode(geoName.getCountryCode())
                    .countryName(geoName.getCountryName()).type(geoName.getFclName())
                    .longitude(geoName.getLng().doubleValue())
                    .latitude(geoName.getLat().doubleValue()).build());
        }
        return SearchResult.<ResultSearch>builder().resultCount(searchResponseGeoNames.getTotalResultsCount()).results(resultList).build();
    }

    @Override
    public ResultSearchFull fetchResultSearch(String sourceId) {
        LOGGER.debug(String.format("fetching uncached buildLocation sourceId = '%s'",sourceId));
        GeoName geoName = getGeoNameFull(sourceId);
        ResultSearch resultSearch = ResultSearch.builder()
                .source(Source.valueOf(geoNameConfig.getSourceName().toUpperCase()))
                .sourceId(String.valueOf(geoName.getGeonameId()))
                .name(geoName.getName()).subdivision(geoName.getAdminName1())
                .countryCode(geoName.getCountryCode())
                .countryName(geoName.getCountryName()).type(geoName.getFclName())
                .longitude(geoName.getLng().doubleValue())
                .latitude(geoName.getLat().doubleValue()).build();
        ResultSearchFull resultSearchFull = ResultSearchFull.builder().resultSearch(resultSearch).build();
        if (geoName.getBoundingBox() != null) {
            resultSearchFull.setBbox(new Double[]{
                    geoName.getBoundingBox().getWest().doubleValue(),
                    geoName.getBoundingBox().getSouth().doubleValue(),
                    geoName.getBoundingBox().getEast().doubleValue(),
                    geoName.getBoundingBox().getNorth().doubleValue()
            });
        }
        if (geoName.getTimezone() != null && geoName.getTimezone().getZoneId() != null)
            resultSearchFull.setZoneId(geoName.getTimezone().getZoneId());
        return resultSearchFull;
    }

    private SearchResponseGeoNames getSearchResponse(String query, int startRow, int limit) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String requestURL = geoNameConfig.buildSearchURL(encodedQuery, startRow,limit);
        ResponseEntity<SearchResponseGeoNames> searchResponse = restTemplate.getForEntity(requestURL, SearchResponseGeoNames.class);
        ExternalExceptions.validateExternalResponse(searchResponse,requestURL);
        return searchResponse.getBody();
    }

    private SearchResponseGeoNames getSearchResponseAdmin(String query, int startRow, int limit) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String requestURL = geoNameConfig.buildSearchAdminURL(encodedQuery, startRow,limit);
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

    private GeoName getGeoNameFull(String sourceId) {
        String getURL = geoNameConfig.buildGetURL(Long.valueOf(sourceId));
        LOGGER.info(String.format("Fetching geoname details from get URL: %s",getURL));
        ResponseEntity<GeoName> getResponse = restTemplate.getForEntity(getURL, GeoName.class);
        ExternalExceptions.validateExternalResponse(getResponse,getURL);
        GeoName fullGeoName = getResponse.getBody();
        assert fullGeoName != null;
        return fullGeoName;
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
                .longitude(geoName.getLng().doubleValue())
                .latitude(geoName.getLat().doubleValue()).build());
    }
}
