package org.voyager.api.model.query;

import lombok.Getter;
import lombok.NonNull;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airport.AirportType;

import java.util.List;

public class IataQuery {
    @Getter
    private List<AirportType> airportTypeList;
    @Getter
    private List<Airline> airlineList;

    IataQuery(List<AirportType> airportTypeList, List<Airline> airlineList) {
        this.airportTypeList = airportTypeList;
        this.airlineList = airlineList;
        if (airportTypeList == null && airlineList == null) {
            throw new IllegalStateException("IataQuery requires at least one nonnull field");
        }
        if (airportTypeList != null && airportTypeList.isEmpty()) {
            throw new IllegalStateException("airportTypeList cannot be empty");
        }
        if (airlineList != null && airlineList.isEmpty()) {
            throw new IllegalStateException("airlineList cannot be empty");
        }
    }

    public static IataQueryBuilder builder() {
        return new IataQueryBuilder();
    }

    public static class IataQueryBuilder {
        private List<AirportType> airportTypeList;
        private List<Airline> airlineList;

        public IataQueryBuilder withAirportTypeList(@NonNull List<AirportType> airportTypeList) {
            this.airportTypeList = airportTypeList;
            return this;
        }

        public IataQueryBuilder withAirlineList(@NonNull List<Airline> airlineList) {
            this.airlineList = airlineList;
            return this;
        }

        public IataQuery build() {
            return new IataQuery(airportTypeList,airlineList);
        }
    }
}
