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
            "WHERE (:typeList IS NULL OR a.type IN :typeList) " +
            "AND (:countryCode IS NULL OR a.countryCode = :countryCode) " +
            "ORDER BY a.iata ASC")
    Page<AirportEntity> findAirportsDynamicWithoutAirlines(
            @Param("typeList") List<AirportType> typeList,
            @Param("countryCode") String countryCode,
            Pageable pageable);

    @Query("SELECT a.iata FROM AirportEntity a ORDER BY iata")
    List<String> selectIata();

    @Query("SELECT a.iata FROM AirportEntity a WHERE type IN ?1 ORDER BY iata")
    List<String> selectIataByTypeIn(List<AirportType> typeList);

    List<AirportEntity> findByIataInOrderByIataAsc(List<String> iataList);
    List<AirportEntity> findByTypeInOrderByIataAsc(List<AirportType> typeList);

    @Query(value = """
        SELECT DISTINCT a.*
        FROM airports a
        WHERE a.iata IN (
            SELECT r.orgn
            FROM routes r
            INNER JOIN flights f ON f.route_id = r.id
            WHERE f.active = true AND f.airline IN (:airlineList)
            UNION
            SELECT r.dstn
            FROM routes r
            INNER JOIN flights f ON f.route_id = r.id
            WHERE f.active = true AND f.airline IN (:airlineList)
        )
        AND a.type IN (:typeList)
        AND a.country = :countryCode
        ORDER BY a.iata ASC
        """,
            countQuery = """
        SELECT COUNT(DISTINCT a.iata)
        FROM airports a
        WHERE a.iata IN (
            SELECT r.orgn
            FROM routes r
            INNER JOIN flights f ON f.route_id = r.id
            WHERE f.active = true AND f.airline IN (:airlineList)
            UNION
            SELECT r.dstn
            FROM routes r
            INNER JOIN flights f ON f.route_id = r.id
            WHERE f.active = true AND f.airline IN (:airlineList)
        )
        AND a.type IN (:typeList)
        AND a.country = :countryCode
        """,
            nativeQuery = true)
    Page<AirportEntity> findAirportsByAirlinesAndTypesAndCountry(
            @Param("airlineList") List<String> airlineList,
            @Param("typeList") List<String> typeList,
            @Param("countryCode") String countryCode,
            Pageable pageable);

    // 2. Airlines + Types (no country)
    @Query(value = """
        SELECT DISTINCT a.*
        FROM airports a
        WHERE a.iata IN (
            SELECT r.orgn
            FROM routes r
            INNER JOIN flights f ON f.route_id = r.id
            WHERE f.active = true AND f.airline IN (:airlineList)
            UNION
            SELECT r.dstn
            FROM routes r
            INNER JOIN flights f ON f.route_id = r.id
            WHERE f.active = true AND f.airline IN (:airlineList)
        )
        AND a.type IN (:typeList)
        ORDER BY a.iata ASC
        """,
            countQuery = """
        SELECT COUNT(DISTINCT a.iata)
        FROM airports a
        WHERE a.iata IN (
            SELECT r.orgn
            FROM routes r
            INNER JOIN flights f ON f.route_id = r.id
            WHERE f.active = true AND f.airline IN (:airlineList)
            UNION
            SELECT r.dstn
            FROM routes r
            INNER JOIN flights f ON f.route_id = r.id
            WHERE f.active = true AND f.airline IN (:airlineList)
        )
        AND a.type IN (:typeList)
        """,
            nativeQuery = true)
    Page<AirportEntity> findAirportsByAirlinesAndTypes(
            @Param("airlineList") List<String> airlineList,
            @Param("typeList") List<String> typeList,
            Pageable pageable);

    // 3. Airlines + Country (no types)
    @Query(value = """
        SELECT DISTINCT a.*
        FROM airports a
        WHERE a.iata IN (
            SELECT r.orgn
            FROM routes r
            INNER JOIN flights f ON f.route_id = r.id
            WHERE f.active = true AND f.airline IN (:airlineList)
            UNION
            SELECT r.dstn
            FROM routes r
            INNER JOIN flights f ON f.route_id = r.id
            WHERE f.active = true AND f.airline IN (:airlineList)
        )
        AND a.country = :countryCode
        ORDER BY a.iata ASC
        """,
            countQuery = """
        SELECT COUNT(DISTINCT a.iata)
        FROM airports a
        WHERE a.iata IN (
            SELECT r.orgn
            FROM routes r
            INNER JOIN flights f ON f.route_id = r.id
            WHERE f.active = true AND f.airline IN (:airlineList)
            UNION
            SELECT r.dstn
            FROM routes r
            INNER JOIN flights f ON f.route_id = r.id
            WHERE f.active = true AND f.airline IN (:airlineList)
        )
        AND a.country = :countryCode
        """,
            nativeQuery = true)
    Page<AirportEntity> findAirportsByAirlinesAndCountry(
            @Param("airlineList") List<String> airlineList,
            @Param("countryCode") String countryCode,
            Pageable pageable);

    // 4. Airlines only
    @Query(value = """
        SELECT DISTINCT a.*
        FROM airports a
        WHERE a.iata IN (
            SELECT r.orgn
            FROM routes r
            INNER JOIN flights f ON f.route_id = r.id
            WHERE f.active = true AND f.airline IN (:airlineList)
            UNION
            SELECT r.dstn
            FROM routes r
            INNER JOIN flights f ON f.route_id = r.id
            WHERE f.active = true AND f.airline IN (:airlineList)
        )
        ORDER BY a.iata ASC
        """,
            countQuery = """
        SELECT COUNT(DISTINCT a.iata)
        FROM airports a
        WHERE a.iata IN (
            SELECT r.orgn
            FROM routes r
            INNER JOIN flights f ON f.route_id = r.id
            WHERE f.active = true AND f.airline IN (:airlineList)
            UNION
            SELECT r.dstn
            FROM routes r
            INNER JOIN flights f ON f.route_id = r.id
            WHERE f.active = true AND f.airline IN (:airlineList)
        )
        """,
            nativeQuery = true)
    Page<AirportEntity> findAirportsByAirlinesOnly(
            @Param("airlineList") List<String> airlineList,
            Pageable pageable);
}