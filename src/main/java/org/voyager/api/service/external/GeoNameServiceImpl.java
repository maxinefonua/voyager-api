package org.voyager.api.service.external;

import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.voyager.api.config.VoyagerGeoNamesConfig;
import org.voyager.commons.model.geoname.config.GeoNamesConfig;
import org.voyager.commons.model.geoname.query.GeoNearbyQuery;
import org.voyager.commons.model.geoname.query.GeoSearchQuery;
import org.voyager.commons.model.geoname.query.GeoTimezoneQuery;

import static org.voyager.api.service.utils.ServiceUtils.handleExternalServiceExceptions;

@Service
public class GeoNameServiceImpl implements GeoNameService {
    @Autowired
    VoyagerGeoNamesConfig voyagerGeoNamesConfig;
    GeoNamesConfig geoNamesConfig;

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoNameServiceImpl.class);

    @PostConstruct
    public void setup() {
        geoNamesConfig = new GeoNamesConfig(voyagerGeoNamesConfig.getUsername());
    }

    private static final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Cacheable("findNearbyCacheGeoService")
    public ResponseEntity<String> findNearbyPlaces(@NonNull GeoNearbyQuery geoNearbyQuery) {
        String requestURL = geoNamesConfig.getNearbyPlaceURL(geoNearbyQuery);
        return handleExternalServiceExceptions(()-> restTemplate.getForEntity(requestURL,String.class));
    }

    @Override
    @Cacheable("timezoneCacheGeoService")
    public ResponseEntity<String> getTimezone(@NonNull GeoTimezoneQuery geoTimezoneQuery) {
        String requestURL = geoNamesConfig.getTimezoneURL(geoTimezoneQuery);
        LOGGER.info("get timezone at {}",requestURL);
        return handleExternalServiceExceptions(()-> restTemplate.getForEntity(requestURL,String.class));
    }

    @Override
    @Cacheable("searchCacheGeoService")
    public <T> ResponseEntity<T> search(@NonNull GeoSearchQuery geoSearchQuery, Class<T> responseType) {
        String requestURL = geoNamesConfig.getSearchURL(geoSearchQuery);
        return handleExternalServiceExceptions(()-> restTemplate.getForEntity(requestURL,responseType));
    }

    @Override
    @Cacheable("getCacheGeoService")
    public <T> ResponseEntity<T> getFull(@NonNull Long geoNameId, Class<T> responseType) {
        String requestURL = geoNamesConfig.getByIdURL(geoNameId);
        return handleExternalServiceExceptions(()-> restTemplate.getForEntity(requestURL,responseType));
    }

    @Override
    @Cacheable("countriesCacheGeoService")
    public ResponseEntity<String> getCountries() {
        String requestURL = geoNamesConfig.getCountriesURL();
        return handleExternalServiceExceptions(()-> restTemplate.getForEntity(requestURL,String.class));
    }
}
