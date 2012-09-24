package com.sparc.knappsack.forms;

import com.sparc.knappsack.enums.AppState;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class UploadApplicationVersion {
    private Long id;
    private Long parentId;
    private Long groupId;
    private List<Long> guestGroupIds = new ArrayList<Long>();
    private String versionName;
    private String recentChanges;
    private MultipartFile appFile;
    private MultipartFile provisioningProfile;
    private AppState appState;
    private Long storageConfigurationId;
    private boolean editing;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public List<Long> getGuestGroupIds() {
        return guestGroupIds;
    }

    public void setGuestGroupIds(List<Long> guestGroupIds) {
        this.guestGroupIds = guestGroupIds;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getRecentChanges() {
        return recentChanges;
    }

    public void setRecentChanges(String recentChanges) {
        this.recentChanges = recentChanges;
    }

    public MultipartFile getAppFile() {
        return appFile;
    }

    public void setAppFile(MultipartFile appFile) {
        this.appFile = appFile;
    }

    public MultipartFile getProvisioningProfile() {
        return provisioningProfile;
    }

    public void setProvisioningProfile(MultipartFile provisioningProfile) {
        this.provisioningProfile = provisioningProfile;
    }

    public AppState getAppState() {
        return appState;
    }

    public void setAppState(AppState appState) {
        this.appState = appState;
    }

    public Long getStorageConfigurationId() {
        return storageConfigurationId;
    }

    public void setStorageConfigurationId(Long storageConfigurationId) {
        this.storageConfigurationId = storageConfigurationId;
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }
}
