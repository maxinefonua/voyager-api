package org.voyager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.voyager.entity.Airport;
import org.voyager.model.AirportType;

import java.util.List;
import java.util.Set;

public interface AirportRepository extends JpaRepository<Airport,String> {
    @Query("SELECT a.iata FROM Airport a ORDER BY iata")
    List<String> selectIataOrderByIata();

    @Query("SELECT a.iata FROM Airport a WHERE type = ?1 ORDER BY iata")
    List<String> selectIataByMilitaryTypeOrderByIata(AirportType type);

    List<Airport> findByIataIn(List<String> iataList);
    List<Airport> findByIataInOrderByIataAsc(List<String> iataList);
    List<Airport> findByCountryCodeOrderByIataAsc(String countryCode);
    List<Airport> findByCountryCodeAndTypeOrderByIataAsc(String countryCode, AirportType type);
    List<Airport> findByTypeOrderByIataAsc(AirportType airportType);
    List<Airport> findByType(AirportType airportType);
    List<Airport> findByTypeIn(List<AirportType> typeList);
    List<Airport> findByTypeInOrderByIataAsc(List<AirportType> typeList);
}