package org.voyager.model.response.geonames;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter @Getter
@ToString(includeFieldNames = false)
public class GeoName {
    Timezone timezone;
    @JsonProperty("bbox")
    BoundingBox boundingBox;

//    AlternativeName[] alternateNames;
//    String asciiName;
//    String wikipediaURL;
    Long geonameId;
    String name;
//    String toponymName;

//    @JsonProperty("srtm3")
//    Integer elevationSRTM;
//    @JsonProperty("astergdem")
//    Integer elevationAGDEM;
    Long population;

//    Map<String,Object> adminCodes1;
//    String adminCode1;
//    String adminCode2;
//    String adminId1;
//    String adminId2;


    String countryId;
    String countryCode;
    String countryName;
    ContinentCode continentCode;

    String adminName1;
//    String adminName2;
//    String adminName3;
//    String adminName4;
//    String adminName5;

    Float lng;
    Float lat;

//    String fcode;
//    String fcodeName;
    FeatureClass fcl;
    String fclName;
}
