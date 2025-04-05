package org.voyager.model.external.nominatim;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter @Getter
@ToString(includeFieldNames = false)
public class Feature {
    String type;
    Properties properties;
    Double[] bbox;
    Geometry geometry;
}