package org.voyager.api.repository.tests;

import org.springframework.data.jpa.repository.JpaRepository;
import org.voyager.api.model.entity.CountryEntity;

public interface TestsCountryRepository extends JpaRepository<CountryEntity,String> {
}
