package org.voyager.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.voyager.commons.model.airline.Airline;
import org.voyager.api.model.entity.FlightEntity;
import org.voyager.commons.model.flight.Flight;

import javax.swing.text.html.Option;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface FlightRepository  extends JpaRepository<FlightEntity,Integer> {
    @Query("SELECT DISTINCT f.airline FROM FlightEntity f WHERE f.routeId = ?1 and f.isActive = ?2")
    List<Airline> selectDistinctAirlineByRouteIdAndIsActive(Integer routeId, Boolean isActive);

    @Query("SELECT DISTINCT f.routeId FROM FlightEntity f WHERE f.airline = ?1 and f.isActive = ?2")
    List<Integer> selectDistinctRouteIdByAirlineAndIsActive(Airline airline, Boolean isActive);

    boolean existsByFlightNumber(String flightNumber);
    Optional<FlightEntity> findByRouteIdAndFlightNumber(Integer routeId, String flightNumber);
    Optional<FlightEntity> findByRouteIdAndFlightNumberAndZonedDateTimeDeparture(Integer routeId, String flightNumber,
                                                                                 ZonedDateTime departure);
    Optional<FlightEntity> findByRouteIdAndFlightNumberAndZonedDateTimeArrival(Integer routeId, String flightNumber,
                                                                               ZonedDateTime arrival);
    Optional<FlightEntity> findByRouteIdAndFlightNumberAndZonedDateTimeDepartureAndZonedDateTimeArrival(
            Integer routeId, String flightNumber,
            ZonedDateTime departure, ZonedDateTime arrival);

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

    // newly added
    Page<FlightEntity> findByRouteIdInAndFlightNumberAndIsActiveAndZonedDateTimeDepartureBetween(
            List<Integer> routeIdList, String flightNumber, Boolean isActive, ZonedDateTime start, ZonedDateTime end,
            Pageable pageable);

    Page<FlightEntity> findByFlightNumberAndIsActiveAndZonedDateTimeDepartureBetween(
            String flightNumber, Boolean isActive,ZonedDateTime start,ZonedDateTime end, Pageable pageable);

    Page<FlightEntity> findByFlightNumberAndZonedDateTimeDepartureBetween(
            String flightNumber,ZonedDateTime start,ZonedDateTime end,Pageable pageable);

    Page<FlightEntity> findByRouteIdInAndAirlineAndIsActiveAndZonedDateTimeDepartureBetween(
            List<Integer> routeIdList, Airline airline, Boolean isActive, ZonedDateTime start, ZonedDateTime end,
            Pageable pageable);
    Page<FlightEntity> findByAirlineAndIsActiveAndZonedDateTimeDepartureBetween(
            Airline airline, Boolean isActive, ZonedDateTime start, ZonedDateTime end, Pageable pageable);
    Page<FlightEntity> findByAirlineAndZonedDateTimeDepartureBetween(
            Airline airline,ZonedDateTime start, ZonedDateTime end, Pageable pageable);
    Page<FlightEntity> findByRouteIdInAndIsActiveAndZonedDateTimeDepartureBetween(
            List<Integer> routeIdList, Boolean isActive, ZonedDateTime start, ZonedDateTime end, Pageable pageable);
    Page<FlightEntity> findByIsActiveAndZonedDateTimeDepartureBetween(
            Boolean isActive, ZonedDateTime start, ZonedDateTime end, Pageable pageable);


    Page<FlightEntity> findByZonedDateTimeDepartureBetween(ZonedDateTime start, ZonedDateTime end, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(DISTINCT f.routeId) = :requiredCount THEN true ELSE false END FROM FlightEntity f WHERE f.routeId IN :routeIds AND f.airline = :airline")
    boolean hasMatchingDistinctRouteCount(
            @Param("requiredCount") long requiredCount,
            @Param("routeIds") List<Integer> routeIds,
            @Param("airline") Airline airline);

    @Query("SELECT CASE WHEN COUNT(DISTINCT f.routeId) = :requiredCount THEN true ELSE false END " +
            "FROM FlightEntity f WHERE f.routeId IN :routeIds AND f.airline = :airline " +
            "AND f.zonedDateTimeDeparture >= :startTime AND f.zonedDateTimeDeparture <= :endTime")
    boolean hasMatchingDistinctRouteCountWithDepartureBetween(
            @Param("requiredCount") long requiredCount,
            @Param("routeIds") List<Integer> routeIds,
            @Param("airline") Airline airline,
            @Param("startTime") ZonedDateTime startTime,
            @Param("endTime") ZonedDateTime endTime);

    @Query("SELECT CASE WHEN EXISTS (" +
            "SELECT 1 FROM FlightEntity f " +
            "WHERE f.routeId IN :routeIds " +
            "AND f.airline IN :airlines " +
            "AND f.zonedDateTimeDeparture BETWEEN :startTime AND :endTime " +
            "GROUP BY f.airline " +
            "HAVING COUNT(DISTINCT f.routeId) = :requiredCount" +
            ") THEN true ELSE false END")
    boolean existsAnyAirlineWithMatchingRouteCount(
            @Param("requiredCount") long requiredCount,
            @Param("routeIds") List<Integer> routeIds,
            @Param("airlines") List<Airline> airlines,
            @Param("startTime") ZonedDateTime startTime,
            @Param("endTime") ZonedDateTime endTime);

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

    Optional<FlightEntity> findByRouteIdAndFlightNumberAndZonedDateTimeArrivalAfter(
            Integer routeId, String flightNumber, ZonedDateTime departure);

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

    Optional<FlightEntity> findByRouteIdAndFlightNumberAndZonedDateTimeDepartureBefore(
            Integer routeId, String flightNumber, ZonedDateTime arrival);


    @Query("SELECT f FROM FlightEntity f WHERE " +
            "(:routeIds IS NULL OR f.routeId IN :routeIds) AND " +
            "(:airlineList IS NULL OR f.airline IN :airlineList) AND " +
            "(:flightNumber IS NULL OR f.flightNumber = :flightNumber) AND " +
            "(:isActive IS NULL OR f.isActive = :isActive) AND " +
            "f.zonedDateTimeDeparture BETWEEN :startTime AND :endTime")
    Page<FlightEntity> findFlightsDynamic(
            @Param("routeIds") List<Integer> routeIds,
            @Param("airlineList") List<Airline> airlineList,
            @Param("flightNumber") String flightNumber,
            @Param("isActive") Boolean isActive,
            @Param("startTime") ZonedDateTime startTime,
            @Param("endTime") ZonedDateTime endTime,
            Pageable pageable
    );

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
}
