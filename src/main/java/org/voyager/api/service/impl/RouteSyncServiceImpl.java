package org.voyager.api.service.impl;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.voyager.api.model.entity.RouteSyncEntity;
import org.voyager.api.repository.admin.AdminRouteSyncRepository;
import org.voyager.api.service.RouteSyncService;
import org.voyager.api.service.utils.MapperUtils;
import org.voyager.commons.model.route.RouteSync;
import org.voyager.commons.model.route.RouteSyncBatchUpdate;
import org.voyager.commons.model.route.RouteSyncPatch;
import org.voyager.commons.model.route.Status;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.voyager.api.service.utils.ServiceUtils.handleJPAExceptions;

@Service
public class RouteSyncServiceImpl implements RouteSyncService {
    @Autowired
    AdminRouteSyncRepository adminRouteSyncRepository;

    @Override
    @Transactional("adminTransactionManager")
    public boolean existsById(@NonNull Integer routeId) {
        return adminRouteSyncRepository.existsById(routeId);
    }

    @Override
    @Transactional("adminTransactionManager")
    public List<RouteSync> getRouteSyncList(@NonNull List<Status> statusList) {
        return adminRouteSyncRepository.findByStatusIn(statusList).stream().map(MapperUtils::entityToRouteSync).toList();
    }

    @Override
    @Transactional("adminTransactionManager")
    public Integer batchUpdate(@NonNull RouteSyncBatchUpdate routeSyncBatchUpdate) {
        return handleJPAExceptions(()-> adminRouteSyncRepository.batchUpdateStatus(
                routeSyncBatchUpdate.getStatus(),ZonedDateTime.now(),routeSyncBatchUpdate.getRouteIdList()));
    }

    @Override
    @Transactional("adminTransactionManager")
    public RouteSync patch(@NonNull Integer routeId, @NonNull RouteSyncPatch routeSyncPatch) {
        Optional<RouteSyncEntity> optional = adminRouteSyncRepository.findById(routeId);
        assert optional.isPresent();
        RouteSyncEntity routeSyncEntity = optional.get();
        ZonedDateTime now = ZonedDateTime.now();
        Status status = routeSyncPatch.getStatus();
        if (status != null) {
            routeSyncEntity.setStatus(status);
            if (status.equals(Status.PROCESSING)) {
                routeSyncEntity.setLastProcessedAt(now);
                routeSyncEntity.setAttempts(routeSyncEntity.getAttempts()+1);
            }
        }
        if (routeSyncPatch.getError() != null) {
            routeSyncEntity.setErrorMessage(routeSyncPatch.getError());
        }
        routeSyncEntity.setUpdatedAt(now);
        return MapperUtils.entityToRouteSync(adminRouteSyncRepository.save(routeSyncEntity));
    }
}
