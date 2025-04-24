package org.voyager.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.voyager.model.entity.Location;


@Data
@NoArgsConstructor
@Builder @AllArgsConstructor
public class LocationDisplay {
    private Integer id;
    private String name;
    private String subdivision;
    private String countryCode;
    private Double latitude;
    private Double longitude;
    private Double[] bbox;
    private Location.Status status;
}
