package org.voyager.api.controller.admin;

import io.vavr.control.Option;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.api.error.MessageConstants;
import org.voyager.api.model.entity.RouteEntity;
import org.voyager.api.service.RouteService;
import org.voyager.api.service.RouteSyncService;
import org.voyager.api.validate.ValidationUtils;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.route.Route;
import org.voyager.commons.model.route.RouteForm;
import org.voyager.commons.model.route.RoutePatch;
import org.voyager.commons.model.route.RouteSync;
import org.voyager.commons.model.route.RouteSyncPatch;
import org.voyager.commons.model.route.RouteSyncBatchUpdate;
import org.voyager.commons.model.route.Status;
import java.util.List;

@RestController
@RequestMapping(Path.Admin.ROUTES)
public class RouteAdminController {
    @Autowired
    RouteService routeService;

    @Autowired
    RouteSyncService routeSyncService;

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteAdminController.class);

    @PostMapping
    public Route addRoute(@RequestBody(required = false) @Valid RouteForm routeForm, BindingResult bindingResult) {
        LOGGER.info("POST {} of {}", Path.Admin.ROUTES,routeForm);
        ValidationUtils.validateRouteForm(routeForm, bindingResult);
        return routeService.save(routeForm);
    }

    @PatchMapping(Path.BY_ID)
    public Route patchRouteById(@PathVariable(name = ParameterNames.ID) String idString,
                                @RequestBody(required = false) @Valid RoutePatch routePatch,
                                BindingResult bindingResult) {
        LOGGER.info("PATCH {} of {}", Path.Admin.ROUTES.concat("/").concat(idString),routePatch);
        Integer id = ValidationUtils.validateAndGetInteger(ParameterNames.ID,idString,false);
        ValidationUtils.validateRoutePatch(routePatch,bindingResult);
        Option<RouteEntity> routeEntityOption = routeService.getRouteEntityById(id);
        if (routeEntityOption.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableMessage(ParameterNames.ID,String.valueOf(id)));
        return routeService.patchRoute(routeEntityOption.get(),routePatch);
    }

    @GetMapping(Path.Admin.SYNC)
    public List<RouteSync> getRouteByStatus(
            @RequestParam(name = ParameterNames.STATUS, required = false) List<String> statusStringList) {
        LOGGER.info("GET {} with {}:{}", Path.Admin.ROUTES.concat(Path.Admin.SYNC),ParameterNames.STATUS,statusStringList);
        List<Status> statusList = ValidationUtils.validateAndGetStatusList(ParameterNames.STATUS,statusStringList);
        return routeSyncService.getRouteSyncList(statusList);
    }

    @PatchMapping(Path.Admin.SYNC)
    public Integer batchUpdate(@RequestBody(required = false) RouteSyncBatchUpdate routeSyncBatchUpdate,
                               BindingResult bindingResult) {
        LOGGER.info("PATCH {} of {}", Path.Admin.ROUTES.concat(Path.Admin.SYNC),routeSyncBatchUpdate);
        ValidationUtils.validate(routeSyncBatchUpdate,bindingResult);
        return routeSyncService.batchUpdate(routeSyncBatchUpdate);
    }

    @PatchMapping(Path.Admin.SYNC_BY_ID)
    public RouteSync patchRouteSync(@PathVariable(name = ParameterNames.ID) String idString,
                                    @RequestBody(required = false) RouteSyncPatch routeSyncPatch,
                                    BindingResult bindingResult) {
        LOGGER.info("PATCH {} of {}", Path.Admin.ROUTES.concat(Path.Admin.SYNC).concat("/").concat(idString),routeSyncPatch);
        Integer routeId = ValidationUtils.validateAndGetRouteId(idString,routeService);
        ValidationUtils.validate(routeSyncPatch,bindingResult);
        return routeSyncService.patch(routeId,routeSyncPatch);
    }
}