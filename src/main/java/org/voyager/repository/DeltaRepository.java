package org.voyager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.voyager.model.delta.DeltaStatus;
import org.voyager.model.entity.Delta;

import java.util.List;

public interface DeltaRepository extends JpaRepository<Delta,String> {
    @Query("SELECT d.iata FROM Delta d WHERE d.status IN('ACTIVE','SEASONAL') ORDER BY iata")
    List<String> selectActiveSeasonalIataOrderByIata();
    List<Delta> findByStatusIn(List<DeltaStatus> statusList);
}
