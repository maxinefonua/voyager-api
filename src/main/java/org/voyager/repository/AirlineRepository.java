package org.voyager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.voyager.model.Airline;
import org.voyager.model.entity.AirlineAirportEntity;

import java.util.List;

public interface AirlineRepository extends JpaRepository<AirlineAirportEntity,String> {
    @Query("SELECT a.airline FROM AirlineAirportEntity a WHERE a.iata = ?1 AND a.isActive = ?2")
    List<Airline> selectAirlinesByIataAndIsActive(String iata, Boolean isActive);

    @Query("SELECT a.iata FROM AirlineAirportEntity a WHERE a.airline = ?1 AND a.isActive = ?2")
    List<String> selectIataCodesByAirlineAndIsActive(Airline airline, Boolean isActive);
}
