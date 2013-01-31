package com.sparc.knappsack.components.events;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.components.services.EmailService;
import com.sparc.knappsack.components.services.EmailServiceImpl;
import com.sparc.knappsack.components.services.InvitationService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.enums.DomainType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("userInviteEvent")
public class UserInviteEvent implements EventDelivery<Invitation> {

    @Qualifier("emailDeliveryService")
    @Autowired(required = true)
    private EmailService emailService;

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
            Domain domain = invitation.getDomain();

            if (domain != null) {

                User invitee = userService.getByEmail(invitation.getEmail());
                Long fromUserId = fromUser != null ? fromUser.getId() : null;
                success = emailService.sendInvitationEmail(fromUserId, invitation.getId());
                if(invitee != null) {
                    if(DomainType.GROUP.equals(domain.getDomainType())) {
                        userService.addUserToGroup(invitee, (Group) domain, invitation.getRole().getUserRole());
                    } else if(DomainType.ORGANIZATION.equals(domain.getDomainType())) {
                        userService.addUserToOrganization(invitee, (Organization) domain, invitation.getRole().getUserRole());
                    }

                    if (emailService instanceof EmailServiceImpl) {
                        invitationService.delete(invitation.getId());
                    }
                }
            }
        }

        return success;
    }
}
