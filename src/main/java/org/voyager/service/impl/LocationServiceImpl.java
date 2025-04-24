package org.voyager.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.voyager.model.LocationDisplay;
import org.voyager.model.entity.Location;
import org.voyager.model.location.LocationForm;
import org.voyager.repository.LocationRepository;
import org.voyager.service.LocationService;
import org.voyager.service.utils.MapperUtils;

import java.util.List;

public class LocationServiceImpl implements LocationService {
    @Autowired
    LocationRepository locationRepository;

    @Override
    public LocationDisplay save(LocationForm locationForm) {
        Location location = MapperUtils.formToLocation(locationForm);
        return MapperUtils.locationToDisplay(locationRepository.save(location));
    }

    @Override
    public List<LocationDisplay> getLocationsByStatus(Location.Status status) {
        return locationRepository.findByStatus(status).stream().map(MapperUtils::locationToDisplay).toList();
    }

    @Override
    public List<LocationDisplay> getLocationsByStatusList(List<Location.Status> statusList) {
        return locationRepository.findByStatusIn(statusList).stream().map(MapperUtils::locationToDisplay).toList();
    }
}
