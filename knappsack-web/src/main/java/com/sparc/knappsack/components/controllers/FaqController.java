package com.sparc.knappsack.components.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FaqController extends AbstractController {

    @RequestMapping(value = "/faq")
    public String showFaqPage() {
        return "faqTH";
    }
}
