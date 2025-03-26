package org.voyager.model.response.nominatim;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString(includeFieldNames = false)
public class Feature {
    String type;
    Properties properties;
    @JsonProperty("bbox")
    Double[] boundingBox;
    Geometry geometry;
}
