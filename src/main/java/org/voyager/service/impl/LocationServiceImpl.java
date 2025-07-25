package org.voyager.service.impl;

import io.vavr.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.error.MessageConstants;
import org.voyager.model.country.Continent;
import org.voyager.model.entity.LocationEntity;
import org.voyager.model.location.*;
import org.voyager.repository.CountryRepository;
import org.voyager.repository.LocationRepository;
import org.voyager.service.LocationService;
import org.voyager.service.utils.MapperUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LocationServiceImpl implements LocationService {
    @Autowired
    LocationRepository locationRepository;
    @Autowired
    CountryRepository countryRepository;

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

    @Override
    public List<Location> getLocations(Option<Source> sourceOption, Option<String> sourceIdOption,
                                       List<String> countryCodeList, Option<Status> statusOption,
                                       List<Continent> continentList) {
        if (!continentList.isEmpty())
            countryCodeList = countryRepository.selectCountryCodesByContinentIn(continentList);

        if (sourceOption.isEmpty() && countryCodeList.isEmpty() && statusOption.isEmpty())
            return locationRepository.findAll().stream().map(MapperUtils::entityToLocation).toList();
        if (sourceOption.isEmpty() && countryCodeList.isEmpty())
            return locationRepository.findByStatus(statusOption.get())
                    .stream().map(MapperUtils::entityToLocation).toList();
        if (sourceOption.isEmpty() && statusOption.isEmpty())
            return locationRepository.findByCountryCodeIn(countryCodeList)
                    .stream().map(MapperUtils::entityToLocation).toList();
        if (countryCodeList.isEmpty() && statusOption.isEmpty() && sourceIdOption.isEmpty())
            return locationRepository.findBySource(sourceOption.get())
                    .stream().map(MapperUtils::entityToLocation).toList();
        if (countryCodeList.isEmpty() && sourceIdOption.isEmpty())
            return locationRepository.findBySourceAndStatus(sourceOption.get(),statusOption.get())
                    .stream().map(MapperUtils::entityToLocation).toList();
        if (statusOption.isEmpty() && sourceIdOption.isEmpty())
            return locationRepository.findBySourceAndCountryCodeIn(sourceOption.get(),countryCodeList)
                    .stream().map(MapperUtils::entityToLocation).toList();
        if (countryCodeList.isEmpty() && statusOption.isEmpty())
            return locationRepository.findBySourceAndSourceId(sourceOption.get(),sourceIdOption.get())
                    .stream().map(MapperUtils::entityToLocation).toList();
        if (sourceOption.isEmpty())
            return locationRepository.findByStatusAndCountryCodeIn(statusOption.get(),countryCodeList)
                    .stream().map(MapperUtils::entityToLocation).toList();
        if (countryCodeList.isEmpty())
            return locationRepository.findBySourceAndSourceIdAndStatus(sourceOption.get(),sourceIdOption.get(),
                            statusOption.get()).stream().map(MapperUtils::entityToLocation).toList();
        if (statusOption.isEmpty())
            return locationRepository.findBySourceAndSourceIdAndCountryCodeIn(sourceOption.get(),sourceIdOption.get(),
                            countryCodeList).stream().map(MapperUtils::entityToLocation).toList();
        return locationRepository.findBySourceAndSourceIdAndStatusAndCountryCodeIn(sourceOption.get(),
                        sourceIdOption.get(),statusOption.get(),countryCodeList).stream().map(MapperUtils::entityToLocation).toList();
    }
}