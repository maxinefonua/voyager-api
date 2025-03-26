package org.voyager.repository;

import org.springframework.data.repository.CrudRepository;
import org.voyager.model.entity.Region;

public interface RegionRepository extends CrudRepository<Region, Integer> {
    Region findByName(String name);
}
