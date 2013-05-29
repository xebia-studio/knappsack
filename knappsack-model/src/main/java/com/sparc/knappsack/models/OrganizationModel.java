package com.sparc.knappsack.models;

import com.sparc.knappsack.enums.DomainType;

import java.util.Date;

public class OrganizationModel extends DomainModel {

    private Long storageConfigurationId;
    private String storagePrefix;
    private Date createDate;

    public Long getStorageConfigurationId() {
        return storageConfigurationId;
    }

    public void setStorageConfigurationId(Long storageConfigurationId) {
        this.storageConfigurationId = storageConfigurationId;
    }

    public String getStoragePrefix() {
        return storagePrefix;
    }

    public void setStoragePrefix(String storagePrefix) {
        this.storagePrefix = storagePrefix;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public DomainType getDomainType() {
        return DomainType.ORGANIZATION;
    }
}
