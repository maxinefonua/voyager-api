package org.voyager.api.repository.primary;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.voyager.commons.model.airline.Airline;
import org.voyager.api.model.entity.FlightEntity;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface FlightRepository  extends JpaRepository<FlightEntity,Integer> {
    boolean existsByFlightNumber(String flightNumber);
    Optional<FlightEntity> findByRouteIdAndFlightNumberAndZonedDateTimeDeparture(
            Integer routeId, String flightNumber, ZonedDateTime departure);
    Optional<FlightEntity> findByRouteIdAndFlightNumberAndZonedDateTimeArrival(
            Integer routeId, String flightNumber, ZonedDateTime arrival);

    List<FlightEntity> findByRouteIdAndFlightNumber(Integer routeId, String flightNumber);
    // For flights arriving AFTER a certain time
    @Query(value = """
    SELECT f.* FROM flights f 
    WHERE f.route_id = :routeId 
    AND f.flight_no = :flightNumber 
    AND f.arrival_zdt > :departure
    ORDER BY ABS(EXTRACT(EPOCH FROM (:departure - f.arrival_zdt))) 
    LIMIT 1
    """, nativeQuery = true)
    Optional<FlightEntity> findClosestArrivalAfterDeparture(
            @Param("routeId") Integer routeId,
            @Param("flightNumber") String flightNumber,
            @Param("departure") ZonedDateTime departure);

    // For flights arriving BEFORE a certain time
    @Query(value = """
    SELECT f.* FROM flights f 
    WHERE f.route_id = :routeId 
    AND f.flight_no = :flightNumber 
    AND f.departure_zdt < :arrival
    ORDER BY ABS(EXTRACT(EPOCH FROM (:arrival - f.departure_zdt))) 
    LIMIT 1
    """, nativeQuery = true)
    Optional<FlightEntity> findClosestDepartureBeforeArrival(
            @Param("routeId") Integer routeId,
            @Param("flightNumber") String flightNumber,
            @Param("arrival") ZonedDateTime arrival);

    @Query(value = """
    SELECT f FROM FlightEntity f 
    WHERE (:routeIds IS NULL OR f.routeId IN :routeIds) 
    AND (:airlineList IS NULL OR f.airline IN :airlineList) 
    AND (:flightNumber IS NULL OR f.flightNumber = :flightNumber) 
    AND (:isActive IS NULL OR f.isActive = :isActive) 
    AND f.zonedDateTimeDeparture BETWEEN :startTime AND :endTime
    """,
            countQuery = """
    SELECT COUNT(f) FROM FlightEntity f 
    WHERE (:routeIds IS NULL OR f.routeId IN :routeIds) 
    AND (:airlineList IS NULL OR f.airline IN :airlineList) 
    AND (:flightNumber IS NULL OR f.flightNumber = :flightNumber) 
    AND (:isActive IS NULL OR f.isActive = :isActive) 
    AND f.zonedDateTimeDeparture BETWEEN :startTime AND :endTime
    """)
    Page<FlightEntity> findFlightsDynamic(
            @Param("routeIds") List<Integer> routeIds,
            @Param("airlineList") List<Airline> airlineList,
            @Param("flightNumber") String flightNumber,
            @Param("isActive") Boolean isActive,
            @Param("startTime") ZonedDateTime startTime,
            @Param("endTime") ZonedDateTime endTime,
            Pageable pageable);

    @Query("SELECT f FROM FlightEntity f WHERE " +
            "(:routeIds IS NULL OR f.routeId IN :routeIds) AND " +
            "(:airlineList IS NULL OR f.airline IN :airlineList) AND " +
            "(:flightNumber IS NULL OR f.flightNumber = :flightNumber) AND " +
            "(:isActive IS NULL OR f.isActive = :isActive) AND " +
            "f.zonedDateTimeDeparture BETWEEN :startTime AND :endTime")
    List<FlightEntity> findFlightsDynamic(
            @Param("routeIds") List<Integer> routeIds,
            @Param("airlineList") List<Airline> airlineList,
            @Param("flightNumber") String flightNumber,
            @Param("isActive") Boolean isActive,
            @Param("startTime") ZonedDateTime startTime,
            @Param("endTime") ZonedDateTime endTime);

    Optional<FlightEntity> findByRouteIdAndFlightNumberAndZonedDateTimeDepartureBetween(
            Integer routeId,
            String flightNumber,
            ZonedDateTime startTime,
            ZonedDateTime endTime
    );

    @Query(value = "SELECT DISTINCT r.orgn AS airport_code " +
            "FROM routes r INNER JOIN flights f ON f.route_id = r.id " +
            "WHERE f.active = true AND f.airline IN (:airlines) " +
            "UNION " +
            "SELECT DISTINCT r.dstn " +
            "FROM routes r INNER JOIN flights f ON f.route_id = r.id " +
            "WHERE f.active = true AND f.airline IN (:airlines) " +
            "ORDER BY airport_code ASC", nativeQuery = true)
    List<String> findDistinctAirportsWithAirlineIn(@Param("airlines") List<String> airlineStringList);

    @Query(value = "SELECT DISTINCT f.airline FROM public.flights f " +
            "JOIN public.routes r ON f.route_id = r.id " +
            "WHERE f.active = true " +
            "AND (r.orgn IN (:iataList) OR r.dstn IN (:iataList))",
            nativeQuery = true)
    List<Airline> findDistinctAirlinesForAnyIataIn(@Param("iataList") List<String> iataList);

    @Query(value = "WITH airport_airlines AS (" +
            "  SELECT f.airline, r.orgn AS airport_code FROM public.flights f " +
            "  JOIN public.routes r ON f.route_id = r.id " +
            "  WHERE f.active = true AND r.orgn IN (:iataList) " +
            "  UNION " +
            "  SELECT f.airline, r.dstn AS airport_code FROM public.flights f " +
            "  JOIN public.routes r ON f.route_id = r.id " +
            "  WHERE f.active = true AND r.dstn IN (:iataList)" +
            ") " +
            "SELECT DISTINCT airline FROM airport_airlines aa " +
            "GROUP BY airline " +
            "HAVING COUNT(DISTINCT airport_code) = :iataListSize",
            nativeQuery = true)
    List<Airline> findDistinctAirlinesForAllIataIn(
            @Param("iataList") List<String> iataList,
            @Param("iataListSize") int iataListSize
    );

    @Query(value = """
    SELECT DISTINCT f.airline
    FROM public.flights f
    JOIN public.routes r ON f.route_id = r.id
    WHERE f.active = true
      AND r.orgn IN (:originList)
    INTERSECT
    SELECT DISTINCT f.airline
    FROM public.flights f
    JOIN public.routes r ON f.route_id = r.id
    WHERE f.active = true
      AND r.dstn IN (:destinationList)
    """,
            nativeQuery = true)
    List<Airline> findDistinctAirlinesWithOriginInAndDestinationIn(
            @Param("originList") List<String> originList,
            @Param("destinationList") List<String> destinationList);
}
