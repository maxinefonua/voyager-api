package org.voyager.api.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.voyager.api.model.entity.AirlineAirportEntity;
import org.voyager.api.model.entity.AirlineAirportId;
import org.voyager.commons.model.airline.Airline;
import java.util.List;

public interface AdminAirlineAirportRepository extends JpaRepository<AirlineAirportEntity, AirlineAirportId> {
    int deleteByAirline(Airline airline);
    @Query("SELECT a FROM AirlineAirportEntity a WHERE a.airline = :airline AND a.iata IN :iataList")
    List<AirlineAirportEntity> findAllByAirlineAndIataIn(Airline airline, List<String> iataList);
}
