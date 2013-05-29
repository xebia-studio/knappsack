package com.sparc.knappsack.models.api.v1;

import com.sparc.knappsack.enums.AppState;

public class ApplicationVersion extends ParentModel {
    private Long id;
    private String versionName;
    private String recentChanges;
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

    public AppState getAppState() {
        return appState;
    }

    public void setAppState(AppState appState) {
        this.appState = appState;
    }
}
