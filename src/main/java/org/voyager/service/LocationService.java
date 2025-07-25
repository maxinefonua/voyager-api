package org.voyager.service;

import io.vavr.control.Option;
import jakarta.validation.Valid;
import org.voyager.model.country.Continent;
import org.voyager.model.location.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LocationService {
    public Location save(LocationForm locationForm);
    Option<Location> getLocationById(Integer id);
    public Set<String> getLocationIdsBySource(Source source);
    public Map<String,Status> getSourceIdsToStatusBySource(Source source);
    public Map<String,Status> getSourceIdsToStatusMap(Source source, List<String> sourceIdList);
    public List<Location> getLocationsByStatusList(List<Status> statusList);
    Location patch(Location location, @Valid LocationPatch locationPatch);
    List<Location> getLocations(Option<Source> sourceOption, Option<String> sourceIdOption, List<String> countryCodeList, Option<Status> statusOption, List<Continent> continentList);
}
