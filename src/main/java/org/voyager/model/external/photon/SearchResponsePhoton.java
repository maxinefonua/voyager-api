package org.voyager.model.external.photon;

import lombok.Getter;
import java.util.List;

@Getter
public class SearchResponsePhoton {
    List<Feature> features;
    String type;
}
