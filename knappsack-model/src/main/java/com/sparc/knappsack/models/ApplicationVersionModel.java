package com.sparc.knappsack.models;

import com.sparc.knappsack.enums.AppState;

public class ApplicationVersionModel {

    private Long id;
    private String versionName;
    private String recentChanges;
    private AppFileModel installationFile;
    private String cfBundleIdentifier;
    private String cfBundleVersion;
    private String cfBundleName;
    private AppState appState;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public AppFileModel getInstallationFile() {
        return installationFile;
    }

    public void setInstallationFile(AppFileModel installationFile) {
        this.installationFile = installationFile;
    }

    public String getCfBundleIdentifier() {
        return cfBundleIdentifier;
    }

    public void setCfBundleIdentifier(String cfBundleIdentifier) {
        this.cfBundleIdentifier = cfBundleIdentifier;
    }

    public String getCfBundleVersion() {
        return cfBundleVersion;
    }

    public void setCfBundleVersion(String cfBundleVersion) {
        this.cfBundleVersion = cfBundleVersion;
    }

    public String getCfBundleName() {
        return cfBundleName;
    }

    public void setCfBundleName(String cfBundleName) {
        this.cfBundleName = cfBundleName;
    }

    public AppState getAppState() {
        return appState;
    }

    public void setAppState(AppState appState) {
        this.appState = appState;
    }
}
