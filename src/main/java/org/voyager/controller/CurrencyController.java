package org.voyager.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.voyager.model.currency.Currency;
import org.voyager.model.currency.CurrencyForm;
import org.voyager.model.currency.CurrencyPatch;
import org.voyager.service.CurrencyService;
import org.voyager.validate.ValidationUtils;

import java.util.List;

import static org.voyager.utils.ConstantsUtils.CURRENCY_CODE_PARAM_NAME;
import static org.voyager.utils.ConstantsUtils.IS_ACTIVE_PARAM_NAME;

public class CurrencyController {
    @Autowired
    CurrencyService currencyService;

    @GetMapping("/currencies")
    public List<Currency> getCurrencies(@RequestParam(name = IS_ACTIVE_PARAM_NAME,required = false) Boolean isActive) {
        if (isActive != null) return currencyService.getCurrencies(isActive);
        return currencyService.getCurrencies();
    }

    @GetMapping("/currencies/{currencyCode}")
    public Currency getCurrencyByCode(@PathVariable(name = CURRENCY_CODE_PARAM_NAME) String code) {
        code = ValidationUtils.validateAndGetCurrencyCode(code,currencyService,false);
        return currencyService.getCurrency(code);
    }

    @PatchMapping("/currencies/{currencyCode}")
    public Currency patchCurrency(@PathVariable(name = CURRENCY_CODE_PARAM_NAME) String code,
                                  @RequestBody(required = false) @Valid CurrencyPatch currencyPatch,
                                  BindingResult bindingResult) {
        code = ValidationUtils.validateAndGetCurrencyCode(code,currencyService,false);
        ValidationUtils.validateCurrencyPatch(currencyPatch,bindingResult);
        return currencyService.patchCurrency(code,currencyPatch);
    }

    @PostMapping("/currencies")
    public Currency createCurrency(@RequestBody(required = false) @Valid CurrencyForm currencyForm,
                                   BindingResult bindingResult) {
        ValidationUtils.validateCurrencyForm(currencyForm,bindingResult);
        return currencyService.addCurrency(currencyForm);
    }
}
