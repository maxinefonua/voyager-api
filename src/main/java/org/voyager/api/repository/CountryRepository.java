package org.voyager.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.voyager.commons.model.country.Continent;
import org.voyager.api.model.entity.CountryEntity;

import java.util.List;

public interface CountryRepository extends JpaRepository<CountryEntity,String> {
    List<CountryEntity> findAllByOrderByNameAsc();
    List<CountryEntity> findByContinentInOrderByNameAsc(List<Continent> continentList);
}
