package org.voyager.model.external.nominatim;

import lombok.Getter;
import java.util.List;

@Getter
public class SearchResponseNominatim {
    String type;
    String licence;
    List<Feature> features;
}
