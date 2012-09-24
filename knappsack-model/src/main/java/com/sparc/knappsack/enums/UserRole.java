package com.sparc.knappsack.enums;

public enum UserRole {
    //Knappsack administrator
    ROLE_ADMIN("role.admin"),
    //Knappsack user (non administrator)
    ROLE_USER("role.user"),
    //User for the Group domain
    ROLE_GROUP_USER("role.group.user"),
    //Administer of the Group domain
    ROLE_GROUP_ADMIN("role.group.admin"),
    //Administer of the Organization domain
    ROLE_ORG_ADMIN("role.org.admin"),
    //Non-adminster user of the Organization domain
    ROLE_ORG_USER("role.org.user");

    private final String messageKey;

    private UserRole(String messageKey) {
        this.messageKey = messageKey;
    }

    @SuppressWarnings("unused")
    public String getMessageKey() {
        return messageKey;
    }
}
