package com.sparc.knappsack.components.events;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.components.events.composits.ApplicationVersionResignCompleteComposite;
import com.sparc.knappsack.components.events.composits.EventDeliveryWithComposite;
import com.sparc.knappsack.components.services.EmailService;
import com.sparc.knappsack.components.services.GroupService;
import com.sparc.knappsack.components.services.OrganizationService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("applicationVersionResignCompleteEvent")
public class ApplicationVersionResignCompleteEvent implements EventDeliveryWithComposite<ApplicationVersion, ApplicationVersionResignCompleteComposite> {

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("groupService")
    @Autowired(required = true)
    private GroupService groupService;

    @Qualifier("organizationService")
    @Autowired(required = true)
    private OrganizationService organizationService;

    @Qualifier("emailDeliveryService")
    @Autowired(required = true)
    private EmailService emailService;

    @Override
    public boolean sendNotifications(ApplicationVersion applicationVersion, ApplicationVersionResignCompleteComposite applicationVersionResignCompleteComposite) {
        boolean success = false;
        if (applicationVersion != null && applicationVersionResignCompleteComposite != null) {

            Application parentApplication = applicationVersion.getApplication();
            if (parentApplication != null) {

                User user = applicationVersionResignCompleteComposite.getInitiationUser();

                List<User> users = new ArrayList<User>();
                if (user != null && userService.canUserEditApplication(user, parentApplication)) {
                    //Send notification to user to initiated the resigning process
                    users.add(user);
                } else {
                    //No user existed which initiated the resigning process so notify all Group or Organization admins
                    //TODO: Refactor to use new Application > Group relationship
                    Group parentGroup = groupService.getOwnedGroup(parentApplication);
                    if (parentGroup != null) {
                        //Get all group Admins
                        users = groupService.getAllUsersForRole(parentGroup, UserRole.ROLE_GROUP_ADMIN);
                        if (users == null || users.size() <= 0) {
                            //No Group Admins exist so get Organization Admins
                            Organization parentOrganization = parentGroup.getOrganization();
                            if (parentOrganization != null) {
                                users = organizationService.getAllUsersForRole(parentOrganization, UserRole.ROLE_ORG_ADMIN);
                            }
                        }
                    }
                }

                //Send notifications only if users exists
                if (users != null && users.size() > 0) {
                    List<Long> userIds = new ArrayList<Long>();
                    for (User user1 : users) {
                        userIds.add(user1.getId());
                    }
                    success = emailService.sendApplicationVersionResignCompleteEmail(applicationVersion.getId(), applicationVersionResignCompleteComposite.isSuccess(),applicationVersionResignCompleteComposite.getResignErrorType(), userIds);
                }
            }
        }
        return success;
    }
}
