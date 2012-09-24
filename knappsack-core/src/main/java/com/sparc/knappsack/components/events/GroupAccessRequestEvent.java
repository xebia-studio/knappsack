package com.sparc.knappsack.components.events;

import com.sparc.knappsack.components.entities.GroupUserRequest;
import com.sparc.knappsack.components.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("groupAccessRequestEvent")
public class GroupAccessRequestEvent implements EventDelivery<GroupUserRequest> {

    @Qualifier("emailDeliveryService")
    @Autowired(required = true)
    private EmailService emailService;

    @Override
    public boolean sendNotifications(GroupUserRequest groupUserRequest) {
        boolean success = false;

        if (groupUserRequest != null) {
            success = emailService.sendGroupAccessRequestEmail(groupUserRequest);
        }

        return success;
    }
}
