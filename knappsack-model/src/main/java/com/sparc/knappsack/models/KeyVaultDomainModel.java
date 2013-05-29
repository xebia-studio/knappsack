package com.sparc.knappsack.models;

import java.util.ArrayList;
import java.util.List;

public class KeyVaultDomainModel {

    private DomainModel parentDomain;
    private List<DomainModel> childDomains;

    public DomainModel getParentDomain() {
        return parentDomain;
    }

    public void setParentDomain(DomainModel parentDomain) {
        this.parentDomain = parentDomain;
    }

    public List<DomainModel> getChildDomains() {
        if (childDomains == null) {
            childDomains = new ArrayList<DomainModel>();
        }
        return childDomains;
    }

    public void setChildDomains(List<DomainModel> childDomains) {
        this.childDomains = childDomains;
    }
}
