package org.voyager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.voyager.model.entity.LocationEntity;
import org.voyager.model.location.Source;
import org.voyager.model.location.Status;

import java.util.List;

public interface LocationRepository extends JpaRepository<LocationEntity,Integer> {
    List<LocationEntity> findByStatus(Status status);
    List<LocationEntity> findByStatusIn(List<Status> statusList);
    List<LocationEntity> findBySource(Source source);
    List<LocationEntity> findBySourceAndSourceId(Source source, String sourceId);
}
