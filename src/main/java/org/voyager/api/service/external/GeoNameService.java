package org.voyager.api.service.external;

import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.voyager.commons.model.geoname.query.GeoNearbyQuery;
import org.voyager.commons.model.geoname.query.GeoSearchQuery;
import org.voyager.commons.model.geoname.query.GeoTimezoneQuery;


public interface GeoNameService {
    ResponseEntity<String> findNearbyPlaces(@NonNull GeoNearbyQuery geoNearbyQuery);
    ResponseEntity<String> getTimezone(@NonNull GeoTimezoneQuery geoTimezoneQuery);
    <T> ResponseEntity<T> search(@NonNull GeoSearchQuery geoSearchQuery, Class<T> responseType);
    <T> ResponseEntity<T> getFull(@NonNull Long geoNameId, Class<T> responseType);
    ResponseEntity<String> getCountries();
}
