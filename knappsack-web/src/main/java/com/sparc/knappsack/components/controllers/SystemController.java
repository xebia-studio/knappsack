package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.services.ApplicationService;
import com.sparc.knappsack.components.services.OrganizationService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.enums.SystemNotificationSeverity;
import com.sparc.knappsack.enums.SystemNotificationType;
import com.sparc.knappsack.forms.SystemNotificationForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

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
    public String viewSystemManagementPage(HttpServletRequest request, Model model, @RequestParam(value = "notifUpdateSuccess", required = false) Boolean notificationUpdateSuccess) {
        model.addAttribute("organizationCount", organizationService.countAll());
        model.addAttribute("userCount", userService.countAll());
        model.addAttribute("applicationCount", applicationService.countAll());
        model.addAttribute("systemNotificationTypes", SystemNotificationType.values());
        model.addAttribute("systemNotificationSeverityTypes", SystemNotificationSeverity.values());

        if (!model.containsAttribute("systemNotificationForm")) {
            model.addAttribute("systemNotificationForm", new SystemNotificationForm());
        }

        if (notificationUpdateSuccess != null) {
            model.addAttribute("notificationUpdateSuccess", notificationUpdateSuccess.booleanValue());
        }
        return "manager/systemTH";
    }


}
