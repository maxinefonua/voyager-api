package org.voyager.api.repository.tests;

import org.springframework.data.jpa.repository.JpaRepository;
import org.voyager.api.model.entity.AirportEntity;

public interface TestsAirportRepository extends JpaRepository<AirportEntity,String> {
}
