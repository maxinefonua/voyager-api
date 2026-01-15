package org.voyager.api.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.voyager.api.model.entity.CountryEntity;

public interface AdminCountryRepository extends JpaRepository<CountryEntity,String> {
}
