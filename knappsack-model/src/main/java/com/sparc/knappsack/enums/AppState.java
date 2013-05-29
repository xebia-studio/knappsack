package com.sparc.knappsack.enums;

import java.util.ArrayList;
import java.util.List;

public enum AppState {
    //Available to the Group only
    GROUP_PUBLISH("appState.group.publish", true, true, true, true),
    //Available to the organization
    ORGANIZATION_PUBLISH("appState.org.publish", false, true, true, true),
    //Not available to any domains
    DISABLED("appState.disabled", true, true, true, false),
    //Request that the AppState be changed to ORGANIZATION_PUBLISH
    ORG_PUBLISH_REQUEST("appState.org.publish.request", true, false, true, true),
    //Currently resigning so not available to anyone
    RESIGNING("appState.resigning", false, false, false, false),
    //Generic Error state.  No one can download.
    ERROR("appState.error", false, false, false, false);

    private final String messageKey;
    private final boolean isAvailableToGroup;
    private final boolean isAvailableToOrganization;
    private final boolean userSelectable;
    private final boolean downloadable;

    private AppState(String messageKey, boolean isAvailableToGroup, boolean isAvailableToOrganization, boolean userSelectable, boolean downloadable) {
        this.messageKey = messageKey;
        this.isAvailableToGroup = isAvailableToGroup;
        this.isAvailableToOrganization = isAvailableToOrganization;
        this.userSelectable = userSelectable;
        this.downloadable = downloadable;
    }

    @SuppressWarnings("unused")
    public String getMessageKey() {
        return messageKey;
    }

    @SuppressWarnings("unused")
    public boolean isAvailableToGroup() {
        return isAvailableToGroup;
    }

    @SuppressWarnings("unused")
    public boolean isAvailableToOrganization() {
        return isAvailableToOrganization;
    }

    @SuppressWarnings("unused")
    public boolean isUserSelectable() {
        return userSelectable;
    }

    @SuppressWarnings("unused")
    public boolean isDownloadable() {
        return downloadable;
    }

    public static List<AppState> getAllUserSelectable() {
        List<AppState> userSelectable = new ArrayList<AppState>();
        for (AppState appState : AppState.values()) {
            if (appState.isUserSelectable()) {
                userSelectable.add(appState);
            }
        }
        return userSelectable;
    }

    public static List<AppState> getAllDownloadable() {
        List<AppState> downloadable = new ArrayList<AppState>();
        for (AppState appState : AppState.values()) {
            if (appState.isDownloadable()) {
                downloadable.add(appState);
            }
        }
        return downloadable;
    }
}
