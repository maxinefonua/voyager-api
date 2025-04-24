package org.voyager.service;

import org.voyager.model.LocationDisplay;
import org.voyager.model.entity.Location;
import org.voyager.model.location.LocationForm;

import java.util.List;

public interface LocationService {
    public LocationDisplay save(LocationForm locationForm);
    public List<LocationDisplay> getLocations();
    public List<LocationDisplay> getLocationsByStatus(Location.Status status);
    public List<LocationDisplay> getLocationsByStatusList(List<Location.Status> statusList);
}
