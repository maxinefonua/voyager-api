package org.voyager.model;

public class TownDisplay {
    private String name;
    private String country;
    private String regionName;

    public TownDisplay() {}
    public TownDisplay(String name, String country, String region) {
        this.name = name;
        this.country = country;
        this.regionName = region;
    }

    public String getName() {
        return this.name;
    }

    public String getCountry() {
        return this.country;
    }

    public String getRegionName() {
        return this.regionName;
    }
}
