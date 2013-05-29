package com.sparc.knappsack.components.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ErrorController extends AbstractController {

    @RequestMapping(value = "/error")
    public String error(Model model, @RequestParam(value = "errorId", required = false) String errorId) {
        if (StringUtils.hasText(errorId)) {
            model.addAttribute("errorId", errorId);
        }
        return "errorTH";
    }

    @RequestMapping(value = "/404")
    public String notFound(Model model, @RequestParam(value = "errorId", required = false) String errorId) {
        if (StringUtils.hasText(errorId)) {
            model.addAttribute("errorId", errorId);
        }
        return "statusCodes/404TH";
    }

    @RequestMapping(value = "/403")
    public String forbidden(Model model, @RequestParam(value = "errorId", required = false) String errorId) {
        if (StringUtils.hasText(errorId)) {
            model.addAttribute("errorId", errorId);
        }
        return "statusCodes/403TH";
    }
}
