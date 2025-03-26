package org.voyager.model.response.photon;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.voyager.model.response.nominatim.Feature;
import java.util.List;

@Setter
@Getter
@ToString(includeFieldNames = false)
public class SearchResponsePhoton {
    String type;
    @JsonProperty("features")
    List<Feature> featureList;
}
