package org.voyager.model.response.geonames;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString(includeFieldNames = false)
public class AlternativeName {
    String name;
    String lang;
}