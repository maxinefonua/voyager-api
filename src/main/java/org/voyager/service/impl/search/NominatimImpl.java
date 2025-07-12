package org.voyager.service.impl.search;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.voyager.config.nominatim.NominatimConfig;
import org.voyager.error.ExternalExceptions;
import org.voyager.model.location.Location;
import org.voyager.model.location.Source;
import org.voyager.model.location.Status;
import org.voyager.model.result.LookupAttribution;
import org.voyager.model.result.ResultSearch;
import org.voyager.model.response.SearchResult;
import org.voyager.model.external.nominatim.Address;
import org.voyager.model.external.nominatim.Properties;
import org.voyager.model.external.nominatim.SearchResponseNominatim;
import org.voyager.model.result.ResultSearchFull;
import org.voyager.service.LocationService;
import org.voyager.service.SearchLocationService;

import java.util.List;
import java.util.Map;

@Service
public class NominatimImpl implements SearchLocationService {
    @Autowired
    NominatimConfig nominatimConfig;

    @Autowired
    LocationService locationService;

    private static final RestTemplate restTemplate = new RestTemplate();
    private static final Logger LOGGER = LoggerFactory.getLogger(NominatimImpl.class);

    @Override
    public SearchResult<ResultSearch> search(String query, int startRow, int limit) {
        String requestURL = nominatimConfig.buildSearchURL(query,limit);
        LOGGER.info("full request URL: " + requestURL);
        ResponseEntity<SearchResponseNominatim> searchResponse = restTemplate.getForEntity(requestURL, SearchResponseNominatim.class);
        ExternalExceptions.validateExternalResponse(searchResponse,requestURL);
        assert searchResponse.getBody() != null;
        List<ResultSearch> resultSearchList = searchResponse.getBody().getFeatures().stream()
                .map(feature -> {
                    Properties props = feature.getProperties();
                    Double[] coordinates = feature.getGeometry().getCoordinates();
                    Address address = props.getAddress();
                    String adminNameVal = resolveAdminNameVal(address);
                    String type = resolveType(props);
                    return ResultSearch.builder()
                            .source(Source.valueOf(nominatimConfig.getSourceName().toUpperCase()))
                            .sourceId(String.valueOf(props.getOsmId().longValue()))
                            .name(props.getName()).subdivision(adminNameVal)
                            .countryCode(address.getCountryCode().toUpperCase())
                            .countryName(address.getCountry()).type(type)
                            .longitude(coordinates[0]).build();
                })
                .toList();
        return SearchResult.<ResultSearch>builder().results(resultSearchList).resultCount(resultSearchList.size()).build();
    }

    @Override
    public ResultSearchFull fetchResultSearch(String sourceId) {
        return null;
    }

    @Override
    public LookupAttribution attribution() {
        return LookupAttribution.builder().name(nominatimConfig.getSourceName()).link(nominatimConfig.getSourceLink()).build();
    }

    @Override
    public List<ResultSearch> augmentLocationStatus(List<ResultSearch> cachedResults) {
        List<String> sourceIds = cachedResults.stream().map(ResultSearch::getSourceId).toList();
        Map<String, Status> locationIdToStatusDB = locationService.getSourceIdsToStatusMap(Source.valueOf(nominatimConfig.getSourceName().toUpperCase()),sourceIds);
        cachedResults.forEach(resultSearch -> resultSearch.setStatus(locationIdToStatusDB.getOrDefault(resultSearch.getSourceId(),Status.NEW)));
        return cachedResults;
    }

    private String resolveAdminNameVal(Address address) {
        if (!StringUtils.isEmpty(address.getState())) return address.getState();
        if (!StringUtils.isEmpty(address.getProvince())) return address.getProvince();
        return address.getCounty();
    }

    private String resolveType(Properties props) {
        String type = props.getType();
        if (!StringUtils.isEmpty(type) && !type.equals("administrative")) return type;
        return props.getAddressType();
    }
}
