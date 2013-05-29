package com.sparc.knappsack.forms;

import com.sparc.knappsack.enums.UserRole;

import java.util.ArrayList;
import java.util.List;

public class InvitationForm {
    private String email;
    private UserRole organizationUserRole;
    private UserRole groupUserRole;
    private List<Long> groupIds;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getOrganizationUserRole() {
        return organizationUserRole;
    }

    public void setOrganizationUserRole(UserRole organizationUserRole) {
        this.organizationUserRole = organizationUserRole;
    }

    public UserRole getGroupUserRole() {
        return groupUserRole;
    }

    public void setGroupUserRole(UserRole groupUserRole) {
        this.groupUserRole = groupUserRole;
    }

    public List<Long> getGroupIds() {
        if (groupIds == null) {
            groupIds = new ArrayList<Long>();
        }
        return groupIds;
    }

    public void setGroupIds(List<Long> groupIds) {
        this.groupIds = groupIds;
    }
}
