package org.voyager.service.impl;

import io.vavr.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.parameters.P;
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

import static org.voyager.error.MessageConstants.INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE;
import static org.voyager.utils.ConstantsUtils.SOURCE_ID_PARAM_NAME;
import static org.voyager.utils.ConstantsUtils.SOURCE_PARAM_NAME;

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
    public List<Location> getLocations(Option<Source> sourceOption,
                                       List<String> countryCodeListFromRequest, List<Status> statusList,
                                       List<Continent> continentList, Option<Integer> limitOption) {
        return ServiceUtils.handleJPAExceptions(()->{
            List<String> countryCodeList = countryCodeListFromRequest;
            if (!continentList.isEmpty())
                countryCodeList = countryRepository.selectCountryCodesByContinentIn(continentList);

            if (sourceOption.isEmpty() && countryCodeList.isEmpty() && statusList.isEmpty()) {
                if (limitOption.isEmpty())
                    return locationRepository.findAll().stream().map(MapperUtils::entityToLocation).toList();
                return locationRepository.findAll(PageRequest.of(0,limitOption.get())).stream()
                        .map(MapperUtils::entityToLocation).toList();
            }
            if (sourceOption.isEmpty() && countryCodeList.isEmpty()) {
                if (limitOption.isEmpty())
                    return locationRepository.findByStatusIn(statusList)
                        .stream().map(MapperUtils::entityToLocation).toList();
                return locationRepository.findByStatusIn(statusList,PageRequest.of(0,limitOption.get()))
                        .stream().map(MapperUtils::entityToLocation).toList();
            }
            if (sourceOption.isEmpty() && statusList.isEmpty()) {
                if (limitOption.isEmpty())
                    return locationRepository.findByCountryCodeIn(countryCodeList)
                        .stream().map(MapperUtils::entityToLocation).toList();
                return locationRepository.findByCountryCodeIn(countryCodeList,PageRequest.of(0, limitOption.get()))
                        .stream().map(MapperUtils::entityToLocation).toList();
            }
            if (countryCodeList.isEmpty() && statusList.isEmpty()) {
                if (limitOption.isEmpty())
                    return locationRepository.findBySource(sourceOption.get())
                        .stream().map(MapperUtils::entityToLocation).toList();
                return locationRepository.findBySource(sourceOption.get(), PageRequest.of(0, limitOption.get()))
                        .stream().map(MapperUtils::entityToLocation).toList();
            }
            if (countryCodeList.isEmpty()) {
                if (limitOption.isEmpty())
                    return locationRepository.findBySourceAndStatusIn(sourceOption.get(), statusList)
                        .stream().map(MapperUtils::entityToLocation).toList();
                return locationRepository.findBySourceAndStatusIn(sourceOption.get(), statusList, PageRequest.of(0, limitOption.get()))
                        .stream().map(MapperUtils::entityToLocation).toList();
            }
            if (statusList.isEmpty()) {
                if (limitOption.isEmpty())
                    return locationRepository.findBySourceAndCountryCodeIn(sourceOption.get(), countryCodeList)
                        .stream().map(MapperUtils::entityToLocation).toList();
                return locationRepository.findBySourceAndCountryCodeIn(sourceOption.get(), countryCodeList, PageRequest.of(0, limitOption.get()))
                        .stream().map(MapperUtils::entityToLocation).toList();
            }
            if (sourceOption.isEmpty()) {
                if (limitOption.isEmpty())
                    return locationRepository.findByStatusInAndCountryCodeIn(statusList, countryCodeList)
                        .stream().map(MapperUtils::entityToLocation).toList();
                return locationRepository.findByStatusInAndCountryCodeIn(statusList, countryCodeList,PageRequest.of(0,limitOption.get()))
                        .stream().map(MapperUtils::entityToLocation).toList();
            }
            if (limitOption.isEmpty())
                return locationRepository.findBySourceAndStatusInAndCountryCodeIn(sourceOption.get(),statusList,countryCodeList)
                        .stream().map(MapperUtils::entityToLocation).toList();
            return locationRepository.findBySourceAndStatusInAndCountryCodeIn(sourceOption.get(),statusList,countryCodeList,PageRequest.of(0, limitOption.get()))
                    .stream().map(MapperUtils::entityToLocation).toList();
        });
    }

    @Override
    public Location getLocation(Source source, String sourceId) {
        return ServiceUtils.handleJPAExceptions(() -> {
            List<LocationEntity> locationEntityList = locationRepository.findBySourceAndSourceId(source,sourceId);
            if (locationEntityList.size() > 1) {
                LOGGER.error(String.format("Source '%s' and sourceId '%s' returned multiple locations when duplicates should not exist. Investigation required.",
                        source.name(),sourceId));
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE);
            }
            if (locationEntityList.isEmpty()) {
                LOGGER.error(String.format("Source '%s' and sourceId '%s' returned no matches in db. Verify correct lookup details.",
                        source.name(),sourceId));
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        MessageConstants.buildResourceNotFoundForMultiParameterMessage(SOURCE_PARAM_NAME,source.name(),
                                SOURCE_ID_PARAM_NAME,sourceId));
            }
            return MapperUtils.entityToLocation(locationEntityList.get(0));
        });
    }
}