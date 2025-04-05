package org.voyager.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.voyager.entity.Airport;
import org.voyager.model.AirportDisplay;
import org.voyager.model.AirportType;
import org.voyager.repository.AirportRepository;
import org.voyager.service.AirportsService;
import org.voyager.service.utils.MapperUtils;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class AirportsServiceImpl implements AirportsService<AirportDisplay> {
    @Autowired
    AirportRepository airportRepository;

    public Optional<AirportDisplay> updateAirport(AirportDisplay airportDisplay) {
        Optional<Airport> dbOptional = airportRepository.findById(airportDisplay.getIata());
        if (dbOptional.isEmpty()) return Optional.empty();
        return Optional.empty();
    }

    @Override
    public List<String> getIata() {
        return airportRepository.selectIataOrderByIata();
    }

    @Override
    public List<String> getIataByType(AirportType airportType) {
        return airportRepository.selectIataByMilitaryTypeOrderByIata(airportType);
    }

    @Override
    public List<AirportDisplay> getAll() {
        return airportRepository.findAll(Sort.by(Sort.Direction.ASC, "iata")).stream().map(MapperUtils::airportToDisplay).toList();
    }

    @Override
    public List<AirportDisplay> getByCountryCode(String countryCode, int limit) {
        return airportRepository.findByCountryCodeOrderByIataAsc(countryCode, Limit.of(limit)).stream().map(MapperUtils::airportToDisplay).toList();
    }

    @Override
    public List<AirportDisplay> getByCountryCode(String countryCode) {
        return airportRepository.findByCountryCodeOrderByIataAsc(countryCode).stream().map(MapperUtils::airportToDisplay).toList();
    }

    @Override
    public List<AirportDisplay> getByTypeSortedByDistance(double latitude, double longitude, AirportType type, int limit) {
        return airportRepository.findByTypeOrderByIataAsc(type).stream().map(airport -> MapperUtils.airportToDisplay(airport,
                        AirportDisplay.calculateDistance(latitude,longitude,airport.getLatitude(),airport.getLongitude())))
                .sorted(Comparator.comparingDouble(AirportDisplay::getDistance)).limit(limit).toList();
    }

    @Override
    public Optional<AirportDisplay> getByIata(String iata) {
        Optional<Airport> optional = airportRepository.findById(iata);
        if (optional.isEmpty()) return Optional.empty();
        return optional.map(MapperUtils::airportToDisplay);
    }

    @Override
    public List<AirportDisplay> get(String countryCode, AirportType type) {
        if (countryCode == null && type == null) {
            System.out.println("fetching uncached get airports");
            return airportRepository.findAll(Sort.by(Sort.Direction.ASC, "iata")).stream().map(MapperUtils::airportToDisplay).toList();
        } else if (type == null) {
            System.out.println("fetching uncached get airports by country code: "+ countryCode);
            return airportRepository.findByCountryCodeOrderByIataAsc(countryCode).stream().map(MapperUtils::airportToDisplay).toList();
        } else if (countryCode == null) {
            System.out.println("fetching uncached get airports by military type: "+type);
            return airportRepository.findByTypeOrderByIataAsc(type).stream().map(MapperUtils::airportToDisplay).toList();
        } else {
            System.out.println("fetching uncached get airports by country code: " + countryCode + " and military type: "+type);
            return airportRepository.findByCountryCodeAndTypeOrderByIataAsc(countryCode,type).stream().map(MapperUtils::airportToDisplay).toList();
        }
    }
}
