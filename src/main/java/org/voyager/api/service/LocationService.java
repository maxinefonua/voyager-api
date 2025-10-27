package org.voyager.api.service;

import io.vavr.control.Option;
import jakarta.validation.Valid;
import org.voyager.commons.model.country.Continent;
import org.voyager.commons.model.location.*;

import java.util.List;
import java.util.Map;

public interface LocationService {
    Boolean existsById(Integer id);
    void deleteById(Integer id);
    Location save(LocationForm locationForm);
    Location getLocationById(Integer id);
    Map<String,Status> getSourceIdsToStatusMap(Source source, List<String> sourceIdList);
    Location patch(Integer id, @Valid LocationPatch locationPatch);
    List<Location> getLocations(Option<Source> sourceOption,
                                List<String> countryCodeList, List<Status> statusList,
                                List<Continent> continentList, Option<Integer> limitOption);

    Location getLocation(Source source, String sourceId);
}
