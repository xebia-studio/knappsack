package com.sparc.knappsack.models;

public class DomainStatisticsModel {
    long totalUsers;
    long totalPendingInvitations;
    long totalApplications;
    long totalApplicationVersions;
    double totalMegabyteStorageAmount;
    double totalMegabyteBandwidthUsed;

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalPendingInvitations() {
        return totalPendingInvitations;
    }

    public void setTotalPendingInvitations(long totalPendingInvitations) {
        this.totalPendingInvitations = totalPendingInvitations;
    }

    public long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(long totalApplications) {
        this.totalApplications = totalApplications;
    }

    public long getTotalApplicationVersions() {
        return totalApplicationVersions;
    }

    public void setTotalApplicationVersions(long totalApplicationVersions) {
        this.totalApplicationVersions = totalApplicationVersions;
    }

    public double getTotalMegabyteStorageAmount() {
        return totalMegabyteStorageAmount;
    }

    public void setTotalMegabyteStorageAmount(double totalMegabyteStorageAmount) {
        this.totalMegabyteStorageAmount = totalMegabyteStorageAmount;
    }

    public double getTotalMegabyteBandwidthUsed() {
        return totalMegabyteBandwidthUsed;
    }

    public void setTotalMegabyteBandwidthUsed(double totalMegabyteBandwidthUsed) {
        this.totalMegabyteBandwidthUsed = totalMegabyteBandwidthUsed;
    }
}
