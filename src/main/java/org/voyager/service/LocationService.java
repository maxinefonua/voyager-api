package org.voyager.service;

import org.voyager.model.location.LocationDisplay;
import org.voyager.model.location.Status;
import org.voyager.model.location.LocationForm;

import java.util.List;

public interface LocationService {
    public LocationDisplay save(LocationForm locationForm);
    public List<LocationDisplay> getLocations();
    public List<LocationDisplay> getLocationsByStatus(Status status);
    public List<LocationDisplay> getLocationsByStatusList(List<Status> statusList);
}
