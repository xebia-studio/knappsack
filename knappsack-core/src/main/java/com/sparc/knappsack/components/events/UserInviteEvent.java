package com.sparc.knappsack.components.events;

import com.sparc.knappsack.components.entities.Invitation;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.EmailService;
import com.sparc.knappsack.components.services.EmailServiceImpl;
import com.sparc.knappsack.components.services.InvitationService;
import com.sparc.knappsack.components.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

@Component("userInviteEvent")
public class UserInviteEvent implements EventDelivery<List<Invitation>> {

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
    public boolean sendNotifications(List<Invitation> invitations) {
        boolean success = false;

        if (!CollectionUtils.isEmpty(invitations)) {

            User fromUser = userService.getUserFromSecurityContext();
            Long fromUserId = fromUser != null ? fromUser.getId() : null;

            Map<String, List<Invitation>> map = new HashMap<String, List<Invitation>>();
            for (Invitation invitation : invitations) {
                String key = StringUtils.trimAllWhitespace(invitation.getEmail()).toLowerCase();
                if (map.get(key) == null) {
                    map.put(key, new ArrayList<Invitation>());
                }
                map.get(key).add(invitation);
            }

            for (String key : map.keySet()) {
                User invitee = userService.getByEmail(key);
                List<Invitation> invitationList = map.get(key);
                Set<Long> invitationIds = new HashSet<Long>();
                for (Invitation invitation : invitationList) {
                    if (invitee != null) {
                        userService.addUserToDomain(invitee, invitation.getDomain(), invitation.getRole().getUserRole());
                    }
                    invitationIds.add(invitation.getId());
                }
                List<Long> invitationsSent = emailService.sendInvitationsEmail(fromUserId, new ArrayList<Long>(invitationIds));

                if (!CollectionUtils.isEmpty(invitationsSent) && invitee != null && emailService instanceof EmailServiceImpl) {
                    for (Invitation invitation : invitationList) {
                        invitationService.delete(invitation.getId());
                    }
                }

                if (!CollectionUtils.isEmpty(invitationsSent)) {
                    success = true;
                }
            }
        }

        return success;
    }
}
