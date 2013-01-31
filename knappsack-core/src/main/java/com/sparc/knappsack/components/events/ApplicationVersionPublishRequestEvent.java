package com.sparc.knappsack.components.events;

import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.EmailService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.models.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("applicationVersionPublishRequestEvent")
public class ApplicationVersionPublishRequestEvent implements EventDelivery<ApplicationVersion> {

    @Qualifier("emailDeliveryService")
    @Autowired(required = true)
    private EmailService emailService;

    @Autowired(required = true)
    private UserService userService;

    @Override
    public boolean sendNotifications(ApplicationVersion applicationVersion) {
        boolean success = false;

        User principal = userService.getUserFromSecurityContext();

        if (applicationVersion != null && principal != null) {
            UserModel userModel = new UserModel();
            userModel.setEmail(principal.getEmail());
            userModel.setFirstName(principal.getFirstName());
            userModel.setLastName(principal.getLastName());
            userModel.setId(principal.getId());
            userModel.setUserName(principal.getUsername());
            success = emailService.sendApplicationPublishRequestEmail(applicationVersion.getId(), userModel);
        }

        return success;
    }
}
