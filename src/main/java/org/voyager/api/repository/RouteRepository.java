package org.voyager.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.voyager.api.model.entity.RouteEntity;
import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<RouteEntity,Integer> {
    Optional<RouteEntity> findByOriginAndDestination(String origin, String destination);

    @Query("SELECT r FROM RouteEntity r WHERE " +
            "(:origins IS NULL OR r.origin IN :origins) AND " +
            "(:destinations IS NULL OR r.destination IN :destinations) AND " +
            "(:excludeDestinations IS NULL OR r.destination NOT IN :excludeDestinations) AND " +
            "(:excludeRouteIds IS NULL OR r.id NOT IN :excludeRouteIds)")
    List<RouteEntity> findRoutes(
            @Param("origins") List<String> origins,
            @Param("destinations") List<String> destinations,
            @Param("excludeDestinations") List<String> excludeDestinations,
            @Param("excludeRouteIds") List<Integer> excludeRouteIds);
}
