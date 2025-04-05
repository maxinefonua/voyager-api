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
import org.voyager.config.nominatim.NominatimConfig;
import org.voyager.model.result.LookupAttribution;
import org.voyager.model.result.ResultSearch;
import org.voyager.model.response.VoyagerListResponse;
import org.voyager.model.external.nominatim.Address;
import org.voyager.model.external.nominatim.Properties;
import org.voyager.model.external.nominatim.SearchResponseNominatim;
import org.voyager.service.SearchLocationService;
import java.util.List;

@Primary @Service
public class NominatimImpl implements SearchLocationService {
    @Autowired
    NominatimConfig nominatimConfig;

    private static final RestTemplate restTemplate = new RestTemplate();
    private static final Logger LOGGER = LoggerFactory.getLogger(PhotonImpl.class);

    @Override
    public VoyagerListResponse<ResultSearch> search(String query, int startRow, int limit) {
        String requestURL = nominatimConfig.buildSearchURL(query,limit);
        LOGGER.info("full request URL: " + requestURL);
        ResponseEntity<SearchResponseNominatim> searchResponse = restTemplate.getForEntity(requestURL, SearchResponseNominatim.class);
        if (searchResponse.getStatusCode().value() != 200) {
            StringBuilder sb = new StringBuilder();
            sb.append("Received non-200 status code from external API endpoint: ");
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
        List<ResultSearch> resultSearchList = searchResponse.getBody().getFeatures().stream()
                .map(feature -> {
                    Properties props = feature.getProperties();
                    Double[] coordinates = feature.getGeometry().getCoordinates();
                    Double[] bbox = feature.getBbox();
                    Address address = props.getAddress();
                    String adminNameVal = resolveAdminNameVal(address);
                    String type = resolveType(props);
                    return ResultSearch.builder()
                            .name(props.getName()).adminName(adminNameVal)
                            .countryCode(address.getCountryCode().toUpperCase())
                            .countryName(address.getCountry()).type(type)
                            .westBound(bbox[0]).southBound(bbox[1])
                            .eastBound(bbox[2]).northBound(bbox[3])
                            .longitude(coordinates[0]).latitude(coordinates[1]).build();
                })
                .toList();
        return VoyagerListResponse.<ResultSearch>builder().results(resultSearchList).resultCount(resultSearchList.size()).build();
    }

    @Override
    public LookupAttribution attribution() {
        return LookupAttribution.builder().name("Nominatim").link("https://nominatim.org/").build();
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
