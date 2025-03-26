package org.voyager.model.response.geonames;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString(includeFieldNames = false)
public class BoundingBox {
    Float east;
    Float south;
    Float north;
    Float west;
//    Integer accuracyLevel;
}
