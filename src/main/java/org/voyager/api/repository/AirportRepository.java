package org.voyager.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.voyager.api.model.entity.AirportEntity;
import org.voyager.commons.model.airport.AirportType;
import java.util.List;

public interface AirportRepository extends JpaRepository<AirportEntity,String> {
    @Query("SELECT a.iata FROM AirportEntity a ORDER BY iata")
    List<String> selectIata();

    @Query("SELECT a.iata FROM AirportEntity a WHERE type IN ?1 ORDER BY iata")
    List<String> selectIataByTypeIn(List<AirportType> typeList);

    List<AirportEntity> findByIataInOrderByIataAsc(List<String> iataList);
    List<AirportEntity> findByCountryCodeOrderByIataAsc(String countryCode);
    List<AirportEntity> findByCountryCodeAndTypeInOrderByIataAsc(String countryCode, List<AirportType> typeList);
    List<AirportEntity> findByTypeInOrderByIataAsc(List<AirportType> typeList);
}