package org.voyager.service.impl;

import io.vavr.control.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.voyager.model.country.Continent;
import org.voyager.model.country.Country;
import org.voyager.model.country.CountryForm;
import org.voyager.model.entity.CountryEntity;
import org.voyager.repository.CountryRepository;
import org.voyager.service.CountryService;
import org.voyager.service.utils.MapperUtils;

import java.util.List;

import static org.voyager.service.utils.ServiceUtils.handleJPAExceptions;

@Service
public class CountryServiceImpl implements CountryService {
    @Autowired
    CountryRepository countryRepository;

    @Override
    public List<Country> getAll(List<Continent> continentList, Option<String> currencyCodeOption) {
        return handleJPAExceptions(() -> {
            List<CountryEntity> countryEntities;
            if (continentList.isEmpty() && currencyCodeOption.isEmpty()) countryEntities = countryRepository.findAllByOrderByNameAsc();
            else if (currencyCodeOption.isEmpty()) countryEntities = countryRepository.findByContinentInOrderByNameAsc(continentList);
            else if (continentList.isEmpty()) countryEntities = countryRepository.findByCurrencyCodeOrderByNameAsc(currencyCodeOption.get());
            else countryEntities = countryRepository.findByCurrencyCodeAndContinentInOrderByNameAsc(currencyCodeOption.get(),continentList);
            return countryEntities.stream().map(MapperUtils::entityToCountry).toList();
        });
    }

    @Override
    public Country addCountry(CountryForm countryForm) {
        return handleJPAExceptions(() ->
                MapperUtils.entityToCountry(countryRepository.save(MapperUtils.formToCountryEntity(countryForm))));
    }

    @Override
    public boolean countryCodeExists(String countryCode) {
        return handleJPAExceptions(()->
                countryRepository.existsById(countryCode));
    }

    @Override
    public Option<Country> getCountry(String countryCode) {
        return handleJPAExceptions(() ->
                Option.ofOptional(countryRepository.findById(countryCode).map(MapperUtils::entityToCountry)));
    }
}
