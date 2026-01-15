package org.voyager.api.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.voyager.api.model.entity.AirportEntity;

public interface AdminAirportRepository extends JpaRepository<AirportEntity,String> {

}
