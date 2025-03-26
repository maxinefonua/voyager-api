package org.voyager.model.response.nominatim;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString(includeFieldNames = false)
public class Properties {
    @JsonProperty("place_id")
    Long placeId;
    @JsonProperty("osm_type")
    String osmType;
    @JsonProperty("osm_id")
    Long osmId;
    Float[] extent;
    String country;
    String city;
    @JsonProperty("countrycode")
    String countryCode;
    @JsonProperty("postcode")
    String postalCode;
    String county;
    String type;
    @JsonProperty("osm_key")
    String osmKey;
    @JsonProperty("osm_value")
    String osmValue;
    @JsonProperty("housenumber")
    String houseNumber;
    String street;
    String district;
    String name;
    String state;

    // Nominatim
    @JsonProperty("place_rank")
    Integer placeRank;
    String category;
    Float importance;
    @JsonProperty("addresstype")
    String addressType;
    @JsonProperty("display_name")
    String displayName;
    Address address;
}
