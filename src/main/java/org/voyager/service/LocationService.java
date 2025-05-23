package org.voyager.service;

import io.vavr.control.Option;
import org.voyager.model.location.Location;
import org.voyager.model.location.Source;
import org.voyager.model.location.Status;
import org.voyager.model.location.LocationForm;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LocationService {
    public Location save(LocationForm locationForm);
    public List<Location> getLocations();
    public List<Location> getLocationsByStatus(Status status);
    public List<Location> getLocationsBySourceAndSourceId(Source source, String sourceId);
    public List<Location> getLocationsBySource(Source source);
    Option<Location> getLocationById(Integer id);
    public Set<String> getLocationIdsBySource(Source source);
    public Map<String,Status> getLocationIdsToStatusBySource(Source source);
    public List<Location> getLocationsByStatusList(List<Status> statusList);
}
