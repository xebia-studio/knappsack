package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.services.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

@Controller
public class BrowseController extends AbstractController {

    @Qualifier("applicationService")
    @Autowired
    private ApplicationService applicationService;

    @RequestMapping(value = "/browse", method = RequestMethod.GET)
    public String applications(Model model) {
        model.addAttribute("applications", applicationService.getAll());

        List<String> imageList = new ArrayList<String>();
        imageList.add("/resources/img/knappsack.png");
        imageList.add("/resources/img/google-logo.png");
        imageList.add("/resources/img/openidlogosmall.png");
        model.addAttribute("images", imageList);

        return "browse";
    }
}
