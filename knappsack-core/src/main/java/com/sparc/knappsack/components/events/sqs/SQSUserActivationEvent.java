package com.sparc.knappsack.components.events.sqs;

import com.sparc.knappsack.components.services.EmailService;
import com.sparc.knappsack.models.EmailModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("sqsUserActivationEvent")
public class SQSUserActivationEvent implements SQSEventDelivery {

    private static final Logger log = LoggerFactory.getLogger(SQSUserActivationEvent.class);

    @Qualifier("emailService")
    @Autowired(required = true)
    private EmailService emailService;

    @Override
    public boolean sendNotifications(EmailModel emailModel) {
        boolean success = false;
        if (emailModel != null) {
            try {
                Long userId = (Long) emailModel.getParams().get("userId");
                success = emailService.sendActivationEmail(userId);
            } catch (ClassCastException e) {
                log.error("Error casting params out of EmailModel:", e);
            }
        }
        return success;
    }
}
