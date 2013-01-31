package com.sparc.knappsack.models;

import com.sparc.knappsack.enums.ApplicationType;

import java.util.ArrayList;
import java.util.List;

public class ApplicationModel {

    private Long id;
    private String name;
    private String description;
    private ApplicationType applicationType;
    private List<ApplicationVersionModel> versions;
    private ImageModel icon;
    private List<ImageModel> screenShots = new ArrayList<ImageModel>();
    private Long groupId;
    private String groupName;
    private boolean canUserEdit;

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

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public List<ApplicationVersionModel> getVersions() {
        return versions;
    }

    public void setVersions(List<ApplicationVersionModel> versions) {
        this.versions = versions;
    }

    public ImageModel getIcon() {
        return icon;
    }

    public void setIcon(ImageModel icon) {
        this.icon = icon;
    }

    public List<ImageModel> getScreenShots() {
        return screenShots;
    }

    public void setScreenShots(List<ImageModel> screenShots) {
        this.screenShots = screenShots;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean isCanUserEdit() {
        return canUserEdit;
    }

    public void setCanUserEdit(boolean canUserEdit) {
        this.canUserEdit = canUserEdit;
    }
}
