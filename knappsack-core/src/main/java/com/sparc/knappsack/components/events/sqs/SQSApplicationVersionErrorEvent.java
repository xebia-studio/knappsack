package com.sparc.knappsack.components.events.sqs;

import com.sparc.knappsack.components.services.EmailService;
import com.sparc.knappsack.enums.ResignErrorType;
import com.sparc.knappsack.models.EmailModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("sqsApplicationVersionErrorEvent")
public class SQSApplicationVersionErrorEvent implements SQSEventDelivery {

    private static final Logger log = LoggerFactory.getLogger(SQSApplicationVersionErrorEvent.class);

    @Qualifier("emailService")
    @Autowired(required = true)
    private EmailService emailService;

    @Override
    public boolean sendNotifications(EmailModel emailModel) {
        boolean success = false;

        if (emailModel != null) {
            try {
                Long applicationVersionId = (Long) emailModel.getParams().get("applicationVersionId");
                Long userId = (Long) emailModel.getParams().get("userId");
                Boolean resignSuccess = (Boolean) emailModel.getParams().get("resignSuccess");

                List<Long> userIds = new ArrayList<Long>();
                userIds.add(userId);
                success = emailService.sendApplicationVersionResignCompleteEmail(applicationVersionId, resignSuccess, ResignErrorType.GENERIC, userIds);
            } catch (ClassCastException e) {
                log.error("Error casting params out of EmailModel:", e);
            }
        }

        return success;
    }
}
