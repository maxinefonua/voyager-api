package org.voyager.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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
}
