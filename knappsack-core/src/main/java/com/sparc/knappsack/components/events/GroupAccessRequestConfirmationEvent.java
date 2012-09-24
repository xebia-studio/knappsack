package com.sparc.knappsack.components.events;

import com.sparc.knappsack.components.entities.GroupUserRequest;
import com.sparc.knappsack.components.services.EmailService;
import com.sparc.knappsack.models.GroupModel;
import com.sparc.knappsack.models.GroupUserRequestModel;
import com.sparc.knappsack.models.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("groupAccessRequestConfirmationEvent")
public class GroupAccessRequestConfirmationEvent implements EventDelivery<GroupUserRequest> {

    @Qualifier("emailDeliveryService")
    @Autowired(required = true)
    private EmailService emailService;

    @Override
    public boolean sendNotifications(GroupUserRequest groupUserRequest) {
        boolean success = false;

        if (groupUserRequest != null) {
            GroupUserRequestModel groupUserRequestModel = new GroupUserRequestModel();
            groupUserRequestModel.setId(groupUserRequest.getId());
            groupUserRequestModel.setStatus(groupUserRequest.getStatus());

            UserModel userModel = new UserModel();
            userModel.setId(groupUserRequest.getUser().getId());
            userModel.setUserName(groupUserRequest.getUser().getUsername());
            userModel.setEmail(groupUserRequest.getUser().getEmail());
            userModel.setFirstName(groupUserRequest.getUser().getFirstName());
            userModel.setLastName(groupUserRequest.getUser().getLastName());
            groupUserRequestModel.setUser(userModel);

            GroupModel groupModel = new GroupModel();
            groupModel.setId(groupUserRequest.getGroup().getId());
            groupModel.setName(groupUserRequest.getGroup().getName());
            groupUserRequestModel.setGroup(groupModel);

            success = emailService.sendGroupAccessConfirmationEmail(groupUserRequestModel);
        }

        return success;
    }
}
