package com.sparc.knappsack.components.events.sqs;

import com.sparc.knappsack.components.services.EmailService;
import com.sparc.knappsack.components.services.EmailServiceImpl;
import com.sparc.knappsack.components.services.InvitationService;
import com.sparc.knappsack.models.EmailModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component("sqsUserInviteEvent")
public class SQSUserInviteEvent implements SQSEventDelivery {

    private static final Logger log = LoggerFactory.getLogger(SQSUserInviteEvent.class);

    @Qualifier("emailService")
    @Autowired(required = true)
    private EmailService emailService;

    @Qualifier("emailDeliveryService")
    @Autowired(required = true)
    private EmailService emailDeliveryService;

    @Qualifier("invitationService")
    @Autowired(required = true)
    private InvitationService invitationService;

    @Override
    public boolean sendNotifications(EmailModel emailModel) {
        boolean success = false;
        if (emailModel != null) {
            try {
                Long fromUserId = (Long) emailModel.getParams().get("userId");
                List<Long> invitationIds = (List<Long>) emailModel.getParams().get("invitationIds");

                // Attempt to send all invitations and get back a list of successful invitations
                List<Long> invitationsSent = emailService.sendInvitationsEmail(fromUserId, invitationIds);

                if (!CollectionUtils.isEmpty(invitationsSent)) {

                    for (Long invitationId : invitationsSent) {

                        // Delete Invitation if emailService used is not the SQS service
                        if (emailService instanceof EmailServiceImpl) {
                            invitationService.deleteInvitation(invitationId);
                        }

                        // Remove successful invitation from original list so that original list can be used to retry unsent invitations
                        invitationIds.remove(invitationId);
                    }
                }

                if (!CollectionUtils.isEmpty(invitationIds)) {
                    // Attempt to resend existing invitations which were not sent
                    success = emailDeliveryService.sendInvitationsEmail(fromUserId, invitationIds).size() == invitationIds.size();
                } else {
                    // All invitations were removed from the original list so assume all were sent properly
                    success = true;
                }


            } catch (ClassCastException e) {
                log.info("Error casting params out of EmailModel:", e);
            }
        }

        return success;
    }
}
