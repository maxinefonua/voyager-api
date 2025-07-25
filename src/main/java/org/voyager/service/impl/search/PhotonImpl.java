package org.voyager.service.impl.search;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.voyager.config.photon.PhotonConfig;
import org.voyager.error.ExternalExceptions;
import org.voyager.model.location.Location;
import org.voyager.model.location.Source;
import org.voyager.model.location.Status;
import org.voyager.model.result.LookupAttribution;
import org.voyager.model.result.ResultSearch;
import org.voyager.model.response.SearchResult;
import org.voyager.model.external.photon.Properties;
import org.voyager.model.external.photon.Feature;
import org.voyager.model.external.photon.SearchResponsePhoton;
import org.voyager.model.result.ResultSearchFull;
import org.voyager.service.LocationService;
import org.voyager.service.SearchLocationService;

import java.util.List;
import java.util.Map;

@Service
public class PhotonImpl implements SearchLocationService {
    @Autowired
    PhotonConfig photonConfig;

    @Autowired
    LocationService locationService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PhotonImpl.class);
    private static final RestTemplate restTemplate = new RestTemplate();

    @Override
    public SearchResult<ResultSearch> search(String query, int startRow, int limit) {
        String requestURL = photonConfig.buildSearchURL(query,limit);
        LOGGER.info("full request URL: " + requestURL);
        ResponseEntity<SearchResponsePhoton> searchResponse = restTemplate.getForEntity(requestURL, SearchResponsePhoton.class);
        ExternalExceptions.validateExternalResponse(searchResponse,requestURL);
        assert searchResponse.getBody() != null;
        List<Feature> featureList = searchResponse.getBody().getFeatures();
        List<ResultSearch> resultList = featureList.stream()
                .filter(feature -> feature.getProperties() != null && feature.getProperties().getExtent() != null)
                .map(feature -> {
                    Properties props = feature.getProperties();
                    Double[] coordinates = feature.getGeometry().getCoordinates();
                    String type = resolveType(props);
                    return ResultSearch.builder()
                            .source(Source.valueOf(photonConfig.getSourceName().toUpperCase()))
                            .sourceId(String.valueOf(props.getOsmId().longValue()))
                            .name(props.getName()).subdivision(props.getState())
                            .countryCode(props.getCountryCode().toUpperCase())
                            .countryName(props.getCountry()).type(type)
                            .longitude(coordinates[0]).build();
                        })
                .toList();
        return SearchResult.<ResultSearch>builder().results(resultList).resultCount(resultList.size()).build();
    }

    @Override
    public ResultSearchFull fetchResultSearch(String sourceId) {
        return null;
    }

    @Override
    public LookupAttribution attribution() {
        return LookupAttribution.builder().name("Photon").link("https://photon.komoot.io/").build();
    }

    @Override
    public List<ResultSearch> augmentLocationStatus(List<ResultSearch> cachedResults) {
        List<String> sourceIds = cachedResults.stream().map(ResultSearch::getSourceId).toList();
        Map<String, Status> locationIdToStatusDB = locationService.getSourceIdsToStatusMap(Source.valueOf(photonConfig.getSourceName().toUpperCase()),sourceIds);
        cachedResults.forEach(resultSearch -> resultSearch.setStatus(locationIdToStatusDB.getOrDefault(resultSearch.getSourceId(),Status.NEW)));
        return cachedResults;
    }

    private static String resolveType(Properties props) {
        String type = props.getType();
        if (!StringUtils.isEmpty(type) && !type.equals("other")) return type;
        return props.getOsmValue();
    }
}
