package org.voyager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.voyager.model.entity.LocationEntity;
import org.voyager.model.location.Location;
import org.voyager.model.location.Source;
import org.voyager.model.location.Status;

import java.util.Collection;
import java.util.List;

public interface LocationRepository extends JpaRepository<LocationEntity,Integer> {
    List<LocationEntity> findByStatusIn(List<Status> statusList);
    List<LocationEntity> findBySource(Source source);
    List<LocationEntity> findBySourceAndStatusIn(Source source, List<Status> statusList);
    List<LocationEntity> findBySourceAndSourceId(Source source, String sourceId);
    List<LocationEntity> findBySourceAndSourceIdAndStatusIn(Source source, String sourceId, List<Status> statusList);
    List<LocationEntity> findBySourceAndSourceIdIn(Source source, List<String> sourceIdList);
    List<LocationEntity> findByCountryCodeIn(List<String> countryCodeList);
    List<LocationEntity> findByStatusInAndCountryCodeIn(List<Status> status, List<String> countryCodeList);
    List<LocationEntity> findBySourceAndCountryCodeIn(Source source, List<String> countryCodeList);
    List<LocationEntity> findBySourceAndSourceIdAndCountryCodeIn(Source source, String sourceId, List<String> countryCodeList);
    List<LocationEntity> findBySourceAndSourceIdAndStatusInAndCountryCodeIn(Source source, String sourceId, List<Status> statusList, List<String> countryCodeList);
}
