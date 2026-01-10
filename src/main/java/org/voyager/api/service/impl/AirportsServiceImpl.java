package org.voyager.api.service.impl;

import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.api.error.MessageConstants;
import org.voyager.commons.model.airport.AirportForm;
import org.voyager.commons.model.airport.AirportPatch;
import org.voyager.api.model.entity.AirportEntity;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airport.Airport;
import org.voyager.commons.model.airport.AirportType;
import org.voyager.api.model.query.IataQuery;
import org.voyager.api.repository.AirlineAirportRepository;
import org.voyager.api.repository.AirportRepository;
import org.voyager.api.service.AirportsService;
import org.voyager.api.service.utils.MapperUtils;
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;
import java.util.Comparator;
import java.util.Optional;
import static org.voyager.api.service.utils.ServiceUtils.handleJPAExceptions;

@Service
public class AirportsServiceImpl implements AirportsService {
    @Autowired
    AirportRepository airportRepository;

    @Autowired
    AirlineAirportRepository airlineAirportRepository;

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
    public List<String> getIata(IataQuery iataQuery) {
        return handleJPAExceptions(() -> {
            List<Airline> airlineList = iataQuery.getAirlineList();
            Set<String> airlineIataCodes = null;
            if (airlineList != null) {
                airlineIataCodes = new HashSet<>(airlineAirportRepository
                        .selectDistinctIataCodesByAirlineIn(airlineList));
            }
            List<AirportType> airportTypeList = iataQuery.getAirportTypeList();
            Set<String> typeIataCodes = null;
            if (airportTypeList != null) {
                typeIataCodes = new HashSet<>(airportRepository
                        .selectIataByTypeIn(airportTypeList));
            }
            List<String> matches = new ArrayList<>();
            if (airlineIataCodes != null && typeIataCodes != null) {
                for (String iata : airlineIataCodes) {
                    if (typeIataCodes.contains(iata)) matches.add(iata);
                }
            } else if (airlineIataCodes != null)  {
                matches.addAll(airlineIataCodes);
            } else if (typeIataCodes != null){
                matches.addAll(typeIataCodes);
            } else {
                throw new IllegalStateException("one field must have been set");
            }
            return matches;
        });
    }

    @Override
    public List<Airport> getAll(Option<String> countryCode, List<AirportType> airportTypeList, List<Airline> airlineList) {
        return handleJPAExceptions(() -> {
            if (countryCode.isEmpty() && airportTypeList.isEmpty() && airlineList.isEmpty()) {
                LOGGER.debug("fetching non-cached get all airports");
                return airportRepository.findAll(Sort.by(Sort.Direction.ASC, "iata")).stream()
                        .map(MapperUtils::entityToAirport).toList();
            }
            if (countryCode.isEmpty() && airportTypeList.isEmpty()) {
                LOGGER.debug("fetching non-cached get airports by airlineList: {}", airlineList);
                return airportRepository.findByIataInOrderByIataAsc(
                        getActiveAirlineCodes(airlineList)).stream().map(MapperUtils::entityToAirport).toList();
            }
            if (countryCode.isEmpty() && airlineList.isEmpty()) {
                LOGGER.debug("fetching non-cached get airports by type: {}", airportTypeList);
                return airportRepository.findByTypeInOrderByIataAsc(airportTypeList).stream().map(MapperUtils::entityToAirport).toList();
            }
            if (airportTypeList.isEmpty() && airlineList.isEmpty()) {
                LOGGER.debug("fetching non-cached get airports by country code: {}", countryCode.get());
                return airportRepository.findByCountryCodeOrderByIataAsc(countryCode.get()).stream().map(MapperUtils::entityToAirport).toList();
            }
            if (airlineList.isEmpty()) {
                LOGGER.debug("fetching non-cached get airports by type: {} and country code: {}", airportTypeList, countryCode.get());
                return airportRepository.findByCountryCodeAndTypeInOrderByIataAsc(countryCode.get(), airportTypeList).stream().map(MapperUtils::entityToAirport).toList();
            }
            if (airportTypeList.isEmpty()) {
                LOGGER.debug("fetching non-cached get airports by country code: {} and airlineList: {}", countryCode.get(), airlineList);
                List<String> validAirlineAirports = getActiveAirlineCodes(airlineList);
                return airportRepository.findByCountryCodeOrderByIataAsc(countryCode.get()).stream()
                        .filter(airportEntity -> validAirlineAirports.contains(airportEntity.getIata())
                ).map(MapperUtils::entityToAirport).toList();
            }
            if (countryCode.isEmpty()) {
                LOGGER.debug("fetching non-cached get airports by type: {} and airlineList: {}", airportTypeList, airlineList);
                List<String> validAirlineAirports = getActiveAirlineCodes(airlineList);
                return airportRepository.findByTypeInOrderByIataAsc(airportTypeList).stream().filter(
                        airportEntity -> validAirlineAirports.contains(airportEntity.getIata())
                ).map(MapperUtils::entityToAirport).toList();
            }
            List<String> validAirlineAirports = getActiveAirlineCodes(airlineList);
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
                LOGGER.debug("fetching non-cached get nearby airports for latitude: {}, longitude: {}, with limit: {}", latitude, longitude, limit);
                return airportRepository.findByTypeInOrderByIataAsc(List.of(AirportType.CIVIL, AirportType.MILITARY)).stream().map(airportEntity -> MapperUtils.entityToAirport(airportEntity,
                                Airport.calculateDistanceKm(latitude, longitude, airportEntity.getLatitude(), airportEntity.getLongitude())))
                        .sorted(Comparator.comparingDouble(Airport::getDistance)).limit(limit).toList();
            } else if (airlineList.isEmpty()) {
                LOGGER.debug("fetching non-cached get nearby airports for type: {}, latitude: {}, longitude: {}, with limit: {}", airportTypeList, latitude, longitude, limit);
                return airportRepository.findByTypeInOrderByIataAsc(airportTypeList).stream().map(airportEntity -> MapperUtils.entityToAirport(airportEntity,
                                Airport.calculateDistanceKm(latitude, longitude, airportEntity.getLatitude(), airportEntity.getLongitude())))
                        .sorted(Comparator.comparingDouble(Airport::getDistance)).limit(limit).toList();
            } else if (airportTypeList.isEmpty()) {
                LOGGER.debug("fetching non-cached get nearby airports for airlines: {}, latitude: {}, longitude: {}, with limit: {}", airlineList, latitude, longitude, limit);
                return airportRepository.findByIataInOrderByIataAsc(getDistinctIataCodesForAirlineList(airlineList)).stream().map(airportEntity -> MapperUtils.entityToAirport(airportEntity,
                                Airport.calculateDistanceKm(latitude, longitude, airportEntity.getLatitude(), airportEntity.getLongitude())))
                        .sorted(Comparator.comparingDouble(Airport::getDistance)).limit(limit).toList();
            }

            LOGGER.debug("fetching non-cached get nearby airports for type: {}, airline: {}, latitude: {}, longitude: {}, with limit: {}", airportTypeList, airlineList, latitude, longitude, limit);
            return airportRepository.findByIataInOrderByIataAsc(getDistinctIataCodesForAirlineList(airlineList)).stream().map(airportEntity -> MapperUtils.entityToAirport(airportEntity,
                            Airport.calculateDistanceKm(latitude, longitude, airportEntity.getLatitude(), airportEntity.getLongitude())))
                    .sorted(Comparator.comparingDouble(Airport::getDistance)).limit(limit).toList();
        });
    }

    @Override
    public List<Airport> getNearbyAirport(@Validated String iata, int limit,
                                          List<AirportType> airportTypeList, List<Airline> airlineList) {
        return handleJPAExceptions(() -> {
            AirportEntity givenAirport = airportRepository.findById(iata).get();
            return getByDistance(givenAirport.getLatitude(), givenAirport.getLongitude(),
                    limit, airportTypeList,airlineList);
        });
    }

    @Override
    public Airport getByIata(String iata) {
        Optional<AirportEntity> optional =
                handleJPAExceptions(() -> airportRepository.findById(iata));
        if (optional.isEmpty()) {
            LOGGER.error("getByIata called with a nonexistent iata value = '{}'", iata);
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
    public Airport createAirport(@Validated AirportForm airportForm) {
        return handleJPAExceptions(()-> {
            if (airportRepository.existsById(airportForm.getIata())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,String.format(
                        "airport with code %s already exists", airportForm.getIata()));
            }
            return MapperUtils.entityToAirport(airportRepository.save(MapperUtils.formToAirportEntity(airportForm)));
                });
    }

    @Override
    public void deleteAirport(String iata) {
        handleJPAExceptions(()->airportRepository.deleteById(iata));
    }

    private List<String> getActiveAirlineCodes(List<Airline> airlineList) {
        return handleJPAExceptions(() ->
                airlineAirportRepository.selectDistinctIataCodesByAirlineInAndIsActive(airlineList,true));
    }

    private List<String> getDistinctIataCodesForAirlineList(List<Airline> airlineList) {
        return handleJPAExceptions(() ->
                airlineAirportRepository.selectDistinctIataCodesByAirlineInAndIsActive(airlineList,true));
    }
}
