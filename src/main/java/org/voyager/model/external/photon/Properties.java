package org.voyager.model.external.photon;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter @Getter
@ToString(includeFieldNames = false)
public class Properties {
    @JsonProperty("osm_type")
    String osmType;
    @JsonProperty("osm_id")
    Long osmId;
    Double[] extent;
    String country;
    @JsonProperty("osm_key")
    String osmKey;
    String city;
    @JsonProperty("countrycode")
    String countryCode;
    @JsonProperty("osm_value")
    String osmValue;
    String name;
    String county;
    String state;
    String type;
}
