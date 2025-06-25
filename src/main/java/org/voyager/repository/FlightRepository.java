package org.voyager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.voyager.model.Airline;
import org.voyager.model.entity.FlightEntity;

import java.util.List;

public interface FlightRepository  extends JpaRepository<FlightEntity,Integer> {
    List<FlightEntity> findByIsActive(Boolean isActive);
    List<FlightEntity> findByRouteId(Integer routeId);
    List<FlightEntity> findByRouteIdAndIsActive(Integer routeId,Boolean isActive);
    List<FlightEntity> findByRouteIdAndFlightNumber(Integer routeId, String flightNumber);
    List<FlightEntity> findByRouteIdAndFlightNumberAndIsActive(Integer routeId, String flightNumber,Boolean isActive);
    List<FlightEntity> findByFlightNumber(String flightNumber);
    List<FlightEntity> findByFlightNumberAndIsActive(String flightNumber,Boolean isActive);
    List<FlightEntity> findByAirline(Airline airline);
}
