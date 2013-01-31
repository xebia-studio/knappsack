package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.Category;
import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.models.ApplicationModel;
import com.sparc.knappsack.util.UserAgentInfo;
import com.sparc.knappsack.util.WebRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
public class HomeController extends AbstractController {

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String root(UserAgentInfo userAgentInfo, Model model) {
        return home(userAgentInfo, model);
    }

    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String home(UserAgentInfo userAgentInfo, Model model) {

        User user = userService.getUserFromSecurityContext();

        List<ApplicationModel> applicationModels = userService.getApplicationModelsForUser(user, userAgentInfo.getApplicationType(), AppState.ORGANIZATION_PUBLISH, AppState.GROUP_PUBLISH, (user.isOrganizationAdmin() ? AppState.ORG_PUBLISH_REQUEST : null));
        model.addAttribute("applications", applicationModels);

        List<Group> userGroups =  userService.getGroups(user);
        if (userService.getOrganizations(user).isEmpty() && userGroups.isEmpty()) {
            model.addAttribute("doesUserRelationshipExist", false);
        } else {
            model.addAttribute("doesUserRelationshipExist", true);
        }

        List<Category> categories = userService.getCategoriesForUser(user, userAgentInfo.getApplicationType());
        model.addAttribute("categories", categories);
        model.addAttribute("homeURL", WebRequest.getInstance().generateURL("/"));

        return "homeTH";
    }
}
