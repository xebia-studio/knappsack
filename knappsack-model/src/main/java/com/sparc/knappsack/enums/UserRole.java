package com.sparc.knappsack.enums;

import java.util.ArrayList;
import java.util.List;

public enum UserRole {
    //Knappsack administrator
    ROLE_ADMIN("role.admin", null, false),
    //Knappsack user (non administrator)
    ROLE_USER("role.user", null, false),
    //User for the Group domain
    ROLE_GROUP_USER("role.group.user", DomainType.GROUP, true),
    //Administer of the Group domain
    ROLE_GROUP_ADMIN("role.group.admin", DomainType.GROUP, true),
    //Administer of the Organization domain
    ROLE_ORG_ADMIN("role.org.admin", DomainType.ORGANIZATION, true),
    //Non-administer user of the Organization domain
    ROLE_ORG_USER("role.org.user", DomainType.ORGANIZATION, true),
    //User for a Group but not the Organization
    ROLE_ORG_GUEST("role.org.guest", DomainType.ORGANIZATION, false);

    private final String messageKey;
    private final DomainType domainType;
    private final boolean selectable;

    private UserRole(String messageKey, DomainType domainType, boolean selectable) {
        this.messageKey = messageKey;
        this.domainType = domainType;
        this.selectable = selectable;
    }

    @SuppressWarnings("unused")
    public String getMessageKey() {
        return messageKey;
    }

    @SuppressWarnings("unused")
    public DomainType getDomainType() {
        return domainType;
    }

    @SuppressWarnings("unused")
    public boolean isSelectable() {
        return selectable;
    }

    @SuppressWarnings("unused")
    public static List<UserRole> getAllSelectableForDomainType(DomainType domainType) {
        List<UserRole> userRoles = new ArrayList<UserRole>();
        for (UserRole userRole : values()) {
            if (userRole.domainType != null && userRole.domainType.equals(domainType) && userRole.isSelectable()) {
                userRoles.add(userRole);
            }
        }
        return userRoles;
    }

    @SuppressWarnings("unused")
    public static List<UserRole> getAllForDomainType(DomainType domainType) {
        List<UserRole> userRoles = new ArrayList<UserRole>();
        for (UserRole userRole : values()) {
            if (userRole.domainType != null && userRole.domainType.equals(domainType)) {
                userRoles.add(userRole);
            }
        }
        return userRoles;
    }

    @SuppressWarnings("unused")
    private static List<UserRole> getSelectable() {
        List<UserRole> userRoles = new ArrayList<UserRole>();
        for (UserRole userRole : values()) {
            if (userRole.isSelectable()) {
                userRoles.add(userRole);
            }
        }
        return userRoles;
    }
}
