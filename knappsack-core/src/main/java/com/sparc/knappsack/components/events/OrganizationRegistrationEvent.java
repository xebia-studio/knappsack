package com.sparc.knappsack.components.events;

import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.events.composits.EventDeliveryWithComposite;
import com.sparc.knappsack.components.events.composits.OrganizationRegistrationComposite;
import com.sparc.knappsack.components.services.EmailService;
import com.sparc.knappsack.models.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("organizationRegistrationEvent")
public class OrganizationRegistrationEvent implements EventDeliveryWithComposite<Organization, OrganizationRegistrationComposite> {

    @Qualifier("emailDeliveryService")
    @Autowired(required = true)
    private EmailService emailService;

    @Override
    public boolean sendNotifications(Organization organization, OrganizationRegistrationComposite organizationRegistrationComposite) {
        boolean success = false;

        if (organization != null && organizationRegistrationComposite != null) {
            UserModel userModel = new UserModel();
            userModel.setId(organizationRegistrationComposite.getUserId());
            userModel.setFirstName(organizationRegistrationComposite.getFirstName());
            userModel.setLastName(organizationRegistrationComposite.getLastName());
            userModel.setEmail(organizationRegistrationComposite.getEmail());
            userModel.setUserName(organizationRegistrationComposite.getUserName());

            success = emailService.sendOrganizationRegistrationEmail(organization.getId(), userModel);
        }

        return success;
    }
}
