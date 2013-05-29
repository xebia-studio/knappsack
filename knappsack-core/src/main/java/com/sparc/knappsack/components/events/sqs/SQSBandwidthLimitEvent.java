package com.sparc.knappsack.components.events.sqs;

import com.sparc.knappsack.components.services.EmailService;
import com.sparc.knappsack.models.EmailModel;
import com.sparc.knappsack.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("sqsBandwidthLimitEvent")
public class SQSBandwidthLimitEvent implements SQSEventDelivery {

    private static final Logger log = LoggerFactory.getLogger(SQSBandwidthLimitEvent.class);

    @Qualifier("emailService")
    @Autowired(required = true)
    private EmailService emailService;

    @Override
    public boolean sendNotifications(EmailModel emailModel) {
        boolean success = false;
        if (emailModel != null) {
            try {
                Long organizationId = (Long) emailModel.getParams().get("organizationId");
                UserModel userModel = (UserModel) emailModel.getParams().get("userModel");

                List<UserModel> userModelList = new ArrayList<UserModel>();
                userModelList.add(userModel);

                success = emailService.sendBandwidthLimitNotification(organizationId, userModelList);
            } catch (ClassCastException e) {
                log.info("Error casting params out of EmailModel:", e);
            }
        }
        return success;
    }
}
