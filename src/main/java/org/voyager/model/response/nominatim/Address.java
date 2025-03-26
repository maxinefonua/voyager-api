package org.voyager.model.response.nominatim;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Address {
    @JsonProperty("house_number")
    String houseNumber;
    String office;
    String amenity;
    String road;
    @JsonProperty("neighbourhood")
    String neighborhood;
    String hamlet;
    String suburb;
    String city;
    String locality;
    String county;
    @JsonProperty("state_district")
    String stateDistrict;
    String state;
    String province;
    @JsonProperty("postcode")
    String postalCode;
    String country;
    @JsonProperty("country_code")
    String countryCode;
}
