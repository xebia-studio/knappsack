package com.sparc.knappsack.components.events;

import com.sparc.knappsack.components.entities.DomainRequest;
import com.sparc.knappsack.components.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("domainAccessRequestEvent")
public class DomainAccessRequestEvent implements EventDelivery<DomainRequest> {

    private static final Logger log = LoggerFactory.getLogger(DomainAccessRequestEvent.class);

    @Qualifier("emailDeliveryService")
    @Autowired(required = true)
    private EmailService emailService;

    @Override
    public boolean sendNotifications(DomainRequest domainRequest) {
        boolean success = false;

        if (domainRequest != null) {
            success = emailService.sendDomainAccessRequestEmail(domainRequest.getId());
        }

        return success;
    }
}
