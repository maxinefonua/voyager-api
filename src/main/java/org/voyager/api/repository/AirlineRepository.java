package org.voyager.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.voyager.commons.model.airline.Airline;
import org.voyager.api.model.entity.AirlineEntity;

public interface AirlineRepository extends JpaRepository<AirlineEntity,Airline> {
}
