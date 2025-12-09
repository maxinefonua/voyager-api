package org.voyager.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
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

    @Modifying
    @Query("DELETE FROM FlightEntity f WHERE f.airline = :airline AND f.isActive = :isActive")
    int deleteByAirlineAndIsActive(@Param("airline") Airline airline,
                                   @Param("isActive") Boolean isActive);

    @Modifying
    @Query("DELETE FROM FlightEntity f WHERE f.isActive = :isActive")
    int deleteByIsActive(@Param("isActive") Boolean isActive);

    @Modifying
    @Query("DELETE FROM FlightEntity f WHERE f.airline = :airline")
    int deleteByAirline(Airline airline);

    @Modifying
    @Query("DELETE FROM FlightEntity f WHERE f.zonedDateTimeArrival < :cutoffTime")
    int deleteByZonedDateTimeArrivalBefore(@Param("cutoffTime") ZonedDateTime cutoffTime);

    @Modifying
    @Query("DELETE FROM FlightEntity f WHERE f.zonedDateTimeArrival < :cutoffTime AND f.airline = :airline")
    int deleteByZonedDateTimeArrivalBeforeAndAirline(@Param("cutoffTime") ZonedDateTime cutoffTime, @Param("airline") Airline airline);

    @Modifying
    @Query("DELETE FROM FlightEntity f WHERE f.zonedDateTimeArrival < :cutoffTime AND f.isActive = :isActive")
    int deleteByZonedDateTimeArrivalBeforeAndIsActive(@Param("cutoffTime") ZonedDateTime cutoffTime,
                                                      @Param("isActive") Boolean isActive);
    @Modifying
    @Query("DELETE FROM FlightEntity f WHERE f.zonedDateTimeArrival < :cutoffTime AND f.airline = :airline AND f.isActive = :isActive")
    int deleteByZonedDateTimeArrivalBeforeAndAirlineAndIsActive(@Param("cutoffTime") ZonedDateTime cutoffTime,
                                                                @Param("airline") Airline airline,
                                                                @Param("isActive") Boolean isActive);

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

//    @Query("SELECT f FROM FlightEntity f WHERE " +
//            "(:routeIds IS NULL OR f.routeId IN :routeIds) AND " +
//            "(:airlineList IS NULL OR f.airline IN :airlineList) AND " +
//            "(:flightNumber IS NULL OR f.flightNumber = :flightNumber) AND " +
//            "(:isActive IS NULL OR f.isActive = :isActive) AND " +
//            "f.zonedDateTimeDeparture BETWEEN :startTime AND :endTime")
//    Page<FlightEntity> findFlightsDynamic(
//            @Param("routeIds") List<Integer> routeIds,
//            @Param("airlineList") List<Airline> airlineList,
//            @Param("flightNumber") String flightNumber,
//            @Param("isActive") Boolean isActive,
//            @Param("startTime") ZonedDateTime startTime,
//            @Param("endTime") ZonedDateTime endTime,
//            Pageable pageable
//    );

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
}
