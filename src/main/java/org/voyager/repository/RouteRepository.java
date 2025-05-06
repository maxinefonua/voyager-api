package org.voyager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.voyager.model.Airline;
import org.voyager.model.entity.Route;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route,Integer> {
    List<Route> findByOrigin(String origin);
    List<Route> findByDestination(String destination);
    List<Route> findByAirline(Airline airline);
    List<Route> findByOriginAndDestination(String origin, String destination);
    List<Route> findByOriginAndAirline(String origin, Airline airline);
    List<Route> findByDestinationAndAirline(String destination, Airline airline);
    Optional<Route> findByOriginAndDestinationAndAirline(String origin, String destination, Airline airline);
}
