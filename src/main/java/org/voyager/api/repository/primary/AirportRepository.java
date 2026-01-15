package org.voyager.api.repository.primary;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.voyager.api.model.entity.AirportEntity;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airport.AirportType;
import java.util.List;

public interface AirportRepository extends JpaRepository<AirportEntity,String> {
    @Query("SELECT DISTINCT a FROM AirportEntity a " +
            "WHERE (:airlineList IS NULL OR EXISTS (" +
            "    SELECT 1 FROM AirlineAirportEntity aa " +
            "    WHERE aa.iata = a.iata " +
            "    AND aa.airline IN :airlineList" +
            ")) " +
            "AND (:typeList IS NULL OR a.type IN :typeList) " +
            "AND (:countryCode IS NULL OR a.countryCode = :countryCode) " +
            "ORDER BY a.iata ASC")
    Page<AirportEntity> findAirportsDynamic(
            @Param("airlineList") List<Airline> airlineList,
            @Param("typeList") List<AirportType> typeList,
            @Param("countryCode") String countryCode,
            Pageable pageable);

    @Query("SELECT a.iata FROM AirportEntity a ORDER BY iata")
    List<String> selectIata();

    @Query("SELECT a.iata FROM AirportEntity a WHERE type IN ?1 ORDER BY iata")
    List<String> selectIataByTypeIn(List<AirportType> typeList);

    List<AirportEntity> findByIataInOrderByIataAsc(List<String> iataList);
    List<AirportEntity> findByCountryCodeOrderByIataAsc(String countryCode);
    List<AirportEntity> findByCountryCodeAndTypeInOrderByIataAsc(String countryCode, List<AirportType> typeList);
    List<AirportEntity> findByTypeInOrderByIataAsc(List<AirportType> typeList);
    Page<AirportEntity> findAllByOrderByIataAsc(Pageable pageable);
}