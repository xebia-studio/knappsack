package com.sparc.knappsack.enums.api.v1;

public enum SystemStatistics {
    ORGANIZATION_COUNT("Total number of organizations in the system"),
    USER_COUNT("Total number of users in the system"),
    APPLICATION_COUNT("Total number of applications in the system");

    private String description;

    SystemStatistics(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
