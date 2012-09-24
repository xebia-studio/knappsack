package com.sparc.knappsack.components.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController extends AbstractController {

    @RequestMapping(value = "/errorTH.html")
    public String error() {
        return "errorTH";
    }

    @RequestMapping(value = "/404TH.html")
    public String notFound() {
        return "statusCodes/404TH";
    }

    @RequestMapping(value = "/403TH.html")
    public String forbidden() {
        return "statusCodes/403TH";
    }
}
