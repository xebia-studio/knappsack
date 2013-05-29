package com.sparc.knappsack.models;

public class ApplicationVersionPublishRequestModel {

    private ApplicationModel application;
    private ApplicationVersionModel applicationVersion;
    private OrganizationModel organization;
    private Long groupId;
    private String groupName;

    public ApplicationModel getApplication() {
        return application;
    }

    public void setApplication(ApplicationModel application) {
        this.application = application;
    }

    public ApplicationVersionModel getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(ApplicationVersionModel applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public OrganizationModel getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationModel organization) {
        this.organization = organization;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
