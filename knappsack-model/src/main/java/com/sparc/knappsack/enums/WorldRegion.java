package com.sparc.knappsack.enums;

public enum WorldRegion {
    NORTH_AMERICA("worldRegion.north_america"),
    EUROPE_ASIA("worldRegion.europe_asia");

    private final String messageKey;

    private WorldRegion(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
