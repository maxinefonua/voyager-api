package org.voyager.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.error.MessageConstants;
import org.voyager.model.location.LocationDisplay;
import org.voyager.model.entity.Location;
import org.voyager.model.location.LocationForm;
import org.voyager.model.location.Status;
import org.voyager.repository.LocationRepository;
import org.voyager.service.LocationService;
import org.voyager.service.utils.MapperUtils;

import java.util.List;

import static org.voyager.utils.ConstantsUtils.SOURCE_PROPERTY_NAME;

@Service
public class LocationServiceImpl implements LocationService {
    @Autowired
    LocationRepository locationRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationServiceImpl.class);

    @Override
    public LocationDisplay save(LocationForm locationForm) {
        Location location = MapperUtils.formToLocation(locationForm);
        try {
            location = locationRepository.save(location);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.buildRespositorySaveErrorMessage("location"));
        }
        return MapperUtils.locationToDisplay(location);
    }

    @Override
    public List<LocationDisplay> getLocations() {
        return locationRepository.findAll().stream().map(MapperUtils::locationToDisplay).toList();
    }

    @Override
    public List<LocationDisplay> getLocationsByStatus(Status status) {
        return locationRepository.findByStatus(status).stream().map(MapperUtils::locationToDisplay).toList();
    }

    @Override
    public List<LocationDisplay> getLocationsByStatusList(List<Status> statusList) {
        return locationRepository.findByStatusIn(statusList).stream().map(MapperUtils::locationToDisplay).toList();
    }
}
