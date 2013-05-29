package com.sparc.knappsack.enums;

public enum ResignErrorType {
    GENERIC("resignErrorType.generic"),
    IOS_INVALID_PASSWORD("resignErrorType.ios_invalid_password"),
    IOS_INVALID_KEY("resignErrorType.ios_invalid_key"),
    IOS_INVALID_CERTIFICATE("resignErrorType.ios_invalid_certificate"),
    IOS_INVALID_PROFILE("resignErrorType.ios_invalid_profile"),
    IOS_INVALID_IPA("resignErrorType.ios_invalid_ipa");

    private final String messageKey;

    ResignErrorType(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
