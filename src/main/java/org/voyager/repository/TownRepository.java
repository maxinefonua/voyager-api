package org.voyager.repository;

import org.springframework.data.repository.CrudRepository;
import org.voyager.model.entity.Town;

public interface TownRepository extends CrudRepository<Town, Integer> {
}
