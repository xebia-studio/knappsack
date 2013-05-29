package com.sparc.knappsack.forms;


import com.sparc.knappsack.enums.ApplicationType;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class ApplicationForm {
    private Long id;
    private Long groupId;
    private String name;
    private String description;
    private Long categoryId;
    private ApplicationType applicationType;
    private MultipartFile icon;
    private List<MultipartFile> screenshots = new ArrayList<MultipartFile>();
    private ApplicationVersionForm applicationVersion;
    private String contextPath;

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

    public List<MultipartFile> getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(List<MultipartFile> screenshots) {
        this.screenshots = screenshots;
    }

    public ApplicationVersionForm getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(ApplicationVersionForm applicationVersion) {
        this.applicationVersion = applicationVersion;
    }
    
    public String getContextPath() {
        return contextPath;
    }
    
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

}
