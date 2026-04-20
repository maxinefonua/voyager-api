package org.voyager.api.model.request;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.voyager.commons.model.country.Continent;

import java.util.List;

@Builder @Data
public class CountryPageRequest {
    List<Continent> continentList;
    PageRequest pageRequest;
}
