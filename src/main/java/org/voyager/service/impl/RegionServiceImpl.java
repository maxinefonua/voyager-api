package org.voyager.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.voyager.model.TownDisplay;
import org.voyager.model.entity.Region;
import org.voyager.model.entity.Town;
import org.voyager.repository.RegionRepository;
import org.voyager.service.RegionService;

import java.util.ArrayList;
import java.util.List;

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
    public List<TownDisplay> convertTownListToTownDisplayList(Iterable<Town> townList) {
        ArrayList<TownDisplay> townDisplayList = new ArrayList<>();
        townList.forEach(town -> {
            Region region = regionRepository.findById(town.getRegionId()).get();
            townDisplayList.add(new TownDisplay(town.getName(),town.getCountry(),region.getName()));
        });
        return townDisplayList;
    }
}
