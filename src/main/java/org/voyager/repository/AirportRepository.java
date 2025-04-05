package org.voyager.repository;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.voyager.entity.Airport;
import org.voyager.model.AirportType;

import java.util.List;

public interface AirportRepository extends JpaRepository<Airport,String> {
    @Query("SELECT a.iata FROM Airport a ORDER BY iata")
    List<String> selectIataOrderByIata();

    @Query("SELECT a.iata FROM Airport a WHERE type = ?1 ORDER BY iata")
    List<String> selectIataByMilitaryTypeOrderByIata(AirportType type);

    List<Airport> findAll();
    List<Airport> findByCountryCodeOrderByIataAsc(String countryCode);
    List<Airport> findByCountryCodeAndTypeOrderByIataAsc(String countryCode, AirportType type);
    List<Airport> findByTypeOrderByIataAsc(AirportType airportType);
    List<Airport> findByCountryCodeOrderByIataAsc(String countryCode, Limit limit);
    Airport saveAndFlush(Airport airport);
}