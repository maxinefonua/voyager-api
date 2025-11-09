package org.voyager.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.validation.annotation.Validated;
import org.voyager.api.model.entity.RouteEntity;
import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<RouteEntity,Integer> {
    boolean existsByOrigin(String origin);
    boolean existsByDestination(String destination);
    Optional<RouteEntity> findByOriginAndDestination(String origin, String destination);

    List<RouteEntity> findByIdIn(List<Integer> routeIdList);
    List<RouteEntity> findByOrigin(String origin);
    List<RouteEntity> findByOriginAndIdIn(String origin, List<Integer> routeIdList);
    List<RouteEntity> findByDestination(String destination);
    List<RouteEntity> findByDestinationAndIdIn(String destination, List<Integer> routeIdList);
    List<RouteEntity> findByOriginAndDestinationAndIdIn(String origin, String destination, List<Integer> routeIdList);

    // newly added
    List<RouteEntity> findByOriginIn(List<String> originList);
    List<RouteEntity> findByDestinationIn(List<String> originList);
    List<RouteEntity> findByOriginAndDestinationIn(String origin, List<String> destinationList);

    // again newly added
    List<RouteEntity> findByOriginAndDestinationNotIn(String origin, List<String> destinationList);
    List<RouteEntity> findByOriginAndIdNotIn(String origin, List<Integer> routeIdList);

    @Query("SELECT r FROM RouteEntity r WHERE r.origin = :origin AND r.id NOT IN :excludedRouteIds AND r.destination NOT IN :excludedDestinations")
    List<RouteEntity> selectByOriginExcludingRoutesAndDestinations(
            @Param("origin") String origin,
            @Param("excludedRouteIds") List<Integer> excludedRouteIds,
            @Param("excludedDestinations") List<String> excludedDestinations);
}
