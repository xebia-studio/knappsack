package com.sparc.knappsack.models;

import java.util.Date;

public class OrganizationModel {

    private Long id;
    private String name;
    private Long storageConfigurationId;
    private String storagePrefix;
    private Long paymentPlanId;
    private Date createDate;
    private CustomerModel customer;

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

    public CustomerModel getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerModel customerModel) {
        this.customer = customerModel;
    }
}
