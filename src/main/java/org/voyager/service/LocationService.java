package org.voyager.service;

import io.vavr.control.Option;
import jakarta.validation.Valid;
import org.voyager.model.country.Continent;
import org.voyager.model.location.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LocationService {
    Boolean existsById(Integer id);
    void deleteById(Integer id);
    Location save(LocationForm locationForm);
    Location getLocationById(Integer id);
    Map<String,Status> getSourceIdsToStatusMap(Source source, List<String> sourceIdList);
    Location patch(Integer id, @Valid LocationPatch locationPatch);
    List<Location> getLocations(Option<Source> sourceOption, Option<String> sourceIdOption, List<String> countryCodeList, List<Status> statusList, List<Continent> continentList);
}
