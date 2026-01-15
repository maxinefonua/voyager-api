package org.voyager.api.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.voyager.commons.model.airline.Airline;
import org.voyager.api.model.entity.AirlineEntity;

public interface AdminAirlineRepository extends JpaRepository<AirlineEntity,Airline> {
}
