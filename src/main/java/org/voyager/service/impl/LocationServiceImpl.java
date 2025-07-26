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
import org.voyager.service.utils.ServiceUtils;

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
    public Boolean existsById(Integer id) {
        return locationRepository.existsById(id);
    }

    @Override
    public void deleteById(Integer id) {
        ServiceUtils.handleJPAExceptions(()->{
            locationRepository.deleteById(id);
        });
    }

    @Override
    public Location save(LocationForm locationForm) {
        return ServiceUtils.handleJPAExceptions(() -> {
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
        });
    }

    @Override
    public Location getLocationById(Integer id) {
        return MapperUtils.entityToLocation(locationRepository.findById(id).get());
    }

    @Override
    public Map<String, Status> getSourceIdsToStatusMap(Source source, List<String> sourceIdList) {
        return locationRepository.findBySourceAndSourceIdIn(source,sourceIdList).stream().collect(Collectors.toMap(LocationEntity::getSourceId, LocationEntity::getStatus));
    }

    @Override
    public Location patch(Integer id, LocationPatch locationPatch) {
        return ServiceUtils.handleJPAExceptions(()->{
            LocationEntity fromDb = locationRepository.findById(id).get();
            if (locationPatch.getAirports() != null) fromDb.setAirports(locationPatch.getAirports().toArray(String[]::new));
            if (locationPatch.getStatus() != null) fromDb.setStatus(Status.valueOf(locationPatch.getStatus()));
            try {
                return MapperUtils.entityToLocation(locationRepository.save(fromDb));
            } catch (Exception e) {
                LOGGER.error(e.getMessage()); // TODO: implement alerting
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        MessageConstants.buildRespositoryPatchErrorMessage(
                                "location",String.valueOf(id)),
                        e);
            }
        });
    }

    @Override
    public List<Location> getLocations(Option<Source> sourceOption, Option<String> sourceIdOption,
                                       List<String> countryCodeListFromRequest, List<Status> statusList,
                                       List<Continent> continentList) {
        return ServiceUtils.handleJPAExceptions(()->{
            List<String> countryCodeList = countryCodeListFromRequest;
            if (!continentList.isEmpty())
                countryCodeList = countryRepository.selectCountryCodesByContinentIn(continentList);

            if (sourceOption.isEmpty() && countryCodeList.isEmpty() && statusList.isEmpty())
                return locationRepository.findAll().stream().map(MapperUtils::entityToLocation).toList();
            if (sourceOption.isEmpty() && countryCodeList.isEmpty())
                return locationRepository.findByStatusIn(statusList)
                        .stream().map(MapperUtils::entityToLocation).toList();
            if (sourceOption.isEmpty() && statusList.isEmpty())
                return locationRepository.findByCountryCodeIn(countryCodeList)
                        .stream().map(MapperUtils::entityToLocation).toList();
            if (countryCodeList.isEmpty() && statusList.isEmpty() && sourceIdOption.isEmpty())
                return locationRepository.findBySource(sourceOption.get())
                        .stream().map(MapperUtils::entityToLocation).toList();
            if (countryCodeList.isEmpty() && sourceIdOption.isEmpty())
                return locationRepository.findBySourceAndStatusIn(sourceOption.get(),statusList)
                        .stream().map(MapperUtils::entityToLocation).toList();
            if (statusList.isEmpty() && sourceIdOption.isEmpty())
                return locationRepository.findBySourceAndCountryCodeIn(sourceOption.get(),countryCodeList)
                        .stream().map(MapperUtils::entityToLocation).toList();
            if (countryCodeList.isEmpty() && statusList.isEmpty())
                return locationRepository.findBySourceAndSourceId(sourceOption.get(),sourceIdOption.get())
                        .stream().map(MapperUtils::entityToLocation).toList();
            if (sourceOption.isEmpty())
                return locationRepository.findByStatusInAndCountryCodeIn(statusList,countryCodeList)
                        .stream().map(MapperUtils::entityToLocation).toList();
            if (countryCodeList.isEmpty())
                return locationRepository.findBySourceAndSourceIdAndStatusIn(sourceOption.get(),sourceIdOption.get(),
                        statusList).stream().map(MapperUtils::entityToLocation).toList();
            if (statusList.isEmpty())
                return locationRepository.findBySourceAndSourceIdAndCountryCodeIn(sourceOption.get(),sourceIdOption.get(),
                        countryCodeList).stream().map(MapperUtils::entityToLocation).toList();
            return locationRepository.findBySourceAndSourceIdAndStatusInAndCountryCodeIn(sourceOption.get(),
                    sourceIdOption.get(),statusList,countryCodeList).stream().map(MapperUtils::entityToLocation).toList();
        });
    }
}