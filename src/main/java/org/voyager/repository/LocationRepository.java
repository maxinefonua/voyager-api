package org.voyager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.voyager.model.entity.Location;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location,Integer> {
    List<Location> findByStatus(Location.Status status);
    List<Location> findByStatusIn(List<Location.Status> statusList);
}
