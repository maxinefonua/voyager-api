package org.voyager.service;

import jakarta.validation.Valid;
import org.voyager.model.currency.Currency;
import org.voyager.model.currency.CurrencyForm;
import org.voyager.model.currency.CurrencyPatch;

import java.util.List;

public interface CurrencyService {
    Boolean codeExists(String code);
    Currency getCurrency(String code);
    Currency addCurrency(CurrencyForm currencyForm);
    List<Currency> getCurrencies();
    List<Currency> getCurrencies(Boolean isActive);
    Currency patchCurrency(String code, @Valid CurrencyPatch currencyPatch);
}
