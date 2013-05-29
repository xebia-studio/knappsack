package com.sparc.knappsack.models;

public class ManagerChecklist {

    //TODO: possibly add a check categories
    private Long organizationId;
    private Long groupId;
    private Long applicationId;
    private boolean hasOrganizationUsers;
    private boolean hasGroups;
    private boolean hasApps;
    private boolean hasAppVersions;
    private boolean hasGroupUsers;

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public boolean getHasOrganizationUsers() {
        return hasOrganizationUsers;
    }

    public void setHasOrganizationUsers(boolean hasOrganizationUsers) {
        this.hasOrganizationUsers = hasOrganizationUsers;
    }

    public boolean getHasGroups() {
        return hasGroups;
    }

    public void setHasGroups(boolean hasGroups) {
        this.hasGroups = hasGroups;
    }

    public boolean getHasApps() {
        return hasApps;
    }

    public void setHasApps(boolean hasApps) {
        this.hasApps = hasApps;
    }

    public boolean getHasAppVersions() {
        return hasAppVersions;
    }

    public void setHasAppVersions(boolean hasAppVersions) {
        this.hasAppVersions = hasAppVersions;
    }

    public boolean getHasGroupUsers() {
        return hasGroupUsers;
    }

    public void setHasGroupUsers(boolean hasGroupUsers) {
        this.hasGroupUsers = hasGroupUsers;
    }

    public boolean isGroupNeeded() {
        return !hasGroups;
    }

    public boolean isAppNeeded() {
        return hasGroups && groupId != null && !hasApps;
    }

    public boolean isAppVersionNeeded() {
        return hasApps && applicationId != null && !hasAppVersions;
    }

    public boolean isOrganizationUserNeeded() {
        return !hasOrganizationUsers;
    }

    public String getInviteOrganizationUsersURL() {
        return "/manager/inviteUser";
    }

    public String getCreateGroupURL() {
        return "/manager/addGroup";
    }

    public String getCreateApplicationURL() {
        return "/manager/addApplication?grp=" + groupId;
    }

    public String getCreateApplicationVersionURL() {
        return "/manager/addVersion/" + applicationId;
    }

    public boolean isComplete() {
        return !isAppNeeded() && !isAppVersionNeeded() && !isGroupNeeded() && !isOrganizationUserNeeded();
    }

    public boolean isNotComplete() {
        return !isComplete();
    }

    public int getPercentComplete() {
        int a = hasApps ? 1 : 0;
        int b = hasAppVersions ? 1 : 0;
        int c = hasGroups ? 1 : 0;
        int d = isOrganizationUserNeeded() ? 0 : 1;

        return (int) (((a + b + c + d) / 4.0) * 100);
    }
}
