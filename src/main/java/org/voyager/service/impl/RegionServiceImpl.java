package org.voyager.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.voyager.model.entity.Region;
import org.voyager.repository.RegionRepository;
import org.voyager.service.RegionService;

import java.util.Optional;

@Service
public class RegionServiceImpl implements RegionService {

    @Autowired
    private RegionRepository regionRepository;

    @Override
    public Region createRegion(Region region) {
        // TODO: verify returns with new id
        return regionRepository.save(region);
    }

    @Override
    public Iterable<Region> getAllRegions() {
        return regionRepository.findAll();
    }

    @Override
    public Region getRegionByName(String name) {
        return regionRepository.findByName(name);
    }

    @Override
    public Optional<Region> getRegionById(Integer id) {
        return regionRepository.findById(id);
    }
}
