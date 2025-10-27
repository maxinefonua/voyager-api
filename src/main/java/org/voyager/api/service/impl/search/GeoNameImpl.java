package org.voyager.api.service.impl.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.voyager.api.config.VoyagerGeoNamesConfig;
import org.voyager.api.error.ExternalExceptions;
import org.voyager.commons.model.geoname.fields.FeatureClass;
import org.voyager.commons.model.geoname.query.GeoSearchQuery;
import org.voyager.commons.model.location.Source;
import org.voyager.commons.model.location.Status;
import org.voyager.commons.model.result.LookupAttribution;
import org.voyager.commons.model.result.ResultSearch;
import org.voyager.api.model.external.geonames.SearchResponseGeoNames;
import org.voyager.commons.model.response.SearchResult;
import org.voyager.api.model.external.geonames.GeoName;
import org.voyager.commons.model.result.ResultSearchFull;
import org.voyager.api.service.LocationService;
import org.voyager.api.service.SearchLocationService;
import org.voyager.api.service.external.GeoNameService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service @Primary
public class GeoNameImpl implements SearchLocationService {
    @Autowired
    VoyagerGeoNamesConfig voyagerGeoNamesConfig;

    @Autowired
    LocationService locationService;

    @Autowired
    GeoNameService geoNameService;

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
                    .source(Source.valueOf(voyagerGeoNamesConfig.getSourceName().toUpperCase()))
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
                .source(Source.valueOf(voyagerGeoNamesConfig.getSourceName().toUpperCase()))
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
        GeoSearchQuery geoSearchQuery = GeoSearchQuery.builder().query(query)
                .startRow(startRow).featureClass(FeatureClass.P)
                .isNameRequired(true).maxRows(limit).build();
        ResponseEntity<SearchResponseGeoNames> searchResponse = geoNameService.search(geoSearchQuery,
                SearchResponseGeoNames.class);
        ExternalExceptions.validateExternalResponse(searchResponse);
        return searchResponse.getBody();
    }

    private SearchResponseGeoNames getSearchResponseAdmin(String query, int startRow, int limit) {
        GeoSearchQuery geoSearchQuery = GeoSearchQuery.builder().query(query)
                .startRow(startRow).featureClass(FeatureClass.A)
                .isNameRequired(true).maxRows(limit).build();
        ResponseEntity<SearchResponseGeoNames> searchResponse = geoNameService.search(geoSearchQuery, SearchResponseGeoNames.class);
        ExternalExceptions.validateExternalResponse(searchResponse);
        return searchResponse.getBody();
    }

     @Override
     public LookupAttribution attribution() {
        return LookupAttribution.builder().name(voyagerGeoNamesConfig.getSourceName()).link(voyagerGeoNamesConfig.getSourceLink()).build();
     }

    @Override
    public List<ResultSearch> augmentLocationStatus(List<ResultSearch> cachedResults) {
        List<String> sourceIds = cachedResults.stream().map(ResultSearch::getSourceId).toList();
        Map<String,Status> locationIdToStatusDB = locationService.getSourceIdsToStatusMap(Source.valueOf(voyagerGeoNamesConfig.getSourceName().toUpperCase()),sourceIds);
        cachedResults.forEach(resultSearch -> resultSearch.setStatus(locationIdToStatusDB.getOrDefault(resultSearch.getSourceId(),Status.NEW)));
        return cachedResults;
    }

    private GeoName getGeoNameFull(String sourceId) {
        ResponseEntity<GeoName> getResponse = geoNameService.getFull(Long.valueOf(sourceId), GeoName.class);
        ExternalExceptions.validateExternalResponse(getResponse);
        GeoName fullGeoName = getResponse.getBody();
        assert fullGeoName != null;
        return fullGeoName;
    }

     private Stream<ResultSearch> buildResultSearch(GeoName geoName, Status status){
        ResponseEntity<GeoName> getResponse = geoNameService.getFull(geoName.getGeonameId(), GeoName.class);
        ExternalExceptions.validateExternalResponse(getResponse);
        GeoName fullGeoName = getResponse.getBody();
        assert fullGeoName != null;
        if (fullGeoName.getBoundingBox() == null) return Stream.empty();
        return Stream.of(ResultSearch.builder()
                .status(status)
                .source(Source.valueOf(voyagerGeoNamesConfig.getSourceName().toUpperCase()))
                .sourceId(String.valueOf(geoName.getGeonameId()))
                .name(geoName.getName()).subdivision(geoName.getAdminName1())
                .countryCode(fullGeoName.getCountryCode().toUpperCase())
                .countryName(geoName.getCountryName()).type(geoName.getFclName())
                .longitude(geoName.getLng().doubleValue())
                .latitude(geoName.getLat().doubleValue()).build());
    }
}
