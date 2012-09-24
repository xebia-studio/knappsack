package com.sparc.knappsack.enums;

public enum LoginError {

    INVALID_CREDENTIALS("login.error.invalidCredentials"),
    OTHER("login.error.other");

    private final String messageKey;

    LoginError(String messageKey) {
        this.messageKey = messageKey;
    }

    @SuppressWarnings("unused")
    public String getMessageKey() {
        return messageKey;
    }
}
