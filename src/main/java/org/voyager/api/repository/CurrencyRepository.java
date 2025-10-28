package org.voyager.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.voyager.api.model.entity.CurrencyEntity;

import java.util.List;

public interface CurrencyRepository extends JpaRepository<CurrencyEntity,String> {
    List<CurrencyEntity> findByIsActive(Boolean isActive);
}
