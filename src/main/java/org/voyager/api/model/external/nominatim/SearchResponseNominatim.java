package org.voyager.api.model.external.nominatim;

import lombok.Getter;
import java.util.List;

@Getter
public class SearchResponseNominatim {
    String type;
    String licence;
    List<Feature> features;
}
