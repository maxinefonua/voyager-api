package org.voyager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.voyager.model.Airline;
import org.voyager.model.entity.Route;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route,Integer> {
    @Query("SELECT COUNT(*)>0 FROM Route r WHERE r.origin = ?1")
    Boolean originExists(String origin);
    @Query("SELECT COUNT(*)>0 FROM Route r WHERE r.destination = ?1")
    Boolean destinationExists(String destination);

    List<Route> findByIsActive(Boolean isActive);
    List<Route> findByOrigin(String origin);
    List<Route> findByOriginAndIsActive(String origin,Boolean isActive);
    List<Route> findByDestination(String destination);
    List<Route> findByDestinationAndIsActive(String destination,Boolean isActive);
    List<Route> findByAirline(Airline airline);
    List<Route> findByAirlineAndIsActive(Airline airline,Boolean isActive);
    List<Route> findByOriginAndDestination(String origin, String destination);
    List<Route> findByOriginAndDestinationAndIsActive(String origin, String destination,Boolean isActive);
    List<Route> findByOriginAndAirline(String origin, Airline airline);
    List<Route> findByOriginAndAirlineAndIsActive(String origin, Airline airline,Boolean isActive);
    List<Route> findByDestinationAndAirline(String destination, Airline airline);
    List<Route> findByDestinationAndAirlineAndIsActive(String destination, Airline airline,Boolean isActive);
    Optional<Route> findByOriginAndDestinationAndAirline(String origin, String destination, Airline airline);
    Optional<Route> findByOriginAndDestinationAndAirlineAndIsActive(String origin, String destination, Airline airline,Boolean isActive);
}
