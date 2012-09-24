package com.sparc.knappsack.enums;

public enum ApplicationType {
    ANDROID("applicationType.android", true, MimeType.ANDROID),
    IPHONE("applicationType.iphone", true, MimeType.IOS),
    IPAD("applicationType.ipad", true, MimeType.IOS),
    IOS("applicationType.ios", true, MimeType.IOS),
    CHROME("applicationType.chrome", false, MimeType.CHROME),
    FIREFOX("applicationType.firefox", false, MimeType.FIREFOX),
    WINDOWSPHONE7("applicationType.windowsphone7", true, MimeType.WINPHONE),
    BLACKBERRY("applicationType.blackberry", true, MimeType.BLACKBERRY_COD),
    OTHER("applicationType.other", false, null);

    private final String messageKey;
    private final boolean isMobilePlatform;
    private final MimeType mimeType;

    ApplicationType(String messageKey, boolean isMobilePlatform, MimeType mimeType) {
        this.messageKey = messageKey;
        this.isMobilePlatform = isMobilePlatform;
        this.mimeType = mimeType;
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
}
