package com.sparc.knappsack.models;

public class DomainStatisticsModel {
    int totalUsers;
    int totalApplications;
    int totalApplicationVersions;
    double totalMegabyteStorageAmount;

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(int totalApplications) {
        this.totalApplications = totalApplications;
    }

    public int getTotalApplicationVersions() {
        return totalApplicationVersions;
    }

    public void setTotalApplicationVersions(int totalApplicationVersions) {
        this.totalApplicationVersions = totalApplicationVersions;
    }

    public double getTotalMegabyteStorageAmount() {
        return totalMegabyteStorageAmount;
    }

    public void setTotalMegabyteStorageAmount(double totalMegabyteStorageAmount) {
        this.totalMegabyteStorageAmount = totalMegabyteStorageAmount;
    }
}
