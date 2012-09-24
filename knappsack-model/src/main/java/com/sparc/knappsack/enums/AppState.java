package com.sparc.knappsack.enums;

public enum AppState {
    //Available to the Group only
    GROUP_PUBLISH("group.publish", true),
    //Available to the organization
    ORGANIZATION_PUBLISH("org.publish", false),
    //Not available to any domains
    DISABLED("disabled", true),
    //Request that the AppState be changed to ORGANIZATION_PUBLISH
    ORG_PUBLISH_REQUEST("org.publish.request", true);

    private final String messageKey;
    private final boolean isAvailableToGroup;

    private AppState(String messageKey, boolean isAvailableToGroup) {
        this.messageKey = messageKey;
        this.isAvailableToGroup = isAvailableToGroup;
    }

    @SuppressWarnings("unused")
    public String getMessageKey() {
        return messageKey;
    }

    @SuppressWarnings("unused")
    public boolean isAvailableToGroup() {
        return isAvailableToGroup;
    }
}
