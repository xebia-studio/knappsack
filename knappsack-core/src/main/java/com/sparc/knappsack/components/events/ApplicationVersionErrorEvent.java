package com.sparc.knappsack.components.events;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.components.services.EmailService;
import com.sparc.knappsack.components.services.GroupService;
import com.sparc.knappsack.components.services.OrganizationService;
import com.sparc.knappsack.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("applicationVersionErrorEvent")
public class ApplicationVersionErrorEvent implements EventDelivery<ApplicationVersion> {

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
    public boolean sendNotifications(ApplicationVersion applicationVersion) {
        boolean success = false;

        if (applicationVersion != null) {
            Application parentApplication = applicationVersion.getApplication();
            if (parentApplication != null) {

                //TODO: Refactor to use new Application > Domain relationship
                Group parentGroup = groupService.getOwnedGroup(parentApplication);
                if (parentGroup != null) {

                    // Get all Group admins if any exist
                    List<User> admins = groupService.getAllUsersForRole(parentGroup, UserRole.ROLE_GROUP_ADMIN);

                    // Get all Organization admins if no group admins exist
                    if (admins == null || admins.size() <= 0) {
                        Organization parentOrganization = parentGroup.getOrganization();
                        if (parentOrganization != null) {
                            admins = organizationService.getAllUsersForRole(parentOrganization, UserRole.ROLE_ORG_ADMIN);
                        }
                    }

                    // Send notifications to admins if any exist
                    if (admins != null && admins.size() > 0) {
                        List<Long> adminIds = new ArrayList<Long>();
                        for (User admin : admins) {
                            adminIds.add(admin.getId());
                        }
                        success = emailService.sendApplicationVersionErrorEmail(applicationVersion.getId(), adminIds);
                    }
                }
            }
        }

        return success;
    }
}
