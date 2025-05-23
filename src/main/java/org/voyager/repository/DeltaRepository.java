package org.voyager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.voyager.model.delta.DeltaStatus;
import org.voyager.model.entity.DeltaEntity;

import java.util.List;

public interface DeltaRepository extends JpaRepository<DeltaEntity,String> {
    @Query("SELECT d.iata FROM DeltaEntity d WHERE d.status IN('ACTIVE','SEASONAL') ORDER BY iata")
    List<String> selectActiveSeasonalIataOrderByIata();
    List<DeltaEntity> findByStatusIn(List<DeltaStatus> statusList);
}
