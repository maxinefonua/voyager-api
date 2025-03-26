package org.voyager.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.voyager.model.response.geonames.GeoName;
import java.util.List;

@Setter @Getter
@ToString(includeFieldNames = false)
public class SearchResponseGeoNames {
    @JsonProperty("totalResultsCount")
    Integer totalResultsCount;
    @JsonProperty("geonames")
    List<GeoName> geoNames;
}
