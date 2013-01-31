package com.sparc.knappsack.components.events;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("userAccountActivationSuccessEvent")
public class UserAccountActivationSuccessEvent implements EventDelivery<User> {

    @Qualifier("emailDeliveryService")
    @Autowired(required = true)
    private EmailService emailService;

    @Override
    public boolean sendNotifications(User user) {
        boolean success = false;
        if (user != null) {
            success = emailService.sendActivationSuccessEmail(user.getId());
        }

        return success;
    }
}
