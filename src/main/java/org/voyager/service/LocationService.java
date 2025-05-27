package org.voyager.service;

import io.vavr.control.Option;
import jakarta.validation.Valid;
import org.voyager.model.location.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LocationService {
    public Location save(LocationForm locationForm);
    public List<Location> getLocations();
    public List<Location> getLocationsByStatus(Status status);
    public List<Location> getLocationsBySourceAndSourceId(Source source, String sourceId);
    public List<Location> getLocationsBySourceAndSourceIdList(Source source, List<String> sourceIdList);
    public List<Location> getLocationsBySource(Source source);
    Option<Location> getLocationById(Integer id);
    public Set<String> getLocationIdsBySource(Source source);
    public Map<String,Status> getSourceIdsToStatusBySource(Source source);
    public Map<String,Status> getSourceIdsToStatusMap(Source source, List<String> sourceIdList);
    public List<Location> getLocationsByStatusList(List<Status> statusList);
    Location patch(Location location, @Valid LocationPatch locationPatch);
}
