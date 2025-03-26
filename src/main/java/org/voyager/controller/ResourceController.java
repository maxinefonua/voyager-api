package org.voyager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.voyager.model.TownDisplay;
import org.voyager.model.response.SearchResponseGeoNames;
import org.voyager.repository.TownRepository;
import org.voyager.service.RegionService;
import org.voyager.service.SearchLocationService;

import java.util.List;

@RestController
class ResourceController {

    @Autowired
    private TownRepository townRepository;

    @Autowired
    private RegionService regionService;

    @Autowired
    private SearchLocationService<SearchResponseGeoNames> searchLocationService;

    @GetMapping("/towns")
    @Cacheable("townCache")
    public List<TownDisplay> getTowns() {
        System.out.println("fetching uncached getTowns");
        return regionService.convertTownListToTownDisplayList(townRepository.findAll());
    }

    @GetMapping("/search")
    @Cacheable("searchCache")
    public SearchResponseGeoNames search(@RequestParam String searchText, @RequestParam(defaultValue = "0") Integer startRow) {
        System.out.println("fetching uncached searchText: " + searchText + ", startRow: " + startRow);
        return searchLocationService.search(searchText,startRow);
    }
}
