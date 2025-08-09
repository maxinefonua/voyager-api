package org.voyager.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.voyager.model.currency.Currency;
import org.voyager.model.currency.CurrencyForm;
import org.voyager.model.currency.CurrencyPatch;
import org.voyager.model.entity.CurrencyEntity;
import org.voyager.repository.CurrencyRepository;
import org.voyager.service.CurrencyService;
import org.voyager.service.utils.MapperUtils;

import java.util.List;

import static org.voyager.service.utils.ServiceUtils.handleJPAExceptions;

@Service
public class CurrencyServiceImpl implements CurrencyService {
    @Autowired
    CurrencyRepository currencyRepository;

    @Override
    public Boolean codeExists(String code) {
        return currencyRepository.existsById(code);
    }

    @Override
    public Currency getCurrency(String code) {
        return handleJPAExceptions(() -> MapperUtils.entityToCurrency(currencyRepository.findById(code).get()));
    }

    @Override
    public Currency addCurrency(CurrencyForm currencyForm) {
        return handleJPAExceptions(() -> MapperUtils.entityToCurrency(
                currencyRepository.save(MapperUtils.formToCurrencyEntity(currencyForm))));
    }

    @Override
    public List<Currency> getCurrencies() {
        return currencyRepository.findAll().stream().map(MapperUtils::entityToCurrency).toList();
    }

    @Override
    public List<Currency> getCurrencies(Boolean isActive) {
        return currencyRepository.findByIsActive(isActive).stream().map(MapperUtils::entityToCurrency).toList();
    }

    @Override
    public Currency patchCurrency(String code, CurrencyPatch currencyPatch) {
        return handleJPAExceptions(() -> {
            CurrencyEntity currencyEntity = currencyRepository.findById(code).get();
            if (currencyPatch.getUsdRate() != null) currencyEntity.setUsdRate(currencyPatch.getUsdRate());
            if (currencyPatch.getIsActive() != null) currencyEntity.setIsActive(currencyPatch.getIsActive());
            if (currencyPatch.getSymbol() != null) currencyEntity.setSymbol(currencyPatch.getSymbol());
            return MapperUtils.entityToCurrency(currencyRepository.save(currencyEntity));
        });
    }
}
