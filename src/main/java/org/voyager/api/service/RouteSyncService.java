package org.voyager.api.service;

import lombok.NonNull;
import org.springframework.validation.annotation.Validated;
import org.voyager.commons.model.route.RouteSync;
import org.voyager.commons.model.route.RouteSyncBatchUpdate;
import org.voyager.commons.model.route.RouteSyncPatch;
import org.voyager.commons.model.route.Status;
import java.util.List;

public interface RouteSyncService {
    boolean existsById(@NonNull Integer routeId);
    List<RouteSync> getRouteSyncList(@NonNull List<Status> statusList);
    Integer batchUpdate(@NonNull @Validated RouteSyncBatchUpdate routeSyncBatchUpdate);
    RouteSync patch(@NonNull Integer routeId, @NonNull @Validated RouteSyncPatch routeSyncPatch);
}