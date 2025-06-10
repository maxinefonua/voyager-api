package org.voyager.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.voyager.model.location.Source;
import org.voyager.model.location.Status;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Data @NoArgsConstructor
@Builder @AllArgsConstructor
@Table(name="locations")
public class LocationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "src", length = 16)
    private Source source;

    @Column(name = "src_id", length = 30)
    private String sourceId;

    @Column(length = 30)
    private String name;

    @Column(name = "subd", length = 50)
    private String subdivision;

    @Column(name = "cc",length = 2, columnDefinition = "bpchar")
    private String countryCode;

    @Column(name = "lat")
    private Double latitude;

    @Column(name = "lng")
    private Double longitude;

    @Column
    private Double[] bbox;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String[] airports;
}
