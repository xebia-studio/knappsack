package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.services.ApplicationService;
import com.sparc.knappsack.components.services.OrganizationService;
import com.sparc.knappsack.components.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequestMapping(value = "/manager/system")
public class SystemController {

    @Qualifier("organizationService")
    @Autowired(required = true)
    private OrganizationService organizationService;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("applicationService")
    @Autowired(required = true)
    private ApplicationService applicationService;

    @RequestMapping
    public String viewSystemManagementPage(Model model) {
        model.addAttribute("organizationCount", organizationService.countAll());
        model.addAttribute("userCount", userService.countAll());
        model.addAttribute("applicationCount", applicationService.countAll());
        return "manager/systemTH";
    }


}
