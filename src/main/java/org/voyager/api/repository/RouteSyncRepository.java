package org.voyager.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.voyager.api.model.entity.RouteSyncEntity;
import org.voyager.commons.model.route.Status;

import java.time.ZonedDateTime;
import java.util.List;

public interface RouteSyncRepository extends JpaRepository<RouteSyncEntity,Integer> {
    List<RouteSyncEntity> findByStatusIn(List<Status> statusList);

    @Modifying
    @Query("UPDATE RouteSyncEntity r SET r.status = :status, r.updatedAt = :now, " +
            "r.lastProcessedAt = CASE WHEN :status = 'PROCESSING' THEN :now ELSE r.lastProcessedAt END, " +
            "r.attempts = CASE WHEN :status = 'PROCESSING' THEN r.attempts + 1 ELSE r.attempts END " +
            "WHERE r.id IN :ids")
    int batchUpdateStatus(
            @Param("status") Status status,
            @Param("now") ZonedDateTime now,
            @Param("ids") List<Integer> routeIdList
    );
}
