package org.voyager.model.external.nominatim;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter @Getter
@ToString(includeFieldNames = false)
public class Address {
    String locality;
    String county;
    String state;
    String province;
    String region;
    String iso2;
    @JsonProperty("postcode")
    String postalCode;
    String country;
    @JsonProperty("country_code")
    String countryCode;
}
