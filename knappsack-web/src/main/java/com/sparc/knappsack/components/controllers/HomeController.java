package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.models.ApplicationModel;
import com.sparc.knappsack.util.UserAgentInfo;
import com.sparc.knappsack.util.WebRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController extends AbstractController {

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String root(UserAgentInfo userAgentInfo, Model model) {
        return home(userAgentInfo, model, null, null, null);
    }

    @PreAuthorize("((#groupId != null ? isUserInDomain(#groupId) : true) and (#categoryId != null ? hasAccessToCategory(#categoryId, #userAgentInfo.applicationType) : true)) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String home(UserAgentInfo userAgentInfo, Model model, @RequestParam(value = "grp", required = false) Long groupId, @RequestParam(value = "ctg", required = false) Long categoryId, @RequestParam(value = "type", required = false) ApplicationType applicationType) {
        User user = userService.getUserFromSecurityContext();

        List<ApplicationModel> applicationModels;
        if ((groupId != null && groupId > 0) || (categoryId != null && categoryId > 0) || applicationType != null) {
            applicationModels = userService.getApplicationsForUserFiltered(user, userAgentInfo.getApplicationType(), groupId, categoryId, applicationType);
        } else {
            applicationModels = userService.getApplicationModelsForUser(user, userAgentInfo.getApplicationType());
        }

        model.addAttribute("applications", applicationModels);

        if (user.getActiveOrganization() == null) {
            model.addAttribute("doesUserRelationshipExist", false);
        } else {
            model.addAttribute("doesUserRelationshipExist", true);
        }

        model.addAttribute("homeURL", WebRequest.getInstance().generateURL("/"));

        model.addAttribute("groupNavActiveIDs", new Long[]{groupId});
        model.addAttribute("categoryNavActiveIDs", new Long[]{categoryId});
        model.addAttribute("typeNavActive", new ApplicationType[]{applicationType});

        return "homeTH";
    }

    @RequestMapping(value = "/disabled", method = RequestMethod.GET)
    public String disabled() {
        return "disabledTH";
    }
}
