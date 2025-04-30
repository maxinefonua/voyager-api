package org.voyager.service;

import org.voyager.model.location.LocationDisplay;
import org.voyager.model.location.Source;
import org.voyager.model.location.Status;
import org.voyager.model.location.LocationForm;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LocationService {
    public LocationDisplay save(LocationForm locationForm);
    public List<LocationDisplay> getLocations();
    public List<LocationDisplay> getLocationsByStatus(Status status);
    public List<LocationDisplay> getLocationsBySourceAndSourceId(Source source, String sourceId);
    public List<LocationDisplay> getLocationsBySource(Source source);
    public Set<String> getLocationIdsBySource(Source source);
    public Map<String,Status> getLocationIdsToStatusBySource(Source source);
    public List<LocationDisplay> getLocationsByStatusList(List<Status> statusList);
}
