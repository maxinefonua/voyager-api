package org.voyager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.voyager.model.Airline;
import org.voyager.model.entity.FlightEntity;

import java.util.List;

public interface FlightRepository  extends JpaRepository<FlightEntity,Integer> {
    @Query("SELECT DISTINCT f.airline FROM FlightEntity f WHERE f.routeId = ?1 and f.isActive = ?2")
    List<Airline> selectDistinctAirlineByRouteIdAndIsActive(Integer routeId, Boolean isActive);

    @Query("SELECT DISTINCT f.routeId FROM FlightEntity f WHERE f.airline = ?1 and f.isActive = ?2")
    List<Integer> selectDistinctRouteIdByAirlineAndIsActive(Airline airline, Boolean isActive);

    List<FlightEntity> findByRouteIdAndFlightNumberAndIsActive(Integer routeId, String flightNumber, Boolean isActive);
    List<FlightEntity> findByRouteIdAndAirlineAndIsActive(Integer routeId, Airline airline, Boolean isActive);

    List<FlightEntity> findByRouteIdAndFlightNumber(Integer routeId, String flightNumber);
    List<FlightEntity> findByRouteIdAndAirline(Integer routeId, Airline airline);
    List<FlightEntity> findByRouteIdAndIsActive(Integer routeId, Boolean isActive);
    List<FlightEntity> findByFlightNumberAndIsActive(String flightNumber, Boolean isActive);
    List<FlightEntity> findByAirlineAndIsActive(Airline airline, Boolean isActive);

    List<FlightEntity> findByRouteId(Integer routeId);
    List<FlightEntity> findByFlightNumber(String flightNumber);
    List<FlightEntity> findByAirline(Airline airline);
    List<FlightEntity> findByIsActive(Boolean isActive);
}
