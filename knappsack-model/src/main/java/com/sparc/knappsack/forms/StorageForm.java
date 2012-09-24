package com.sparc.knappsack.forms;

import com.sparc.knappsack.enums.StorageType;

public class StorageForm {
    private Long id;
    private String name;
    private String baseLocation;
    private StorageType storageType;
    private String prefix = "";
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

    public String getBaseLocation() {
        return baseLocation;
    }

    public void setBaseLocation(String baseLocation) {
        this.baseLocation = baseLocation;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }
}
