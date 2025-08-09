package org.voyager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.voyager.model.entity.LanguageEntity;

import java.util.List;

public interface LanguageRepository extends JpaRepository<LanguageEntity,String> {

    List<LanguageEntity> findByIso6391(String iso6391);
    List<LanguageEntity> findByIso6392(String iso6392);
    List<LanguageEntity> findByIso6393(String iso6393);
}
