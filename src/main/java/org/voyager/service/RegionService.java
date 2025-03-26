package org.voyager.service;

import org.voyager.model.TownDisplay;
import org.voyager.model.entity.Region;
import org.voyager.model.entity.Town;

import java.util.List;

public interface RegionService {
    // TODO: implement Either framework to handle errors
    public abstract Region createRegion(Region region);
    public abstract Iterable<Region> getAllRegions();
    public abstract Region getRegionByName(String name);
    public abstract List<TownDisplay> convertTownListToTownDisplayList(Iterable<Town> townList);
}
