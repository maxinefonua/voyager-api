package org.voyager.api.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.voyager.commons.constants.GeoNames;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.model.geoname.fields.FeatureClass;
import org.voyager.commons.model.geoname.query.GeoNearbyQuery;
import org.voyager.commons.model.geoname.query.GeoSearchQuery;
import org.voyager.commons.model.geoname.query.GeoTimezoneQuery;
import org.voyager.api.service.external.GeoNameService;
import org.voyager.api.validate.ValidationUtils;

@RestController
public class GeoNamesController {
    @Autowired
    GeoNameService geoNameService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoNamesController.class);

    @GetMapping(GeoNames.NEARBY_PLACES)
    @Cacheable("geoNearbyVoyager")
    public ResponseEntity<String> findNearbyPlaces(@RequestParam(name = ParameterNames.LATITUDE_PARAM_NAME)
                                                       String latitudeString,
                                                   @RequestParam(name = ParameterNames.LONGITUDE_PARAM_NAME)
                                                   String longitudeString,
                                                   @RequestParam(name = GeoNames.ParameterNames.RADIUS,
                                                   required = false) String radiusString) {
        LOGGER.info("uncached GET {} with latitude: {}, longitude: {}, radius: {}",GeoNames.NEARBY_PLACES,
                latitudeString,longitudeString,radiusString);
        Double latitude = ValidationUtils.validateAndGetDouble(
                ParameterNames.LATITUDE_PARAM_NAME,latitudeString);
        Double longitude = ValidationUtils.validateAndGetDouble(
                ParameterNames.LONGITUDE_PARAM_NAME,longitudeString);

        Integer radius = null;
        if (StringUtils.isNotBlank(radiusString)) {
            radius = ValidationUtils.validateAndGetInteger(GeoNames.ParameterNames.RADIUS,radiusString);
        }

        GeoNearbyQuery geoNearbyQuery = GeoNearbyQuery.builder().latitude(latitude).longitude(longitude)
                .radiusKm(radius).build();

        return geoNameService.findNearbyPlaces(geoNearbyQuery);
    }

    @GetMapping(GeoNames.TIMEZONE)
    @Cacheable("geoTimezoneVoyager")
    public ResponseEntity<String> getTimezone(@RequestParam(name = ParameterNames.LATITUDE_PARAM_NAME)
                                           String latitudeString,
                                          @RequestParam(name = ParameterNames.LONGITUDE_PARAM_NAME)
                                           String longitudeString) {
        LOGGER.info("uncached GET {} with latitude: {}, longitude: {}",GeoNames.TIMEZONE,
                latitudeString, longitudeString);
        Double latitude = ValidationUtils.validateAndGetDouble(
                ParameterNames.LATITUDE_PARAM_NAME,latitudeString);
        Double longitude = ValidationUtils.validateAndGetDouble(
                ParameterNames.LONGITUDE_PARAM_NAME,longitudeString);

        GeoTimezoneQuery geoTimezoneQuery = GeoTimezoneQuery.builder().latitude(latitude)
                .longitude(longitude).build();
        ResponseEntity<String> responseEntity = geoNameService.getTimezone(geoTimezoneQuery);
        return  responseEntity;
    }

    @GetMapping(GeoNames.SEARCH)
    @Cacheable("geoSearchVoyager")
    public ResponseEntity<String> search(@RequestParam(name = GeoNames.ParameterNames.QUERY) String query,
                                         @RequestParam(name = GeoNames.ParameterNames.IS_NAME_REQUIRED,
                                         required = false) String isNameRequiredString,
                                         @RequestParam(name = GeoNames.ParameterNames.FEATURE_CLASS,
                                         required = false) String featureClassString)  {
        LOGGER.info("uncached GET {} with query: {}, isNameRequired: {}, featureClass: {}",GeoNames.SEARCH,
                query,isNameRequiredString,featureClassString);
        ValidationUtils.validateQuery(query);
        GeoSearchQuery geoSearchQuery = GeoSearchQuery.builder().query(query).build();
        if (StringUtils.isNotBlank(isNameRequiredString)) {
            Boolean isNameRequired = ValidationUtils.validateAndGetBoolean(
                    GeoNames.ParameterNames.IS_NAME_REQUIRED, isNameRequiredString);
            geoSearchQuery.setIsNameRequired(isNameRequired);
        }
        if (StringUtils.isNotBlank(featureClassString)) {
            FeatureClass featureClass = ValidationUtils.validateAndGetFeatureClass(
                    GeoNames.ParameterNames.FEATURE_CLASS,featureClassString);
            geoSearchQuery.setFeatureClass(featureClass);
        }
        return geoNameService.search(geoSearchQuery,String.class);
    }

    @GetMapping(GeoNames.FETCH_BY_ID)
    @Cacheable("geoFetchFullVoyager")
    public ResponseEntity<String> fetchFull(@PathVariable(name = ParameterNames.ID_PATH_VAR_NAME)
                                                 String geoIdString)  {
        LOGGER.info("uncached GET {}/{}",GeoNames.FETCH, geoIdString);
        Long geoId = ValidationUtils.validateAndGetLong(ParameterNames.ID_PATH_VAR_NAME,
                geoIdString,false);
        return geoNameService.getFull(geoId,String.class);
    }

    @GetMapping(GeoNames.COUNTRIES)
    @Cacheable("geoCountryVoyager")
    public ResponseEntity<String> getCountryGNList() {
        LOGGER.info("uncached GET {}",GeoNames.COUNTRIES);
        return geoNameService.getCountries();
    }
}
