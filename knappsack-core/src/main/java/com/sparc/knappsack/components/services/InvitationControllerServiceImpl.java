package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Invitation;
import com.sparc.knappsack.components.events.EventDelivery;
import com.sparc.knappsack.components.events.EventDeliveryFactory;
import com.sparc.knappsack.enums.EventType;
import com.sparc.knappsack.forms.InviteeForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service("invitationControllerService")
public class InvitationControllerServiceImpl implements InvitationControllerService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Qualifier("invitationService")
    @Autowired(required = true)
    private InvitationService invitationService;

    @Qualifier("eventDeliveryFactory")
    @Autowired(required = true)
    private EventDeliveryFactory eventDeliveryFactory;

    @Override
    public boolean inviteUser(InviteeForm inviteeForm, Long domainId, boolean deleteInvitationOnSendError) {
        boolean userInvited = false;
        if (inviteeForm != null && StringUtils.hasText(inviteeForm.getEmail()) && inviteeForm.getUserRole() != null && domainId != null) {
            Invitation invitation = invitationService.createInvitation(inviteeForm, domainId);

            userInvited = sendInvitation(invitation, deleteInvitationOnSendError);
        }

        return userInvited;
    }

    @Override
    public boolean sendInvitation(Invitation invitation, boolean deleteOnError) {
        boolean invitationSent = false;
        if (invitation != null && invitation.getId() != null && invitation.getId() > 0) {
            EventDelivery deliveryMechanism = eventDeliveryFactory.getEventDelivery(EventType.USER_INVITE);
            try {
                deliveryMechanism.sendNotifications(invitation);
                invitationSent = true;
            } catch (MailException e) {
                log.info("Error during invitation notification.", e);
                if (deleteOnError) {
                    invitationService.delete(invitation.getId());
                }
            }
        }

        return invitationSent;
    }
}
