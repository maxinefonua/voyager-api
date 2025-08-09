package org.voyager.service;

import org.voyager.model.language.Language;
import org.voyager.model.language.LanguageForm;

import java.util.List;

public interface LanguageService {
    List<Language> getLanguages();
    Language createLanguage(LanguageForm languageForm);
    Language getLanguageByISO6391(String iso6391);
    Language getLanguageByISO6392(String iso6392);
    Language getLanguageByISO6393(String iso6393);
}
