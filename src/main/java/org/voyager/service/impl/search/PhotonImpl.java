package org.voyager.service.impl.search;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.config.photon.PhotonConfig;
import org.voyager.model.result.LookupAttribution;
import org.voyager.model.result.ResultSearch;
import org.voyager.model.response.VoyagerListResponse;
import org.voyager.model.external.photon.Properties;
import org.voyager.model.external.photon.Feature;
import org.voyager.model.external.photon.SearchResponsePhoton;
import org.voyager.service.SearchLocationService;
import java.util.List;

@Service
public class PhotonImpl implements SearchLocationService {
    @Autowired
    PhotonConfig photonConfig;

    private static final Logger LOGGER = LoggerFactory.getLogger(PhotonImpl.class);
    private static final RestTemplate restTemplate = new RestTemplate();

    @Override
    public VoyagerListResponse<ResultSearch> search(String query, int startRow, int limit) {
        String requestURL = photonConfig.buildSearchURL(query,limit);
        LOGGER.info("full request URL: " + requestURL);
        ResponseEntity<SearchResponsePhoton> searchResponse = restTemplate.getForEntity(requestURL, SearchResponsePhoton.class);
        if (searchResponse.getStatusCode().value() != 200 || !searchResponse.hasBody()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Received non-200 status code or null response body from external API endpoint: ");
            sb.append(requestURL);
            if (searchResponse.hasBody()) {
                sb.append("\n");
                sb.append("Response: ");
                sb.append(searchResponse.getBody());
            }
            LOGGER.error(sb.toString());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error occurred fetching search results.");
        }
        assert searchResponse.getBody() != null;
        List<Feature> featureList = searchResponse.getBody().getFeatures();
        List<ResultSearch> resultList = featureList.stream()
                .filter(feature -> feature.getProperties() != null && feature.getProperties().getExtent() != null)
                .map(feature -> {
                    Properties props = feature.getProperties();
                    Double[] coordinates = feature.getGeometry().getCoordinates();
                    Double[] bbox = props.getExtent();
                    String adminNameVal = props.getState();
                    String type = resolveType(props);
                    return ResultSearch.builder()
                            .name(props.getName()).adminName(adminNameVal)
                            .countryCode(props.getCountryCode().toUpperCase())
                            .countryName(props.getCountry()).type(type)
                            .westBound(bbox[0]).southBound(bbox[1])
                            .eastBound(bbox[2]).northBound(bbox[3])
                            .longitude(coordinates[0]).latitude(coordinates[1]).build();
                        })
                .toList();
        return VoyagerListResponse.<ResultSearch>builder().results(resultList).resultCount(resultList.size()).build();
    }

    @Override
    public LookupAttribution attribution() {
        return LookupAttribution.builder().name("Photon").link("https://photon.komoot.io/").build();
    }

    private static String resolveType(Properties props) {
        String type = props.getType();
        if (!StringUtils.isEmpty(type) && !type.equals("other")) return type;
        return props.getOsmValue();
    }
}
