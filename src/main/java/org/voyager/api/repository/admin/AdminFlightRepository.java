package org.voyager.api.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.voyager.api.model.entity.FlightEntity;
import java.util.List;

public interface AdminFlightRepository extends JpaRepository<FlightEntity,Integer> {
    List<FlightEntity> findByRouteIdAndFlightNumber(Integer routeId, String flightNumber);
}
