package org.voyager.api.service.impl;

import io.vavr.control.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.voyager.commons.model.country.Continent;
import org.voyager.commons.model.country.Country;
import org.voyager.commons.model.country.CountryForm;
import org.voyager.api.model.entity.CountryEntity;
import org.voyager.api.repository.CountryRepository;
import org.voyager.api.service.CountryService;
import org.voyager.api.service.utils.MapperUtils;
import java.util.List;

import static org.voyager.api.service.utils.ServiceUtils.handleJPAExceptions;

@Service
public class CountryServiceImpl implements CountryService {
    @Autowired
    CountryRepository countryRepository;

    @Override
    public List<Country> getAll(List<Continent> continentList) {
        return handleJPAExceptions(() -> {
            List<CountryEntity> countryEntities;
            if (continentList.isEmpty()) {
                countryEntities = countryRepository.findAllByOrderByNameAsc();
            } else {
                countryEntities = countryRepository.findByContinentInOrderByNameAsc(continentList);
            }
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
