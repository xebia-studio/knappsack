package com.sparc.knappsack.components.events.sqs;

import com.sparc.knappsack.components.services.EmailService;
import com.sparc.knappsack.models.EmailModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("sqsDomainAccessRequestEvent")
public class SQSDomainAccessRequestEvent implements SQSEventDelivery {

    @Qualifier("emailService")
    @Autowired(required = true)
    private EmailService emailService;

    @Override
    public boolean sendNotifications(EmailModel emailModel) {
        boolean success = false;
        if (emailModel != null) {
            Long domainRequestId = (Long) emailModel.getParams().get("domainRequestId");
            success = emailService.sendDomainAccessRequestEmail(domainRequestId);
        }
        return success;
    }
}
