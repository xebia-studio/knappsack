package com.sparc.knappsack.models;

import com.sparc.knappsack.enums.DomainType;

public class GroupModel extends DomainModel {

    private OrganizationModel organization;

    public OrganizationModel getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationModel organization) {
        this.organization = organization;
    }

    public DomainType getDomainType() {
        return DomainType.GROUP;
    }
}
