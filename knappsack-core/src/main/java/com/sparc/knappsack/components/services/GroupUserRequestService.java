package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.GroupUserRequest;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.enums.Status;
import com.sparc.knappsack.enums.UserRole;

import java.util.List;

public interface GroupUserRequestService extends EntityService<GroupUserRequest> {

    GroupUserRequest createGroupUserRequest(User user, String accessCode);

    List<GroupUserRequest> getAll(Long groupId);

    List<GroupUserRequest> getAll(Long groupId, Status status);

    boolean acceptRequest(GroupUserRequest groupUserRequest, UserRole userRole);

    boolean declineRequest(GroupUserRequest groupUserRequest);
}
