package org.voyager.controller;

import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.voyager.model.language.Language;
import org.voyager.model.language.LanguageForm;
import org.voyager.service.LanguageService;
import org.voyager.validate.ValidationUtils;

import java.util.List;

import static org.voyager.utils.ConstantsUtils.*;

public class LanguageController {

    @Autowired
    LanguageService languageService;

    private static final Logger LOGGER = LoggerFactory.getLogger(LanguageController.class);

    @GetMapping("/languages")
    public List<Language> getLanguages() {
        return languageService.getLanguages();
    }

    @GetMapping("/language")
    public Language getLanguage(@RequestParam(name = LANGUAGE_ISO6391_PARAM_NAME,required = false) String iso6391,
                                @RequestParam(name = LANGUAGE_ISO6392_PARAM_NAME,required = false) String iso6392,
                                @RequestParam(name = LANGUAGE_ISO6393_PARAM_NAME,required = false) String iso6393) {
        ValidationUtils.validateGetLanguageParams(iso6391,iso6392,iso6393);
        if (StringUtils.isNotBlank(iso6391))
            return languageService.getLanguageByISO6391(iso6391.toLowerCase());
        if (StringUtils.isNotBlank(iso6392))
            return languageService.getLanguageByISO6392(iso6392.toLowerCase());
        return languageService.getLanguageByISO6393(iso6393.toLowerCase());
    }

    @PostMapping("/languages")
    public Language addLanguage(@RequestBody(required = false) @Valid LanguageForm languageForm,
                                BindingResult bindingResult) {
        ValidationUtils.validateLanguageForm(languageForm,bindingResult);
        return languageService.createLanguage(languageForm);
    }
}
