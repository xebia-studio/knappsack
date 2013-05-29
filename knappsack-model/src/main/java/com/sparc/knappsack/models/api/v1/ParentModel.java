package com.sparc.knappsack.models.api.v1;

public abstract class ParentModel {

    private Long activeOrganizationId;
    private String activeOrganizationName;

    public Long getActiveOrganizationId() {
        return activeOrganizationId;
    }

    public void setActiveOrganizationId(Long activeOrganizationId) {
        this.activeOrganizationId = activeOrganizationId;
    }

    public String getActiveOrganizationName() {
        return activeOrganizationName;
    }

    public void setActiveOrganizationName(String activeOrganizationName) {
        this.activeOrganizationName = activeOrganizationName;
    }
}
