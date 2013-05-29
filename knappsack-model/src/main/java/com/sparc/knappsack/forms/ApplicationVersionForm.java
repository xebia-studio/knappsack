package com.sparc.knappsack.forms;

import com.sparc.knappsack.enums.AppState;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class ApplicationVersionForm {
    private Long id;
    private Long parentId;
    private List<Long> guestGroupIds = new ArrayList<Long>();
    private String versionName;
    private String recentChanges;
    private MultipartFile appFile;
    private MultipartFile provisioningProfile;
    private AppState appState;
    private Long keyVaultEntryId;
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

    public List<Long> getGuestGroupIds() {
        if (guestGroupIds == null) {
            guestGroupIds = new ArrayList<Long>();
        }
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

    public Long getKeyVaultEntryId() {
        return keyVaultEntryId;
    }

    public void setKeyVaultEntryId(Long keyVaultEntryId) {
        this.keyVaultEntryId = keyVaultEntryId;
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }
}
