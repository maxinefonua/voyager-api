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
    List<RouteEntity> findByOriginInAndDestinationNotInAndIdNotIn(
            List<String> originList,
            List<String> excludeDestinationList,
            List<Integer> excludeRouteIdList);
    List<RouteEntity> findByOriginInAndDestinationIn(List<String> originList,List<String> destinationList);
    List<RouteEntity> findByOriginInAndDestinationInAndDestinationNotInAndIdNotIn(
            List<String> originList,
            List<String> destinationList,
            List<String> excludeDestinationList,
            List<Integer> excludeRouteIdList);
    List<RouteEntity> findByDestinationIn(List<String> originList);
    List<RouteEntity> findByDestinationInAndDestinationNotInAndIdNotIn(
            List<String> originList,
            List<String> excludeDestinationList,
            List<Integer> excludeRouteIdList);
    List<RouteEntity> findByDestinationNotInAndIdNotIn(
            List<String> excludeDestinationList,
            List<Integer> excludeRouteIdList);
    List<RouteEntity> findByOriginAndDestinationIn(String origin, List<String> destinationList);

    // again newly added
    List<RouteEntity> findByOriginAndDestinationNotIn(String origin, List<String> destinationList);
    List<RouteEntity> findByOriginAndIdNotIn(String origin, List<Integer> routeIdList);

    @Query("SELECT r FROM RouteEntity r WHERE r.origin = :origin AND r.id NOT IN :excludedRouteIds AND r.destination NOT IN :excludedDestinations")
    List<RouteEntity> selectByOriginExcludingRoutesAndDestinations(
            @Param("origin") String origin,
            @Param("excludedRouteIds") List<Integer> excludedRouteIds,
            @Param("excludedDestinations") List<String> excludedDestinations);

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
