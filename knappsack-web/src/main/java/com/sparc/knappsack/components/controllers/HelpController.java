package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.models.Contacts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HelpController {

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @RequestMapping(value = "/manager/help")
    public String getHelp() {
        return "manager/managerHelpTH";
    }

    @RequestMapping(value = "/contacts", method = RequestMethod.GET)
    public @ResponseBody
    List<Contacts> getContacts() {
        User user = userService.getUserFromSecurityContext();

        if(user == null) {
            return new ArrayList<Contacts>();
        }
        return userService.getContacts(user);
    }
}
