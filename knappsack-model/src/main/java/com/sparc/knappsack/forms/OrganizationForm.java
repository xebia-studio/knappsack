package com.sparc.knappsack.forms;

public class OrganizationForm {
    private Long id;
    private String name;
    private Long storageConfigurationId;
    private String storagePrefix;
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

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }
}
