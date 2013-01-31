package com.sparc.knappsack.enums;

public enum Language {
    ENGLISH("language.english", 1),
    FRENCH("language.french", 2),
    SPANISH("language.spanish", 4),
    GERMAN("language.german", 3);

    private final String messageKey;
    private final Integer sortOrder;

    private Language(String messageKey, Integer sortOrder) {
        this.messageKey = messageKey;
        this.sortOrder = sortOrder;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }
}
