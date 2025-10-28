package org.voyager.api.model.external.geonames;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter @Getter @NoArgsConstructor
@ToString(includeFieldNames = false)
public class SearchResponseGeoNames {
    @JsonProperty("totalResultsCount")
    Integer totalResultsCount;
    @JsonProperty("geonames")
    List<GeoName> geoNames;
}
