package org.voyager.api.model.query;

import lombok.Getter;
import lombok.NonNull;

import java.util.List;

public class AirlineQuery {
    @Getter
    private List<String> iataList;
    @Getter
    private Boolean isActive = true;

    AirlineQuery(@NonNull List<String> iataList, Boolean isActive) {
        this.iataList = iataList;
        if (isActive != null) this.isActive = isActive;
    }

    public static AirlineQueryBuilder builder() {
        return new AirlineQueryBuilder();
    }

    public static class AirlineQueryBuilder {
        private List<String> iataList;
        private Boolean isActive;

        public AirlineQueryBuilder withIataList(@NonNull List<String> iataList) {
            this.iataList = iataList;
            return this;
        }

        public AirlineQueryBuilder withIsActive(@NonNull Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public AirlineQuery build() {
            return new AirlineQuery(iataList,isActive);
        }
    }
}
