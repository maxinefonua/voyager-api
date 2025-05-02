package org.voyager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.voyager.model.entity.Location;
import org.voyager.model.location.Source;
import org.voyager.model.location.Status;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location,Integer> {
    List<Location> findByStatus(Status status);
    List<Location> findByStatusIn(List<Status> statusList);
    List<Location> findBySource(Source source);
    List<Location> findBySourceAndSourceId(Source source, String sourceId);
}
