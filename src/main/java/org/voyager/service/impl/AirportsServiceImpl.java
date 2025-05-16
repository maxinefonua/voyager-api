package org.voyager.service.impl;

import io.vavr.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.entity.Airport;
import org.voyager.model.Airline;
import org.voyager.model.AirportDisplay;
import org.voyager.model.AirportType;
import org.voyager.repository.AirportRepository;
import org.voyager.service.AirportsService;
import org.voyager.service.DeltaService;
import org.voyager.service.utils.MapperUtils;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class AirportsServiceImpl implements AirportsService {
    @Autowired
    AirportRepository airportRepository;

    @Autowired
    DeltaService deltaService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AirportsServiceImpl.class);

    public Option<AirportDisplay> updateAirport(AirportDisplay airportDisplay) {
        Optional<Airport> result = airportRepository.findById(airportDisplay.getIata());
        result.ifPresent(airport -> {
            airport.setName(airportDisplay.getName());
            airport.setSubdivision(airportDisplay.getSubdivision());
            airport.setCity(airportDisplay.getCity());
            airport.setCountryCode(airportDisplay.getCountryCode());
            airport.setType(airportDisplay.getType());
            airport.setLatitude(airportDisplay.getLatitude());
            airport.setLongitude(airportDisplay.getLongitude());
            airportRepository.save(airport);
        });
        if (result.isPresent()) return Option.of(MapperUtils.airportToDisplay(result.get()));
        return Option.none();
    }

    @Override
    public Boolean ifIataExists(String iata) {
        return airportRepository.existsById(iata);
    }

    @Override
    public List<String> getIata() {
        return airportRepository.selectIataOrderByIata();
    }

    @Override
    public List<String> getIataByType(AirportType type) {
        return airportRepository.selectIataByMilitaryTypeOrderByIata(type);
    }

    @Override
    public List<AirportDisplay> getAll(Option<String> countryCode, Option<AirportType> type, Option<Airline> airline) {
        if (countryCode.isEmpty() && type.isEmpty() && airline.isEmpty()) {
            LOGGER.debug("fetching uncached get all airports");
            return airportRepository.findByTypeInOrderByIataAsc(List.of(AirportType.CIVIL,AirportType.MILITARY)).stream().map(MapperUtils::airportToDisplay).toList();
        }
        if (countryCode.isEmpty() && type.isEmpty()) {
            LOGGER.debug(String.format("fetching uncached get airports by airline: %s",airline.get()));
            return airportRepository.findByIataInOrderByIataAsc(getActiveDeltaCodes()).stream().map(MapperUtils::airportToDisplay).toList();
        }
        if (countryCode.isEmpty() && airline.isEmpty()) {
            LOGGER.debug(String.format("fetching uncached get airports by type: %s",type.get()));
            return airportRepository.findByTypeOrderByIataAsc(type.get()).stream().map(MapperUtils::airportToDisplay).toList();
        }
        if (type.isEmpty() && airline.isEmpty()) {
            LOGGER.debug(String.format("fetching uncached get airports by country code: %s",countryCode.get()));
            return airportRepository.findByCountryCodeOrderByIataAsc(countryCode.get()).stream().map(MapperUtils::airportToDisplay).toList();
        }
        if (airline.isEmpty()) {
            LOGGER.debug(String.format("fetching uncached get airports by type: %s and country code: %s",type.get(),countryCode.get()));
            return airportRepository.findByCountryCodeAndTypeOrderByIataAsc(countryCode.get(),type.get()).stream().map(MapperUtils::airportToDisplay).toList();
        }
        if (type.isEmpty()) {
            LOGGER.debug(String.format("fetching uncached get airports by country code: %s and airline: %s",countryCode.get(),airline.get()));
            return airportRepository.findByCountryCodeOrderByIataAsc(countryCode.get()).stream().filter(
                    airport -> validDeltaCode(airport.getIata())
            ).map(MapperUtils::airportToDisplay).toList();
        }
        if (countryCode.isEmpty()) {
            LOGGER.debug(String.format("fetching uncached get airports by type: %s and airline: %s",type.get(),airline.get()));
            return airportRepository.findByTypeOrderByIataAsc(type.get()).stream().filter(
                    airport -> validDeltaCode(airport.getIata())
            ).map(MapperUtils::airportToDisplay).toList();
        }
        return airportRepository.findByCountryCodeAndTypeOrderByIataAsc(countryCode.get(),type.get()).stream().filter(
                airport -> validDeltaCode(airport.getIata())
        ).map(MapperUtils::airportToDisplay).toList();
    }

    @Override
    public List<AirportDisplay> getByDistance(double latitude, double longitude, int limit, Option<AirportType> type, Option<Airline> airline) {
        if (type.isEmpty() && airline.isEmpty()) {
            LOGGER.debug(String.format("fetching uncached get nearby airports for latitude: %f, longitude: %f, with limit: %d",latitude,longitude,limit));
            return airportRepository.findByTypeIn(List.of(AirportType.CIVIL,AirportType.MILITARY)).stream().map(airport -> MapperUtils.airportToDisplay(airport,
                    AirportDisplay.calculateDistance(latitude,longitude,airport.getLatitude(),airport.getLongitude())))
                    .sorted(Comparator.comparingDouble(AirportDisplay::getDistance)).limit(limit).toList();
        } else if (airline.isEmpty()) {
            LOGGER.debug(String.format("fetching uncached get nearby airports for type: %s, latitude: %f, longitude: %f, with limit: %d",type.get(),latitude,longitude,limit));
            return airportRepository.findByType(type.get()).stream().map(airport -> MapperUtils.airportToDisplay(airport,
                    AirportDisplay.calculateDistance(latitude,longitude,airport.getLatitude(),airport.getLongitude())))
                    .sorted(Comparator.comparingDouble(AirportDisplay::getDistance)).limit(limit).toList();
        } else if (type.isEmpty()) {
            LOGGER.debug(String.format("fetching uncached get nearby airports for airline: %s, latitude: %f, longitude: %f, with limit: %d",airline.get(),latitude,longitude,limit));
            return airportRepository.findByIataIn(getActiveDeltaCodes()).stream().map(airport -> MapperUtils.airportToDisplay(airport,
                            AirportDisplay.calculateDistance(latitude,longitude,airport.getLatitude(),airport.getLongitude())))
                    .sorted(Comparator.comparingDouble(AirportDisplay::getDistance)).limit(limit).toList();
        }

        LOGGER.debug(String.format("fetching uncached get nearby airports for type: %s, airline: %s, latitude: %f, longitude: %f, with limit: %d",type.get(),airline.get(),latitude,longitude,limit));
        return airportRepository.findByIataIn(getActiveDeltaCodes()).stream().map(airport -> MapperUtils.airportToDisplay(airport,
                        AirportDisplay.calculateDistance(latitude,longitude,airport.getLatitude(),airport.getLongitude())))
                .sorted(Comparator.comparingDouble(AirportDisplay::getDistance)).limit(limit).toList();
    }

    @Override
    public AirportDisplay getByIata(String iata) {
        Optional<Airport> optional = airportRepository.findById(iata);
        if (optional.isEmpty()) {
            LOGGER.error(String.format("getByIata called with a nonexistent iata value = '%s'",iata));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"An internal service exception was thrown");
        }
        return MapperUtils.airportToDisplay(optional.get());
    }

    private boolean validDeltaCode(String iata) {
        return deltaService.exists(iata);
    }

    private List<String> getActiveDeltaCodes() {
        return deltaService.getActiveCodes();
    }
}
