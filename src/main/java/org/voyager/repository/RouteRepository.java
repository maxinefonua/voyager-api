package org.voyager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.voyager.model.entity.RouteEntity;
import java.util.List;

public interface RouteRepository extends JpaRepository<RouteEntity,Integer> {
    @Query("SELECT COUNT(*)>0 FROM RouteEntity r WHERE r.origin = ?1")
    Boolean originExists(String origin);
    @Query("SELECT COUNT(*)>0 FROM RouteEntity r WHERE r.destination = ?1")
    Boolean destinationExists(String destination);

    List<RouteEntity> findByOrigin(String origin);
    List<RouteEntity> findByDestination(String destination);
    List<RouteEntity> findByOriginAndDestination(String origin, String destination);
}
