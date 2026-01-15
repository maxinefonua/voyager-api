package org.voyager.api.model.geonames;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.ZoneId;

@Setter
@Getter
@ToString(includeFieldNames = false)
public class Timezone {
    Integer gmtOffset;
    @JsonProperty("timeZoneId")
    ZoneId zoneId;
    Integer dstOffset;
}