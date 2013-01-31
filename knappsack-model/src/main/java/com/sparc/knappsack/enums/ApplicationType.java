package com.sparc.knappsack.enums;

import java.util.ArrayList;
import java.util.List;

public enum ApplicationType {
    ANDROID("applicationType.android", true, MimeType.ANDROID, 1),
    IPHONE("applicationType.iphone", true, MimeType.IOS, 2),
    IPAD("applicationType.ipad", true, MimeType.IOS, 2),
    IOS("applicationType.ios", true, MimeType.IOS, 2),
    CHROME("applicationType.chrome", false, MimeType.CHROME, 3),
    FIREFOX("applicationType.firefox", false, MimeType.FIREFOX, 4),
    WINDOWSPHONE7("applicationType.windowsphone7", true, MimeType.WINPHONE, 5),
    BLACKBERRY("applicationType.blackberry", true, MimeType.BLACKBERRY_COD, 6),
    OTHER("applicationType.other", false, null, 7);

    private final String messageKey;
    private final boolean isMobilePlatform;
    private final MimeType mimeType;
    private final int groupId;

    ApplicationType(String messageKey, boolean isMobilePlatform, MimeType mimeType, int groupId) {
        this.messageKey = messageKey;
        this.isMobilePlatform = isMobilePlatform;
        this.mimeType = mimeType;
        this.groupId = groupId;
    }

    @SuppressWarnings("unused")
    public String getMessageKey() {
        return messageKey;
    }

    public boolean isMobilePlatform() {
        return isMobilePlatform;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public int getGroupId() {
        return groupId;
    }

    public static List<ApplicationType> getAllInGroup(ApplicationType applicationType) {
        List<ApplicationType> applicationTypes = new ArrayList<ApplicationType>();
        if (applicationType != null) {
            for (ApplicationType type : ApplicationType.values()) {
                if (type.groupId == applicationType.groupId) {
                    applicationTypes.add(type);
                }
            }
        }
        return applicationTypes;
    }
}
