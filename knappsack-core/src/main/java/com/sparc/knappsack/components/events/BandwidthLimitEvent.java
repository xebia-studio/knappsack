package com.sparc.knappsack.components.events;

import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.services.EmailService;
import com.sparc.knappsack.components.services.OrganizationService;
import com.sparc.knappsack.models.UserDomainModel;
import com.sparc.knappsack.models.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("bandwidthLimitEvent")
public class BandwidthLimitEvent implements EventDelivery<Organization> {

    @Autowired(required = true)
    private OrganizationService organizationService;

    @Autowired(required = true)
    private EmailService emailService;

    @Override
    public boolean sendNotifications(Organization organization) {
        if (organization != null) {
            List<UserDomainModel> userDomains = organizationService.getAllOrganizationAdmins(organization.getId());
            List<UserModel> users = new ArrayList<UserModel>();
            for (UserDomainModel userDomain : userDomains) {
                users.add(userDomain.getUser());
            }
            return emailService.sendBandwidthLimitNotification(organization.getId(), users);
        }
        return false;
    }
}
