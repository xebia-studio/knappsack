package com.sparc.knappsack.enums;

public enum SystemNotificationType {
    MAINTENANCE(1);

    private SystemNotificationType(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    private Integer sortOrder;

    public int getSortOrder() {
        return sortOrder;
    }
}
