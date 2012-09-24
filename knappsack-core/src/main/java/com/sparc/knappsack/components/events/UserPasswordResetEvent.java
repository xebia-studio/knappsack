package com.sparc.knappsack.components.events;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.events.composits.EventDeliveryWithComposite;
import com.sparc.knappsack.components.events.composits.UserPasswordResetComposite;
import com.sparc.knappsack.components.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("userPasswordResetEvent")
public class UserPasswordResetEvent implements EventDeliveryWithComposite<User, UserPasswordResetComposite> {

    @Qualifier("emailDeliveryService")
    @Autowired(required = true)
    private EmailService emailService;

    @Override
    public boolean sendNotifications(User user, UserPasswordResetComposite composite) {
        boolean success = false;

        if (user != null && composite != null) {
            success = emailService.sendPasswordResetEmail(user, composite.getPassword());
        }

        return success;
    }

}
