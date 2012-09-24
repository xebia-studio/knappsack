package com.sparc.knappsack.components.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller class for all pages for legal documents pertaining to Knappsack
 * <p/>
 *
 * @author Tim Koski
 */
@Controller
@RequestMapping(value = "/legal")
public class LegalController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(LegalController.class);

    @RequestMapping
    public String showLegalRoot() {
        return "legal/privacyTH";
    }

    @RequestMapping(value = "/termsOfUse", method = RequestMethod.GET)
    public String showTermsOfUsePage(Model model) {

        log.info("Received request to show terms of use page");

        return "legal/termsOfUseTH";
    }

    @RequestMapping(value = "/privacy", method = RequestMethod.GET)
    public String showPrivacyPage(Model model) {

        log.info("Received request to show privacy page");

        return "legal/privacyTH";
    }
}
