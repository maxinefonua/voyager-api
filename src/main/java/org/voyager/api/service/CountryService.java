package org.voyager.api.service;

import jakarta.validation.Valid;
import org.voyager.api.model.request.CountryPageRequest;
import org.voyager.commons.model.country.Continent;
import org.voyager.commons.model.country.Country;
import org.voyager.commons.model.country.CountryForm;
import org.voyager.commons.model.response.PagedResponse;

import java.util.List;

public interface CountryService {
    List<Country> getAll(List<Continent> continentList);
    PagedResponse<Country> getPagedCountries(CountryPageRequest countryPageRequest);
    Country addCountry(@Valid CountryForm countryForm);
    boolean countryCodeExists(String countryCode);
    Country getCountry(String countryCode);
    void deleteCountry(String countryCode);
}
