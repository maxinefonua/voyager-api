package org.voyager.api.controller.admin;

import io.vavr.control.Option;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.api.error.MessageConstants;
import org.voyager.api.model.entity.RouteEntity;
import org.voyager.api.service.RouteService;
import org.voyager.api.validate.ValidationUtils;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.route.Route;
import org.voyager.commons.model.route.RouteForm;
import org.voyager.commons.model.route.RoutePatch;

@RestController
@RequestMapping(Path.Admin.ROUTES)
public class RouteAdminController {
    @Autowired
    RouteService routeService;

    @PostMapping
    public Route addRoute(@RequestBody(required = false) @Valid RouteForm routeForm, BindingResult bindingResult) {
        ValidationUtils.validateRouteForm(routeForm, bindingResult);
        return routeService.save(routeForm);
    }


    @PatchMapping(Path.BY_ID)
    public Route patchRouteById(@PathVariable(name = ParameterNames.ID_PATH_VAR_NAME) String idString, @RequestBody(required = false) @Valid RoutePatch routePatch, BindingResult bindingResult) {
        Integer id = ValidationUtils.validateAndGetInteger(ParameterNames.ID_PATH_VAR_NAME,idString,false);
        ValidationUtils.validateRoutePatch(routePatch,bindingResult);
        Option<RouteEntity> routeEntityOption = routeService.getRouteEntityById(id);
        if (routeEntityOption.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableMessage(ParameterNames.ID_PATH_VAR_NAME,String.valueOf(id)));
        return routeService.patchRoute(routeEntityOption.get(),routePatch);
    }
}
