package com.sparc.knappsack.models.api.v1;

import com.sparc.knappsack.enums.ApplicationType;

import java.util.ArrayList;
import java.util.List;

public class Application extends ParentModel {
    private Long id;
    private String name;
    private String description;
    private ApplicationType applicationType;
    private ImageModel icon;
    private List<ImageModel> screenShots = new ArrayList<ImageModel>();
    private Long categoryId;
    private Long groupId;

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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
}
