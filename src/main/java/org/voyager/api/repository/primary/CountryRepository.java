package org.voyager.api.repository.primary;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.voyager.commons.model.country.Continent;
import org.voyager.api.model.entity.CountryEntity;
import java.util.List;

public interface CountryRepository extends JpaRepository<CountryEntity,String> {
    List<CountryEntity> findAllByOrderByNameAsc();
    Page<CountryEntity> findAllByOrderByNameAsc(Pageable pageable);
    @Query("SELECT c FROM CountryEntity c WHERE " +
            "(:continentList IS NULL OR c.continent IN :continentList) " +
            "ORDER BY c.name ASC")
    Page<CountryEntity> findDynamic(@Param("continentList") List<Continent> continentList, Pageable pageable);
    List<CountryEntity> findByContinentInOrderByNameAsc(List<Continent> continentList);
}
