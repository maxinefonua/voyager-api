package org.voyager.model.response.nominatim;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString(includeFieldNames = false)
public class SearchResponseNominatim {
    String type;
    String license;
    @JsonProperty("features")
    List<Feature> featureList;
}