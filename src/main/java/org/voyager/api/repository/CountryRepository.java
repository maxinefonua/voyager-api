package org.voyager.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.voyager.commons.model.country.Continent;
import org.voyager.api.model.entity.CountryEntity;

import java.util.List;

public interface CountryRepository extends JpaRepository<CountryEntity,String> {
    @Query("SELECT c.code FROM CountryEntity c WHERE c.continent IN ?1")
    List<String> selectCountryCodesByContinentIn(List<Continent> continentList);

    List<CountryEntity> findAllByOrderByNameAsc();
    List<CountryEntity> findByContinentInOrderByNameAsc(List<Continent> continentList);
    List<CountryEntity> findByCurrencyCodeOrderByNameAsc(String currencyCode);
    List<CountryEntity> findByCurrencyCodeAndContinentInOrderByNameAsc(String currencyCode,List<Continent> continentList);
}
