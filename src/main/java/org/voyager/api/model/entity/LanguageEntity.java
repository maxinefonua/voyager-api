package org.voyager.api.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "languages")
public class LanguageEntity {
    @Id
    @Column(name = "name")
    String name;
    @Column(name = "iso_639_1",length = 2, columnDefinition = "bpchar")
    String iso6391;
    @Column(name = "iso_639_2",length = 3, columnDefinition = "bpchar")
    String iso6392;
    @Column(name = "iso_639_3",length = 3, columnDefinition = "bpchar")
    String iso6393;
}
