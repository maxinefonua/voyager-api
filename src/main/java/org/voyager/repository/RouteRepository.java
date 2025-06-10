package org.voyager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.voyager.model.Airline;
import org.voyager.model.entity.RouteEntity;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<RouteEntity,Integer> {
    @Query("SELECT COUNT(*)>0 FROM RouteEntity r WHERE r.origin = ?1")
    Boolean originExists(String origin);
    @Query("SELECT COUNT(*)>0 FROM RouteEntity r WHERE r.destination = ?1")
    Boolean destinationExists(String destination);

    List<RouteEntity> findByIsActive(Boolean isActive);
    List<RouteEntity> findByOrigin(String origin);
    List<RouteEntity> findByOriginAndIsActive(String origin, Boolean isActive);
    List<RouteEntity> findByDestination(String destination);
    List<RouteEntity> findByDestinationAndIsActive(String destination, Boolean isActive);
    List<RouteEntity> findByAirline(Airline airline);
    List<RouteEntity> findByAirlineAndIsActive(Airline airline, Boolean isActive);
    List<RouteEntity> findByOriginAndDestination(String origin, String destination);
    List<RouteEntity> findByOriginAndDestinationAndIsActive(String origin, String destination, Boolean isActive);
    List<RouteEntity> findByOriginAndAirline(String origin, Airline airline);
    List<RouteEntity> findByOriginAndAirlineAndIsActive(String origin, Airline airline, Boolean isActive);
    List<RouteEntity> findByDestinationAndAirline(String destination, Airline airline);
    List<RouteEntity> findByDestinationAndAirlineAndIsActive(String destination, Airline airline, Boolean isActive);
    Optional<RouteEntity> findByOriginAndDestinationAndAirline(String origin, String destination, Airline airline);
    Optional<RouteEntity> findByOriginAndDestinationAndAirlineAndIsActive(String origin, String destination, Airline airline, Boolean isActive);
}
