package com.sparc.knappsack.forms;


import com.sparc.knappsack.enums.ApplicationType;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class UploadApplication {
    private Long id;
    private Long groupId;
    private String name;
    private String description;
    private Long categoryId;
    private ApplicationType applicationType;
    private MultipartFile icon;
    private List<MultipartFile> screenShots = new ArrayList<MultipartFile>();
    private List<UploadApplicationVersion> applicationVersions = new ArrayList<UploadApplicationVersion>();
    private Long orgStorageConfigId;
    private Long storageConfigurationId;
    private String contextPath;
    private boolean editing;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
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

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public MultipartFile getIcon() {
        return icon;
    }

    public void setIcon(MultipartFile icon) {
        this.icon = icon;
    }

    public List<MultipartFile> getScreenShots() {
        return screenShots;
    }

    public void setScreenShots(List<MultipartFile> screenShots) {
        this.screenShots = screenShots;
    }

    public List<UploadApplicationVersion> getApplicationVersions() {
        return applicationVersions;
    }

    public void setApplicationVersions(List<UploadApplicationVersion> applicationVersions) {
        this.applicationVersions = applicationVersions;
    }

    public Long getOrgStorageConfigId() {
        return orgStorageConfigId;
    }

    public void setOrgStorageConfigId(Long orgStorageConfigId) {
        this.orgStorageConfigId = orgStorageConfigId;
    }

    public Long getStorageConfigurationId() {
        return storageConfigurationId;
    }

    public void setStorageConfigurationId(Long storageConfigurationId) {
        this.storageConfigurationId = storageConfigurationId;
    }
    
    public String getContextPath() {
        return contextPath;
    }
    
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }
}
