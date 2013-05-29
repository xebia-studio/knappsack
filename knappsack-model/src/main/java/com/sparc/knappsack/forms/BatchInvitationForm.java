package com.sparc.knappsack.forms;

import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.models.Contact;

import java.util.ArrayList;
import java.util.List;

public class BatchInvitationForm {

    private UserRole organizationUserRole;
    private List<Long> groupIds = new ArrayList<Long>();
    private List<Contact> contacts = new ArrayList<Contact>();

    public UserRole getOrganizationUserRole() {
        return organizationUserRole;
    }

    public void setOrganizationUserRole(UserRole organizationUserRole) {
        this.organizationUserRole = organizationUserRole;
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

    public List<Contact> getContacts() {
        if (contacts == null) {
            contacts = new ArrayList<Contact>();
        }
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }
}
