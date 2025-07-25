package org.voyager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.voyager.model.country.Continent;
import org.voyager.model.entity.CountryEntity;

import java.util.List;

public interface CountryRepository extends JpaRepository<CountryEntity,String> {
    @Query("SELECT c.code FROM CountryEntity c WHERE c.continent IN ?1")
    List<String> selectCountryCodesByContinentIn(List<Continent> continentList);

    List<CountryEntity> findByContinentIn(List<Continent> continentList);
}
