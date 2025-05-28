package org.voyager.service.impl;

import io.vavr.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.error.MessageConstants;
import org.voyager.model.entity.LocationEntity;
import org.voyager.model.entity.RouteEntity;
import org.voyager.model.location.*;
import org.voyager.repository.LocationRepository;
import org.voyager.service.LocationService;
import org.voyager.service.utils.MapperUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LocationServiceImpl implements LocationService {
    @Autowired
    LocationRepository locationRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationServiceImpl.class);

    @Override
    public Location save(LocationForm locationForm) {
        LocationEntity locationEntity = MapperUtils.formToLocationEntity(locationForm);
        try {
            locationEntity = locationRepository.save(locationEntity);
        } catch (Exception e) {
            LOGGER.error(e.getMessage()); // TODO: implement alerting
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.buildRespositorySaveErrorMessage("location"),
                    e);
        }
        return MapperUtils.entityToLocation(locationEntity);
    }

    @Override
    public List<Location> getLocations() {
        return locationRepository.findAll().stream().map(MapperUtils::entityToLocation).toList();
    }

    @Override
    public List<Location> getLocationsByStatus(Status status) {
        return locationRepository.findByStatus(status).stream().map(MapperUtils::entityToLocation).toList();
    }

    @Override
    public List<Location> getLocationsBySourceAndSourceId(Source source, String sourceId) {
        return locationRepository.findBySourceAndSourceId(source,sourceId).stream().map(MapperUtils::entityToLocation).toList();
    }

    @Override
    public List<Location> getLocationsBySourceAndSourceIdAndStatus(Source source, String sourceId, Status status) {
        return locationRepository.findBySourceAndSourceIdAndStatus(source,sourceId,status).stream().map(MapperUtils::entityToLocation).toList();
    }

    @Override
    public List<Location> getLocationsBySourceAndStatus(Source source, Status status) {
        return locationRepository.findBySourceAndStatus(source,status).stream().map(MapperUtils::entityToLocation).toList();
    }

    @Override
    public List<Location> getLocationsBySourceAndSourceIdList(Source source, List<String> sourceIdList) {
        return locationRepository.findBySourceAndSourceIdIn(source,sourceIdList).stream().map(MapperUtils::entityToLocation).toList();
    }

    @Override
    public List<Location> getLocationsBySource(Source source) {
        return locationRepository.findBySource(source).stream().map(MapperUtils::entityToLocation).toList();
    }

    @Override
    public Option<Location> getLocationById(Integer id) {
        Optional<LocationEntity> location = locationRepository.findById(id);
        if (location.isEmpty()) return Option.none();
        return Option.of(MapperUtils.entityToLocation(location.get()));
    }

    @Override
    public Set<String> getLocationIdsBySource(Source source) {
        return locationRepository.findBySource(source).stream().map(LocationEntity::getSourceId).collect(Collectors.toSet());
    }

    @Override
    public Map<String, Status> getSourceIdsToStatusBySource(Source source) {
        return locationRepository.findBySource(source).stream().collect(Collectors.toMap(LocationEntity::getSourceId, LocationEntity::getStatus));
    }

    @Override
    public Map<String, Status> getSourceIdsToStatusMap(Source source, List<String> sourceIdList) {
        return locationRepository.findBySourceAndSourceIdIn(source,sourceIdList).stream().collect(Collectors.toMap(LocationEntity::getSourceId, LocationEntity::getStatus));
    }

    @Override
    public List<Location> getLocationsByStatusList(List<Status> statusList) {
        return locationRepository.findByStatusIn(statusList).stream().map(MapperUtils::entityToLocation).toList();
    }

    @Override
    public Location patch(Location location, LocationPatch locationPatch) {
        LocationEntity patched = locationRepository.findById(location.getId()).get();
        if (locationPatch.getAirports() != null) patched.setAirports(locationPatch.getAirports().toArray(String[]::new));
        if (locationPatch.getStatus() != null) patched.setStatus(Status.valueOf(locationPatch.getStatus()));
        try {
            LocationEntity saved = locationRepository.save(patched);
            return MapperUtils.entityToLocation(saved);
        } catch (Exception e) {
            LOGGER.error(e.getMessage()); // TODO: implement alerting
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.buildRespositoryPatchErrorMessage(
                            "location",String.valueOf(location.getId())),
                    e);
        }
    }
}