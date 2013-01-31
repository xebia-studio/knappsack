package com.sparc.knappsack.models;

/**
 * This model represents the total number of downloads for a given application version.
 */
public class ApplicationVersionStatisticSummaryModel {

    private Long applicationVersionId;
    private String applicationName;
    private String applicationVersionName;
    private int total;

    public Long getApplicationVersionId() {
        return applicationVersionId;
    }

    public void setApplicationVersionId(Long applicationVersionId) {
        this.applicationVersionId = applicationVersionId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationVersionName() {
        return applicationVersionName;
    }

    public void setApplicationVersionName(String applicationVersionName) {
        this.applicationVersionName = applicationVersionName;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
