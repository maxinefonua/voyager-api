package org.voyager.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.voyager.api.model.request.CountryPageRequest;
import org.voyager.api.repository.admin.AdminCountryRepository;
import org.voyager.api.repository.tests.TestsCountryRepository;
import org.voyager.commons.model.country.Continent;
import org.voyager.commons.model.country.Country;
import org.voyager.commons.model.country.CountryForm;
import org.voyager.api.model.entity.CountryEntity;
import org.voyager.api.repository.primary.CountryRepository;
import org.voyager.api.service.CountryService;
import org.voyager.api.service.utils.MapperUtils;
import org.voyager.commons.model.response.PagedResponse;

import java.util.List;
import static org.voyager.api.service.utils.ServiceUtils.handleJPAExceptions;

@Service
public class CountryServiceImpl implements CountryService {
    @Autowired
    CountryRepository countryRepository;

    @Autowired
    AdminCountryRepository adminCountryRepository;

    @Autowired
    TestsCountryRepository testsCountryRepository;

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
    public PagedResponse<Country> getPagedCountries(CountryPageRequest countryPageRequest) {
        List<Continent> continentList = getContinentList(countryPageRequest.getContinentList());
        PageRequest pageRequest = countryPageRequest.getPageRequest();
        Page<CountryEntity> page = countryRepository.findDynamic(
                continentList,
                Pageable.ofSize(pageRequest.getPageSize())
                        .withPage(pageRequest.getPageNumber()));
        return PagedResponse.<Country>builder()
                .content(page.getContent().stream().map(MapperUtils::entityToCountry).toList())
                .page(pageRequest.getPageNumber())
                .size(pageRequest.getPageSize())
                .first(page.isFirst())
                .last(page.isLast())
                .totalPages(page.getTotalPages())
                .numberOfElements(page.getNumberOfElements())
                .totalElements(page.getTotalElements())
                .build();
    }

    private List<Continent> getContinentList(List<Continent> continentList) {
        if (continentList == null || continentList.isEmpty()) return null;
        return continentList;
    }

    @Override
    @Transactional("adminTransactionManager")
    public Country addCountry(CountryForm countryForm) {
        return handleJPAExceptions(() -> MapperUtils.entityToCountry(
                adminCountryRepository.save(MapperUtils.formToCountryEntity(countryForm))));
    }

    @Override
    public boolean countryCodeExists(String countryCode) {
        return countryRepository.existsById(countryCode);
    }

    @Override
    public Country getCountry(String countryCode) {
        return countryRepository.findById(countryCode).map(MapperUtils::entityToCountry).get();
    }

    @Override
    @Transactional("testsTransactionManager")
    public void deleteCountry(String countryCode) {
        handleJPAExceptions(()-> testsCountryRepository.deleteById(countryCode));
    }
}
