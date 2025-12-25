package org.voyager.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);
    @Value("${app.version}")
    String appVersion;

    @Value("${app.name}")
    String appName;

    @Value("${app.description}")
    String appDescription;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("appName", appName);
        model.addAttribute("appVersion", appVersion);
        model.addAttribute("appDescription", appDescription);
        return "index";
    }
}
