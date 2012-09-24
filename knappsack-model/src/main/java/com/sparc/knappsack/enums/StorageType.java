package com.sparc.knappsack.enums;

public enum StorageType {
    //Stored on the local server
    LOCAL("storageType.local");

    private final String messageKey;

    private StorageType(String messageKey) {
        this.messageKey = messageKey;
    }

    @SuppressWarnings("unused")
    public String getMessageKey() {
        return messageKey;
    }
}
