package org.voyager.api.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.voyager.api.model.entity.RouteEntity;

public interface AdminRouteRepository extends JpaRepository<RouteEntity,Integer> {

}
