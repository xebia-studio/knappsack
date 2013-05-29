package com.sparc.knappsack.components.events.sqs;

import com.sparc.knappsack.components.services.EmailService;
import com.sparc.knappsack.enums.Status;
import com.sparc.knappsack.models.DomainUserRequestModel;
import com.sparc.knappsack.models.EmailModel;
import com.sparc.knappsack.models.GroupModel;
import com.sparc.knappsack.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("sqsDomainUserAccessRequestConfirmationEvent")
public class SQSDomainUserAccessRequestConfirmationEvent implements SQSEventDelivery {

    private static final Logger log = LoggerFactory.getLogger(SQSDomainUserAccessRequestConfirmationEvent.class);

    @Qualifier("emailService")
    @Autowired(required = true)
    private EmailService emailService;

    @Override
    public boolean sendNotifications(EmailModel emailModel) {
        boolean success = false;
        if (emailModel != null) {
            try {
                DomainUserRequestModel domainUserRequestModel;
                Map<String, Object> modelMap = (Map<String, Object>) emailModel.getParams().get("domainUserRequestModel");
                if (modelMap != null) {
                    Map<String, Object> groupModelMap = (Map<String, Object>) modelMap.get("domain");
                    if (groupModelMap != null) {
                        domainUserRequestModel = new DomainUserRequestModel();
                        Integer domainUserRequestId = (Integer) modelMap.get("id");
                        if (domainUserRequestId != null) {
                            domainUserRequestModel.setId(domainUserRequestId.longValue());
                        }

                        UserModel userModel = null;
                        Map<String, Object> userModelMap = (Map<String, Object>) modelMap.get("user");
                        if (userModelMap != null) {
                            userModel = new UserModel();
                            userModel.setEmail((String) userModelMap.get("email"));
                            Integer id = (Integer) userModelMap.get("id");
                            if (id != null) {
                                userModel.setId(id.longValue());
                            }
                            userModel.setFirstName((String) userModelMap.get("firstName"));
                            userModel.setLastName((String) userModelMap.get("lastName"));
                            userModel.setUserName((String) userModelMap.get("userName"));

                            domainUserRequestModel.setUser(userModel);
                        }

                        GroupModel groupModel = new GroupModel();
                        Integer groupId = (Integer) groupModelMap.get("id");
                        if (groupId != null) {
                            groupModel.setId(groupId.longValue());
                        }
                        groupModel.setName((String) groupModelMap.get("name"));
                        domainUserRequestModel.setDomain(groupModel);

                        domainUserRequestModel.setStatus(Status.valueOf((String) modelMap.get("status")));
                        if (domainUserRequestModel != null && domainUserRequestModel.getUser() != null) {
                            success = emailService.sendDomainUserAccessConfirmationEmail(domainUserRequestModel);
                        }
                    }
                }

            } catch (ClassCastException e) {
                log.info("Error casting params out of EmailModel:", e);
            }
        }
        return success;    }
}
