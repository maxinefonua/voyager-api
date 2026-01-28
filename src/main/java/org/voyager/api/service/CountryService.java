package org.voyager.api.service;

import jakarta.validation.Valid;
import org.voyager.commons.model.country.Continent;
import org.voyager.commons.model.country.Country;
import org.voyager.commons.model.country.CountryForm;
import java.util.List;

public interface CountryService {
    List<Country> getAll(List<Continent> continentList);
    Country addCountry(@Valid CountryForm countryForm);
    boolean countryCodeExists(String countryCode);
    Country getCountry(String countryCode);
    void deleteCountry(String countryCode);
}
