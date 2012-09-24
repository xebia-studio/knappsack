package com.sparc.knappsack.forms;

import org.springframework.web.multipart.MultipartFile;

public class CategoryForm {

    private Long id;
    private String name;
    private String description;
    private MultipartFile icon;
    private Long organizationId;
    private Long storageConfigurationId;
    private Long orgStorageConfigId;
    private boolean editing;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MultipartFile getIcon() {
        return icon;
    }

    public void setIcon(MultipartFile icon) {
        this.icon = icon;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getStorageConfigurationId() {
        return storageConfigurationId;
    }

    public void setStorageConfigurationId(Long storageConfigurationId) {
        this.storageConfigurationId = storageConfigurationId;
    }

    public Long getOrgStorageConfigId() {
        return orgStorageConfigId;
    }

    public void setOrgStorageConfigId(Long orgStorageConfigId) {
        this.orgStorageConfigId = orgStorageConfigId;
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }
}
