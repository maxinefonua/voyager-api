package org.voyager.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.voyager.commons.model.airline.Airline;
import org.voyager.api.model.entity.AirlineAirportEntity;
import org.voyager.api.model.entity.AirlineAirportId;

import java.util.List;
import java.util.Optional;

public interface AirlineAirportRepository extends JpaRepository<AirlineAirportEntity, AirlineAirportId> {
    @Query("SELECT a.airline FROM AirlineAirportEntity a WHERE a.iata = ?1 AND a.isActive = ?2")
    List<Airline> selectAirlinesByIataAndIsActive(String iata, Boolean isActive);

    @Query("SELECT DISTINCT a.airline FROM AirlineAirportEntity a WHERE a.iata IN ?1 AND a.isActive = ?2")
    List<Airline> selectDistinctAirlinesByIataInAndIsActive(List<String> iataList, Boolean isActive);

    @Query("SELECT a.iata FROM AirlineAirportEntity a WHERE a.airline = ?1 AND a.isActive = ?2")
    List<String> selectIataCodesByAirlineAndIsActive(Airline airline, Boolean isActive);

    @Query("SELECT DISTINCT a.iata FROM AirlineAirportEntity a WHERE a.airline IN ?1 AND a.isActive = ?2")
    List<String> selectDistinctIataCodesByAirlineInAndIsActive(List<Airline> airlineList, Boolean isActive);

    @Query("SELECT DISTINCT a.iata FROM AirlineAirportEntity a WHERE a.airline IN ?1")
    List<String> selectDistinctIataCodesByAirlineIn(List<Airline> airlineList);

    Optional<AirlineAirportEntity> findByIataAndAirline(String iata, Airline airline);

    List<AirlineAirportEntity> findByAirline(Airline airline);
}
