package org.voyager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.voyager.model.entity.LocationEntity;
import org.voyager.model.location.Location;
import org.voyager.model.location.Source;
import org.voyager.model.location.Status;

import java.util.Collection;
import java.util.List;

public interface LocationRepository extends JpaRepository<LocationEntity,Integer> {
    List<LocationEntity> findByStatus(Status status);
    List<LocationEntity> findByStatusIn(List<Status> statusList);
    List<LocationEntity> findBySource(Source source);
    List<LocationEntity> findBySourceAndStatus(Source source, Status status);
    List<LocationEntity> findBySourceAndSourceId(Source source, String sourceId);
    List<LocationEntity> findBySourceAndSourceIdAndStatus(Source source, String sourceId, Status status);
    List<LocationEntity> findBySourceAndSourceIdIn(Source source, List<String> sourceIdList);
    List<LocationEntity> findByCountryCodeIn(List<String> countryCodeList);
    List<LocationEntity> findByStatusAndCountryCodeIn(Status status, List<String> countryCodeList);
    List<LocationEntity> findBySourceAndCountryCodeIn(Source source, List<String> countryCodeList);
    List<LocationEntity> findBySourceAndSourceIdAndCountryCodeIn(Source source, String sourceId, List<String> countryCodeList);
    List<LocationEntity> findBySourceAndSourceIdAndStatusAndCountryCodeIn(Source source, String sourceId, Status status, List<String> countryCodeList);
}
