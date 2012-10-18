package com.sparc.knappsack.enums;

public enum SystemNotificationSeverity {
    INFO(1),
    WARNING(2),
    ERROR(3);

    private SystemNotificationSeverity(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    private Integer sortOrder;

    public int getSortOrder() {
        return sortOrder;
    }
}
