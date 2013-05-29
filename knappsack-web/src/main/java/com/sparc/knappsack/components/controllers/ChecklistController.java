package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.ManagerChecklistService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.models.ManagerChecklist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ChecklistController {

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("managerChecklistService")
    @Autowired(required = true)
    private ManagerChecklistService managerChecklistService;

    @RequestMapping(value = "/manager/checklist")
    public
    @ResponseBody
    ManagerChecklist getManagerChecklist() {
        ManagerChecklist managerChecklist = new ManagerChecklist();
        Long orgId;
        User user = userService.getUserFromSecurityContext();
        if (user.getActiveOrganization() != null && user.isActiveOrganizationAdmin()) {
            orgId = user.getActiveOrganization().getId();
            managerChecklist = managerChecklistService.getManagerChecklist(orgId);
        }

        return managerChecklist;
    }
}
