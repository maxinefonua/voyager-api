package org.voyager.service;

import io.vavr.control.Option;
import jakarta.validation.Valid;
import org.voyager.model.country.Continent;
import org.voyager.model.country.Country;
import org.voyager.model.country.CountryForm;

import java.util.List;

public interface CountryService {
    List<Country> getAll(List<Continent> continentList);

    Country addCountry(@Valid CountryForm countryForm);

    boolean countryCodeExists(String countryCode);

    Option<Country> getCountry(String countryCode);
}
