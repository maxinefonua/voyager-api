package org.voyager.model.external.nominatim;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter @Getter
@ToString(includeFieldNames = false)
public class Geometry {
    String type;
    Double[] coordinates;
}
