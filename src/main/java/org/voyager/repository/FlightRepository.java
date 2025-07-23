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

    List<FlightEntity> findByRouteIdInAndFlightNumberAndIsActive(List<Integer> routeIdList, String flightNumber, Boolean isActive);
    List<FlightEntity> findByRouteIdInAndAirlineAndIsActive(List<Integer> routeIdList, Airline airline, Boolean isActive);

    List<FlightEntity> findByRouteIdInAndFlightNumber(List<Integer> routeIdList, String flightNumber);
    List<FlightEntity> findByRouteIdInAndAirline(List<Integer> routeIdList, Airline airline);
    List<FlightEntity> findByRouteIdInAndIsActive(List<Integer> routeIdList, Boolean isActive);
    List<FlightEntity> findByFlightNumberAndIsActive(String flightNumber, Boolean isActive);
    List<FlightEntity> findByAirlineAndIsActive(Airline airline, Boolean isActive);

    List<FlightEntity> findByRouteIdIn(List<Integer> routeIdList);
    List<FlightEntity> findByFlightNumber(String flightNumber);
    List<FlightEntity> findByAirline(Airline airline);
    List<FlightEntity> findByIsActive(Boolean isActive);
}
