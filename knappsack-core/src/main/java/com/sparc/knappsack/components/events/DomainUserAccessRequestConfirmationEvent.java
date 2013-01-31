package com.sparc.knappsack.components.events;

import com.sparc.knappsack.components.entities.DomainUserRequest;
import com.sparc.knappsack.components.services.DomainEntityService;
import com.sparc.knappsack.components.services.DomainEntityServiceFactory;
import com.sparc.knappsack.components.services.EmailService;
import com.sparc.knappsack.models.DomainModel;
import com.sparc.knappsack.models.DomainUserRequestModel;
import com.sparc.knappsack.models.GroupModel;
import com.sparc.knappsack.models.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("domainUserAccessRequestConfirmationEvent")
public class DomainUserAccessRequestConfirmationEvent implements EventDelivery<DomainUserRequest> {

    @Qualifier("emailDeliveryService")
    @Autowired(required = true)
    private EmailService emailService;

    @Qualifier("domainEntityServiceFactory")
    @Autowired(required = true)
    private DomainEntityServiceFactory domainEntityServiceFactory;

    @Override
    public boolean sendNotifications(DomainUserRequest domainUserRequest) {
        boolean success = false;

        if (domainUserRequest != null) {
            DomainUserRequestModel domainUserRequestModel = new DomainUserRequestModel();
            domainUserRequestModel.setId(domainUserRequest.getId());
            domainUserRequestModel.setStatus(domainUserRequest.getStatus());

            UserModel userModel = new UserModel();
            userModel.setId(domainUserRequest.getUser().getId());
            userModel.setUserName(domainUserRequest.getUser().getUsername());
            userModel.setEmail(domainUserRequest.getUser().getEmail());
            userModel.setFirstName(domainUserRequest.getUser().getFirstName());
            userModel.setLastName(domainUserRequest.getUser().getLastName());
            domainUserRequestModel.setUser(userModel);

            DomainEntityService domainEntityService = domainEntityServiceFactory.getDomainEntityService(domainUserRequest.getDomain().getDomainType());
            DomainModel domainModel = domainEntityService.createDomainModel(domainUserRequest.getDomain());
            domainUserRequestModel.setDomain(domainModel);

            success = emailService.sendDomainUserAccessConfirmationEmail(domainUserRequestModel);
        }

        return success;
    }
}
