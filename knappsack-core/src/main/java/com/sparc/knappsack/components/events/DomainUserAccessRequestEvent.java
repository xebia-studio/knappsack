package com.sparc.knappsack.components.events;

import com.sparc.knappsack.components.entities.DomainUserRequest;
import com.sparc.knappsack.components.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("domainUserAccessRequestEvent")
public class DomainUserAccessRequestEvent implements EventDelivery<DomainUserRequest> {

    @Qualifier("emailDeliveryService")
    @Autowired(required = true)
    private EmailService emailService;

    @Override
    public boolean sendNotifications(DomainUserRequest domainUserRequest) {
        boolean success = false;

        if (domainUserRequest != null) {
            success = emailService.sendDomainUserAccessRequestEmail(domainUserRequest.getId());
        }

        return success;
    }
}
