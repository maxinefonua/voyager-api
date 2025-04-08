package org.voyager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.voyager.model.entity.Delta;
import org.voyager.model.entity.Status;

import java.util.List;

public interface DeltaRepository extends JpaRepository<Delta,String> {
    List<Delta> findAll();
    List<Delta> findByStatus(Status status);
    List<Delta> findByStatusNot(Status status);
    List<Delta> findByStatusIn(List<Status> statusList);
}
