package com.sparc.knappsack.enums;

public enum StorageType {
    //Stored on the local server
    LOCAL("storageType.local", false);

    //The message properties key for this StorageType
    private final String messageKey;
    //Specifies if the StorageType is not the server running Knappsack
    private final boolean remote;

    private StorageType(String messageKey, boolean remote) {
        this.messageKey = messageKey;
        this.remote = remote;
    }

    @SuppressWarnings("unused")
    public String getMessageKey() {
        return messageKey;
    }

    public boolean isRemote() {
        return remote;
    }
}
