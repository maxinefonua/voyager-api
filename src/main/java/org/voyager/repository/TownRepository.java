package org.voyager.repository;

import org.springframework.data.repository.CrudRepository;
import org.voyager.model.entity.Town;

import java.util.List;

public interface TownRepository extends CrudRepository<Town, Integer> {
    List<Town> findAll();
}
