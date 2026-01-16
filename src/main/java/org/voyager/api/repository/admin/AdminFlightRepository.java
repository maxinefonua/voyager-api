package org.voyager.api.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.voyager.api.model.entity.FlightEntity;
import org.voyager.commons.model.airline.Airline;

import java.time.ZonedDateTime;
import java.util.List;

public interface AdminFlightRepository extends JpaRepository<FlightEntity,Integer> {
    List<FlightEntity> findByRouteIdAndFlightNumber(Integer routeId, String flightNumber);

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

}
