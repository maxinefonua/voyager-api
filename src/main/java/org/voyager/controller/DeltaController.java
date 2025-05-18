package org.voyager.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.error.MessageConstants;
import org.voyager.model.delta.DeltaDisplay;
import org.voyager.model.delta.DeltaForm;
import org.voyager.model.delta.DeltaPatch;
import org.voyager.model.delta.DeltaStatus;
import org.voyager.service.DeltaService;
import org.voyager.validate.ValidationUtils;

import java.util.List;

import static org.voyager.utils.ConstantsUtils.DELTA_STATUS_PARAM_NAME;
import static org.voyager.utils.ConstantsUtils.IATA_PARAM_NAME;

@RestController
public class DeltaController {
    @Autowired
    private DeltaService deltaService;

    @GetMapping("/delta")
    public List<DeltaDisplay> getDeltas(@RequestParam(name = DELTA_STATUS_PARAM_NAME, required = false) List<String> statusStringList) {
        if (statusStringList == null) return deltaService.getAll();
        List<DeltaStatus> statusList = ValidationUtils.resolveDeltaStatusList(statusStringList);
        return deltaService.getAllByStatusList(statusList);
    }

    @PostMapping("/delta")
    public DeltaDisplay addDelta(@RequestBody @Valid @NotNull DeltaForm deltaForm, BindingResult bindingResult) {
        ValidationUtils.validateDeltaForm(deltaForm, bindingResult);
        return deltaService.save(deltaForm);
    }

    @GetMapping("/delta/{iata}")
    public DeltaDisplay getDeltaByIata(@PathVariable(name = IATA_PARAM_NAME) String iata, @RequestParam(name = DELTA_STATUS_PARAM_NAME, required = false) List<String> statusStringList) {
        iata = iata.toUpperCase();
        if (deltaService.exists(iata)) return deltaService.getByIata(iata).get();
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableMessage(IATA_PARAM_NAME,iata));
    }

    @PatchMapping("/delta/{iata}")
    public DeltaDisplay patchDeltaByIata(@RequestBody @Valid @NotNull DeltaPatch deltaPatch, BindingResult bindingResult, @PathVariable(name = "iata") String iata) {
        ValidationUtils.validateDeltaPatch(deltaPatch,bindingResult);
        iata = iata.toUpperCase();
        if (deltaService.exists(iata)) return deltaService.patch(deltaService.getByIata(iata).get(),deltaPatch);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableMessage(IATA_PARAM_NAME,iata));
    }
}
