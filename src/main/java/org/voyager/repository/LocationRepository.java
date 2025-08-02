package org.voyager.repository;

import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.voyager.model.entity.LocationEntity;
import org.voyager.model.location.Source;
import org.voyager.model.location.Status;

import java.util.Collection;
import java.util.List;

public interface LocationRepository extends JpaRepository<LocationEntity,Integer> {
    List<LocationEntity> findBySourceAndSourceId(Source source, String sourceId);
    List<LocationEntity> findBySourceAndSourceIdIn(Source source, List<String> sourceIdList);

    List<LocationEntity> findByStatusIn(List<Status> statusList);
    List<LocationEntity> findByStatusIn(List<Status> statusList, Pageable pageable);

    List<LocationEntity> findBySource(Source source);
    List<LocationEntity> findBySource(Source source,Pageable pageable);

    List<LocationEntity> findBySourceAndStatusIn(Source source, List<Status> statusList);
    List<LocationEntity> findBySourceAndStatusIn(Source source, List<Status> statusList, Pageable pageable);

    List<LocationEntity> findByCountryCodeIn(List<String> countryCodeList);
    List<LocationEntity> findByCountryCodeIn(List<String> countryCodeList, Pageable pageable);

    List<LocationEntity> findByStatusInAndCountryCodeIn(List<Status> status, List<String> countryCodeList);
    List<LocationEntity> findByStatusInAndCountryCodeIn(List<Status> status, List<String> countryCodeList, Pageable pageable);

    List<LocationEntity> findBySourceAndCountryCodeIn(Source source, List<String> countryCodeList);
    List<LocationEntity> findBySourceAndCountryCodeIn(Source source, List<String> countryCodeList, Pageable pageable);

    List<LocationEntity> findBySourceAndStatusInAndCountryCodeIn(Source source, List<Status> statusList, List<String> countryCodeList);
    List<LocationEntity> findBySourceAndStatusInAndCountryCodeIn(Source source, List<Status> statusList, List<String> countryCodeList, Pageable pageable);
}
