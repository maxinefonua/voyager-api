package org.voyager.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data @NoArgsConstructor
@Builder @AllArgsConstructor
@Table(name="locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Integer id;

    @Column(name = "src", length = 16)
    private String source;

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

    @Column(name = "lon")
    private Double longitude;

    @Column
    private Double[] bbox;

    @Enumerated(EnumType.STRING)
    Status status;

    public enum Status {
        ACTIVE,
        ARCHIVED,
        DELETED
    }
}
