package com.sparc.knappsack.models;

import com.sparc.knappsack.enums.ApplicationType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class KeyVaultEntryModel {
    private Long id;
    private String name;
    private DomainModel parentDomain;
    private List<DomainModel> childDomains;
    private ApplicationType applicationType;
    private Date createDate;
    private UserModel createdBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DomainModel getParentDomain() {
        return parentDomain;
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

    public void setParentDomain(DomainModel parentDomain) {
        this.parentDomain = parentDomain;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public UserModel getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserModel createdBy) {
        this.createdBy = createdBy;
    }
}
