package com.sparc.knappsack.enums;

public enum DomainType {
    GROUP("domain.group"),
    ORGANIZATION("domain.organization");

    private final String messageKey;

    private DomainType(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
