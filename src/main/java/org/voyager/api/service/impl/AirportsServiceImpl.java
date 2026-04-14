package org.voyager.api.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.api.repository.admin.AdminAirportRepository;
import org.voyager.api.repository.primary.FlightRepository;
import org.voyager.api.repository.tests.TestsAirportRepository;
import org.voyager.commons.model.airport.*;
import org.voyager.api.model.entity.AirportEntity;
import org.voyager.commons.model.airline.Airline;
import org.voyager.api.model.query.IataQuery;
import org.voyager.api.repository.primary.AirportRepository;
import org.voyager.api.service.AirportsService;
import org.voyager.api.service.utils.MapperUtils;
import org.voyager.commons.model.response.PagedResponse;
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
    AdminAirportRepository adminAirportRepository;

    @Autowired
    TestsAirportRepository testsAirportRepository;

    @Autowired
    FlightRepository flightRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final Logger LOGGER = LoggerFactory.getLogger(AirportsServiceImpl.class);

    @Override
    public Boolean ifIataExists(String iata) {
        return airportRepository.existsById(iata);
    }

    @Override
    public List<String> getIata() {
        return airportRepository.selectIata();
    }

    @Override
    public List<String> getIata(IataQuery iataQuery) {
        List<Airline> airlineList = iataQuery.getAirlineList();
        Set<String> airlineIataCodes = null;
        if (airlineList != null && !airlineList.isEmpty()) {
            airlineIataCodes = new HashSet<>(
                    flightRepository.findDistinctAirportsWithAirlineIn(
                            airlineList.stream().map(Airline::name).toList()));
        }
        List<AirportType> airportTypeList = iataQuery.getAirportTypeList();
        Set<String> typeIataCodes = null;
        if (airportTypeList != null) {
            typeIataCodes = new HashSet<>(airportRepository.selectIataByTypeIn(airportTypeList));
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
        return matches.stream().sorted().toList();
    }

    @Override
    public PagedResponse<Airport> getPagedAirports(AirportQuery airportQuery) {
        Pageable pageable = Pageable.ofSize(airportQuery.getSize())
                .withPage(airportQuery.getPage());
        Page<AirportEntity> airportEntityPage = fetchAirportEntities(airportQuery,pageable);
        List<Airport> content = airportEntityPage.get().map(MapperUtils::entityToAirport).toList();
        return PagedResponse.<Airport>builder()
                .content(content)
                .page(airportQuery.getPage())
                .size(airportQuery.getSize())
                .totalElements(airportEntityPage.getTotalElements())
                .totalPages(airportEntityPage.getTotalPages())
                .first(airportEntityPage.isFirst())
                .last(airportEntityPage.isLast())
                .numberOfElements(content.size())
                .build();
    }

    private Page<AirportEntity> fetchAirportEntities(AirportQuery airportQuery, Pageable pageable) {
        List<String> airportTypeStringList = getTypeStringList(airportQuery.getAirportTypeList());
        List<String> airlineStringList = getAirlineStringList(airportQuery.getAirlineList());
        String countryCode = airportQuery.getCountryCode();
        if (airportTypeStringList != null && airlineStringList != null && countryCode != null) {
            return airportRepository.findAirportsByAirlinesAndTypesAndCountry(
                    airlineStringList,airportTypeStringList,countryCode,pageable);
        }
        if (airportTypeStringList != null && airlineStringList != null) {
            return airportRepository.findAirportsByAirlinesAndTypes(airlineStringList,airportTypeStringList,pageable);
        }
        if (airlineStringList != null && countryCode != null) {
            return airportRepository.findAirportsByAirlinesAndCountry(airlineStringList,countryCode,pageable);
        }
        if (airlineStringList != null) {
            return airportRepository.findAirportsByAirlinesOnly(airlineStringList,pageable);
        }
        return airportRepository.findAirportsDynamicWithoutAirlines(
                airportQuery.getAirportTypeList(),countryCode,pageable);
    }

    private List<String> getAirlineStringList(List<Airline> airlineList) {
        if (airlineList == null || airlineList.isEmpty()) return null;
        return airlineList.stream().map(Airline::name).toList();
    }

    private List<String> getTypeStringList(List<AirportType> airportTypeList) {
        if (airportTypeList == null || airportTypeList.isEmpty()) return null;
        return airportTypeList.stream().map(AirportType::name).toList();
    }

    @Override
    public List<Airport> getByDistance(double latitude, double longitude,
                                       int limit, List<AirportType> airportTypeList, List<Airline> airlineList) {
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
    }

    @Override
    public List<Airport> getNearbyAirport(@Validated String iata, int limit,
                                          List<AirportType> airportTypeList, List<Airline> airlineList) {
        AirportEntity givenAirport = airportRepository.findById(iata).get();
        return getByDistance(givenAirport.getLatitude(), givenAirport.getLongitude(),
                limit, airportTypeList,airlineList);
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
    @Transactional("adminTransactionManager")
    public Airport patch(String iata, AirportPatch airportPatch) {
        return handleJPAExceptions(()->{
            AirportEntity existing = adminAirportRepository.findById(iata).get();
            if (StringUtils.isNotBlank(airportPatch.getName()))
                existing.setName(airportPatch.getName());
            if (StringUtils.isNotBlank(airportPatch.getCity()))
                existing.setCity(airportPatch.getCity());
            if (StringUtils.isNotBlank(airportPatch.getSubdivision()))
                existing.setSubdivision(airportPatch.getSubdivision());
            if (StringUtils.isNotBlank(airportPatch.getType()))
                existing.setType(AirportType.valueOf(airportPatch.getType().toUpperCase()));
            AirportEntity modified = existing.toBuilder().build();
            existing = adminAirportRepository.save(modified);
            return MapperUtils.entityToAirport(existing);
        });
    }

    @Override
    @Transactional("adminTransactionManager")
    public Airport createAirport(@Validated AirportForm airportForm) {
        return handleJPAExceptions(()-> {
            if (adminAirportRepository.existsById(airportForm.getIata())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,String.format(
                        "airport with code %s already exists", airportForm.getIata()));
            }
            return MapperUtils.entityToAirport(adminAirportRepository.save(MapperUtils.formToAirportEntity(airportForm)));
                });
    }

    @Override
    @Transactional("testsTransactionManager")
    public void deleteAirport(String iata) {
        handleJPAExceptions(()->{
            testsAirportRepository.deleteById(iata);
        });
    }

    private List<String> getDistinctIataCodesForAirlineList(List<Airline> airlineList) {
        return handleJPAExceptions(() ->
                flightRepository.findDistinctAirportsWithAirlineIn(airlineList.stream().map(Airline::name).toList()));
    }
}
