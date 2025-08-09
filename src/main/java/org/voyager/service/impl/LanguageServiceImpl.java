package org.voyager.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.error.MessageConstants;
import org.voyager.model.entity.LanguageEntity;
import org.voyager.model.language.Language;
import org.voyager.model.language.LanguageForm;
import org.voyager.repository.LanguageRepository;
import org.voyager.service.LanguageService;
import org.voyager.service.utils.MapperUtils;

import java.util.List;

import static org.voyager.error.MessageConstants.INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE;
import static org.voyager.service.utils.ServiceUtils.handleJPAExceptions;
import static org.voyager.utils.ConstantsUtils.*;

@Service
public class LanguageServiceImpl implements LanguageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LanguageServiceImpl.class);

    @Autowired
    LanguageRepository languageRepository;

    @Override
    public List<Language> getLanguages() {
        return languageRepository.findAll().stream().map(MapperUtils::entityToLanguage).toList();
    }

    @Override
    public Language createLanguage(LanguageForm languageForm) {
        return handleJPAExceptions(()-> {
                LanguageEntity toSave = MapperUtils.formToLanguageEntity(languageForm);
                return MapperUtils.entityToLanguage(languageRepository.save(toSave));
        });
    }

    @Override
    public Language getLanguageByISO6391(String iso6391) {
        return handleJPAExceptions(()->{
            List<LanguageEntity> languageEntities = languageRepository.findByIso6391(iso6391);
            if (languageEntities.size() > 1) {
                LOGGER.error(String.format("ISO 639-1 code '%s' returned multiple languages when duplicates should not exist. " +
                        "Investigation required.",iso6391));
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE);
            }
            if (languageEntities.isEmpty()) {
                LOGGER.error(String.format("ISO 639-1 code '%s' returned multiple languages when duplicates should not exist. " +
                        "Verify correct lookup details.",iso6391));
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        MessageConstants.buildResourceNotFoundForParameterMessage(LANGUAGE_ISO6391_PARAM_NAME,iso6391));
            }
            return MapperUtils.entityToLanguage(languageEntities.get(0));
        });
    }

    @Override
    public Language getLanguageByISO6392(String iso6392) {
        List<LanguageEntity> languageEntities = languageRepository.findByIso6392(iso6392);
        if (languageEntities.size() > 1) {
            LOGGER.error(String.format("ISO 639-2 code '%s' returned multiple languages when duplicates should not exist. " +
                    "Investigation required.",iso6392));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE);
        }
        if (languageEntities.isEmpty()) {
            LOGGER.error(String.format("ISO 639-2 code '%s' returned multiple languages when duplicates should not exist. " +
                    "Verify correct lookup details.",iso6392));
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    MessageConstants.buildResourceNotFoundForParameterMessage(LANGUAGE_ISO6392_PARAM_NAME,iso6392));
        }
        return MapperUtils.entityToLanguage(languageEntities.get(0));
    }

    @Override
    public Language getLanguageByISO6393(String iso6393) {
        List<LanguageEntity> languageEntities = languageRepository.findByIso6393(iso6393);
        if (languageEntities.size() > 1) {
            LOGGER.error(String.format("ISO 639-3 code '%s' returned multiple languages when duplicates should not exist. " +
                    "Investigation required.",iso6393));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE);
        }
        if (languageEntities.isEmpty()) {
            LOGGER.error(String.format("ISO 639-3 code '%s' returned multiple languages when duplicates should not exist. " +
                    "Verify correct lookup details.",iso6393));
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    MessageConstants.buildResourceNotFoundForParameterMessage(LANGUAGE_ISO6393_PARAM_NAME,iso6393));
        }
        return MapperUtils.entityToLanguage(languageEntities.get(0));
    }
}
