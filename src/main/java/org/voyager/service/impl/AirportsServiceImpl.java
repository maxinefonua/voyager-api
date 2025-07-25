package org.voyager.service.impl;

import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.error.MessageConstants;
import org.voyager.model.airport.AirportPatch;
import org.voyager.model.entity.AirportEntity;
import org.voyager.model.Airline;
import org.voyager.model.airport.Airport;
import org.voyager.model.airport.AirportType;
import org.voyager.repository.AirlineRepository;
import org.voyager.repository.AirportRepository;
import org.voyager.service.AirportsService;
import org.voyager.service.utils.MapperUtils;

import java.util.*;
import java.util.function.Supplier;

import static org.voyager.service.utils.ServiceUtils.handleJPAExceptions;

@Service
public class AirportsServiceImpl implements AirportsService {
    @Autowired
    AirportRepository airportRepository;

    @Autowired
    AirlineRepository airlineRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(AirportsServiceImpl.class);

    @Override
    public Boolean ifIataExists(String iata) {
        return handleJPAExceptions(() -> airportRepository.existsById(iata));
    }

    @Override
    public List<String> getIata() {
        return handleJPAExceptions(() -> airportRepository.selectIata());
    }

    @Override
    public List<String> getIataByType(AirportType type) {
        return handleJPAExceptions(() -> airportRepository.selectIataByType(type));
    }

    @Override
    public List<String> getIataByTypeIn(List<AirportType> typeList) {
        return handleJPAExceptions(() -> airportRepository.selectIataByTypeIn(typeList));
    }

    @Override
    public List<Airport> getAll(Option<String> countryCode, List<AirportType> airportTypeList, Option<Airline> airline) {
        return handleJPAExceptions(() -> {
            if (countryCode.isEmpty() && airportTypeList.isEmpty() && airline.isEmpty()) {
                LOGGER.debug("fetching uncached get all airports");
                return airportRepository.findAll(Sort.by(Sort.Direction.ASC, "iata")).stream()
                        .map(MapperUtils::entityToAirport).toList();
            }
            if (countryCode.isEmpty() && airportTypeList.isEmpty()) {
                LOGGER.debug(String.format("fetching uncached get airports by airline: %s", airline.get()));
                return airportRepository.findByIataInOrderByIataAsc(
                        getActiveAirlineCodes(airline.get())).stream().map(MapperUtils::entityToAirport).toList();
            }
            if (countryCode.isEmpty() && airline.isEmpty()) {
                LOGGER.debug(String.format("fetching uncached get airports by type: %s", airportTypeList));
                return airportRepository.findByTypeInOrderByIataAsc(airportTypeList).stream().map(MapperUtils::entityToAirport).toList();
            }
            if (airportTypeList.isEmpty() && airline.isEmpty()) {
                LOGGER.debug(String.format("fetching uncached get airports by country code: %s", countryCode.get()));
                return airportRepository.findByCountryCodeOrderByIataAsc(countryCode.get()).stream().map(MapperUtils::entityToAirport).toList();
            }
            if (airline.isEmpty()) {
                LOGGER.debug(String.format("fetching uncached get airports by type: %s and country code: %s", airportTypeList, countryCode.get()));
                return airportRepository.findByCountryCodeAndTypeInOrderByIataAsc(countryCode.get(), airportTypeList).stream().map(MapperUtils::entityToAirport).toList();
            }
            if (airportTypeList.isEmpty()) {
                LOGGER.debug(String.format("fetching uncached get airports by country code: %s and airline: %s", countryCode.get(), airline.get()));
                List<String> validAirlineAirports = getActiveAirlineCodes(airline.get());
                return airportRepository.findByCountryCodeOrderByIataAsc(countryCode.get()).stream()
                        .filter(airportEntity -> validAirlineAirports.contains(airportEntity.getIata())
                ).map(MapperUtils::entityToAirport).toList();
            }
            if (countryCode.isEmpty()) {
                LOGGER.debug(String.format("fetching uncached get airports by type: %s and airline: %s", airportTypeList, airline.get()));
                List<String> validAirlineAirports = getActiveAirlineCodes(airline.get());
                return airportRepository.findByTypeInOrderByIataAsc(airportTypeList).stream().filter(
                        airportEntity -> validAirlineAirports.contains(airportEntity.getIata())
                ).map(MapperUtils::entityToAirport).toList();
            }
            List<String> validAirlineAirports = getActiveAirlineCodes(airline.get());
            return airportRepository.findByCountryCodeAndTypeInOrderByIataAsc(countryCode.get(), airportTypeList).stream().filter(
                    airportEntity -> validAirlineAirports.contains(airportEntity.getIata())
            ).map(MapperUtils::entityToAirport).toList();
        });
    }

    @Override
    public List<Airport> getByDistance(double latitude, double longitude,
                                       int limit, List<AirportType> airportTypeList, List<Airline> airlineList) {
        return handleJPAExceptions(() -> {
            if (airportTypeList.isEmpty() && airlineList.isEmpty()) {
                LOGGER.debug(String.format("fetching uncached get nearby airports for latitude: %f, longitude: %f, with limit: %d", latitude, longitude, limit));
                return airportRepository.findByTypeInOrderByIataAsc(List.of(AirportType.CIVIL, AirportType.MILITARY)).stream().map(airportEntity -> MapperUtils.entityToAirport(airportEntity,
                                Airport.calculateDistanceKm(latitude, longitude, airportEntity.getLatitude(), airportEntity.getLongitude())))
                        .sorted(Comparator.comparingDouble(Airport::getDistance)).limit(limit).toList();
            } else if (airlineList.isEmpty()) {
                LOGGER.debug(String.format("fetching uncached get nearby airports for type: %s, latitude: %f, longitude: %f, with limit: %d", airportTypeList, latitude, longitude, limit));
                return airportRepository.findByTypeInOrderByIataAsc(airportTypeList).stream().map(airportEntity -> MapperUtils.entityToAirport(airportEntity,
                                Airport.calculateDistanceKm(latitude, longitude, airportEntity.getLatitude(), airportEntity.getLongitude())))
                        .sorted(Comparator.comparingDouble(Airport::getDistance)).limit(limit).toList();
            } else if (airportTypeList.isEmpty()) {
                LOGGER.debug(String.format("fetching uncached get nearby airports for airlines: %s, latitude: %f, longitude: %f, with limit: %d", airlineList, latitude, longitude, limit));
                return airportRepository.findByIataInOrderByIataAsc(getDistinctIataCodesForAirlineList(airlineList)).stream().map(airportEntity -> MapperUtils.entityToAirport(airportEntity,
                                Airport.calculateDistanceKm(latitude, longitude, airportEntity.getLatitude(), airportEntity.getLongitude())))
                        .sorted(Comparator.comparingDouble(Airport::getDistance)).limit(limit).toList();
            }

            LOGGER.debug(String.format("fetching uncached get nearby airports for type: %s, airline: %s, latitude: %f, longitude: %f, with limit: %d", airportTypeList, airlineList, latitude, longitude, limit));
            return airportRepository.findByIataInOrderByIataAsc(getDistinctIataCodesForAirlineList(airlineList)).stream().map(airportEntity -> MapperUtils.entityToAirport(airportEntity,
                            Airport.calculateDistanceKm(latitude, longitude, airportEntity.getLatitude(), airportEntity.getLongitude())))
                    .sorted(Comparator.comparingDouble(Airport::getDistance)).limit(limit).toList();
        });
    }

    @Override
    public Airport getByIata(String iata) {
        Optional<AirportEntity> optional =
                handleJPAExceptions(() -> airportRepository.findById(iata));
        if (optional.isEmpty()) {
            LOGGER.error(String.format("getByIata called with a nonexistent iata value = '%s'",iata));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"An internal service exception was thrown");
        }
        return MapperUtils.entityToAirport(optional.get());
    }

    @Override
    public Airport patch(String iata, AirportPatch airportPatch) {
        AirportEntity existing = handleJPAExceptions(() -> airportRepository.findById(iata).get());
        if (StringUtils.isNotBlank(airportPatch.getName()))
            existing.setName(airportPatch.getName());
        if (StringUtils.isNotBlank(airportPatch.getCity()))
            existing.setCity(airportPatch.getCity());
        if (StringUtils.isNotBlank(airportPatch.getSubdivision()))
            existing.setSubdivision(airportPatch.getSubdivision());
        if (StringUtils.isNotBlank(airportPatch.getType()))
            existing.setType(AirportType.valueOf(airportPatch.getType().toUpperCase()));
        try {
            AirportEntity modified = existing.toBuilder().build();
            existing =
                    handleJPAExceptions(() -> airportRepository.save(modified));
        } catch (Throwable e) {
            LOGGER.error(e.getMessage()); // TODO: implement alerting
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.buildRespositoryPatchErrorMessage("airport",iata),
                    e);
        }
        return MapperUtils.entityToAirport(existing);
    }

    @Override
    public List<Airline> getAirlines(List<String> iataList) {
        return airlineRepository.selectDistinctAirlinesByIataInAndIsActive(iataList,true);
    }

    private List<String> getActiveAirlineCodes(Airline airline) {
        return handleJPAExceptions(() ->
                airlineRepository.selectIataCodesByAirlineAndIsActive(airline,true));
    }

    private List<String> getDistinctIataCodesForAirlineList(List<Airline> airlineList) {
        return handleJPAExceptions(() ->
                airlineRepository.selectDistinctIataCodesByAirlineInAndIsActive(airlineList,true));
    }
}
