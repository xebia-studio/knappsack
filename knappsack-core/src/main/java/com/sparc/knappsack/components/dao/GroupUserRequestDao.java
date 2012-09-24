package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.components.entities.GroupUserRequest;
import com.sparc.knappsack.enums.Status;

import java.util.List;

public interface GroupUserRequestDao extends Dao<GroupUserRequest> {
    /**
     * @param group Group - Get all GroupUserRequest entries for this Group
     * @return List<GroupUserRequest> - all GroupUserRequest entries for this Group regardless of Status
     */
    List<GroupUserRequest> getAllRequests(Group group);

    /**
     * @param group Group - Get all GroupUserRequest entries for this Group
     * @param status Status - Filter the GroupUserRequest by this specific Status
     * @return List<GroupUserRequest> - all GroupUserRequest entries for this Group of this specific status
     */
    List<GroupUserRequest> getAllRequests(Group group, Status status);
}
