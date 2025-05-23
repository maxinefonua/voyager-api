package org.voyager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.voyager.model.entity.AirportEntity;
import org.voyager.model.airport.AirportType;

import java.util.List;

public interface AirportRepository extends JpaRepository<AirportEntity,String> {
    @Query("SELECT a.iata FROM AirportEntity a ORDER BY iata")
    List<String> selectIata();

    @Query("SELECT a.iata FROM AirportEntity a WHERE type = ?1")
    List<String> selectIataByType(AirportType type);

    List<AirportEntity> findByIataIn(List<String> iataList);
    List<AirportEntity> findByCountryCode(String countryCode);
    List<AirportEntity> findByCountryCodeAndType(String countryCode, AirportType type);
    List<AirportEntity> findByType(AirportType airportType);
    List<AirportEntity> findByTypeIn(List<AirportType> typeList);
}