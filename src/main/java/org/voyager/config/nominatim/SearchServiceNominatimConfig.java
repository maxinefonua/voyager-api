package org.voyager.config.nominatim;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Setter
@Getter
public class SearchServiceNominatimConfig {
    String baseURL = "https://nominatim.openstreetmap.org";
    String searchPath = "/search";
    String lookupPath = "/lookup";
    String queryParam = "?q=";
    String lookupParam = "?osm_ids=";
    String formatParam = "&format=";
    Format format = Format.GEOJSON;
    String languageParam = "&accept-language=en";

    public String buildParamsWithSearchQuery(String searchText) {
        StringBuilder paramBuilder = new StringBuilder();
        paramBuilder.append(queryParam);
        paramBuilder.append(searchText);
        paramBuilder.append(languageParam);
        paramBuilder.append(formatParam);
        paramBuilder.append(format.value);
        return paramBuilder.toString();
    }

    enum Format {
        XML("xml"), JSON("json"), JSONV2("jsonv2"), GEOJSON("geojson"), GEOCODEJSON("geocodejson");
        private static final Map<String, Format> VALUE_MAP = new HashMap<>();
        static {
            for (Format f: values()) {
                VALUE_MAP.put(f.value, f);
            }
        }

        String value;
        Format(String value) {
            this.value = value;
        }

        public static Format getFormat(String value) {
            return VALUE_MAP.get(value);
        }
    }
}
