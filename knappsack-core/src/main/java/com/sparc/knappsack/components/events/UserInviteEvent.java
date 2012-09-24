package com.sparc.knappsack.components.events;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.Invitation;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.DomainService;
import com.sparc.knappsack.components.services.EmailService;
import com.sparc.knappsack.components.services.InvitationService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.enums.DomainType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("userInviteEvent")
public class UserInviteEvent implements EventDelivery<Invitation> {

    @Qualifier("emailDeliveryService")
    @Autowired(required = true)
    private EmailService emailService;

    @Qualifier("domainService")
    @Autowired(required = true)
    private DomainService domainService;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("invitationService")
    @Autowired(required = true)
    private InvitationService invitationService;

    @Override
    public boolean sendNotifications(Invitation invitation) {
        boolean success = false;

        if (invitation != null) {

            User fromUser = userService.getUserFromSecurityContext();
            Domain domain = domainService.get(invitation.getDomainId(), invitation.getDomainType());

            User invitee = userService.getByEmail(invitation.getEmail());
            if(invitee != null) {
                if(DomainType.GROUP.equals(invitation.getDomainType())) {
                    userService.addUserToGroup(invitee, invitation.getDomainId(), invitation.getRole().getUserRole());
                } else if(DomainType.ORGANIZATION.equals(invitation.getDomainType())) {
                    userService.addUserToOrganization(invitee, invitation.getDomainId(), invitation.getRole().getUserRole());
                }

                invitationService.delete(invitation.getId());

            }

            success = emailService.sendInvitationEmail(fromUser, invitation.getEmail(), (domain == null ? "" : domain.getName()), invitation.getDomainType());
        }

        return success;
    }
}
