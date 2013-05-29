package com.sparc.knappsack.models;

import com.sparc.knappsack.enums.ApplicationType;

public class SQSResignerModel {

    private String bucket;
    private ApplicationType applicationType;
    private String fileToSign;
    private String distributionCert;
    private String distributionKey;
    private String distributionKeyPassword;
    private String distributionProfile;
    private String callbackUrl;

    public SQSResignerModel(String bucket, ApplicationType applicationType, String fileToSign, String distributionCert, String distributionKey, String distributionKeyPassword, String distributionProfile, String callbackUrl) {
        this.bucket = bucket;
        this.applicationType = applicationType;
        this.fileToSign = fileToSign;
        this.distributionCert = distributionCert;
        this.distributionKey = distributionKey;
        this.distributionKeyPassword = distributionKeyPassword;
        this.distributionProfile = distributionProfile;
        this.callbackUrl = callbackUrl;
    }

    public SQSResignerModel() {}

    public String toString() {
        return "bucket: " + bucket +
                "applicationType" + applicationType +
                " fileToSign: " + fileToSign +
                " distributionCert: " + distributionCert +
                " distributionKey: " + distributionKey +
                " distributionKeyPassword: " + distributionKeyPassword +
                " distributionProfile: " + distributionProfile +
                " callbackUrl: " + callbackUrl;        
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public void setFileToSign(String fileToSign) {
        this.fileToSign = fileToSign;
    }

    public void setDistributionCert(String distributionCert) {
        this.distributionCert = distributionCert;
    }

    public void setDistributionKey(String distributionKey) {
        this.distributionKey = distributionKey;
    }

    public void setDistributionKeyPassword(String distributionKeyPassword) {
        this.distributionKeyPassword = distributionKeyPassword;
    }

    public void setDistributionProfile(String distributionProfile) {
        this.distributionProfile = distributionProfile;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getBucket() {
        return bucket;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public String getFileToSign() {
        return fileToSign;
    }

    public String getDistributionCert() {
        return distributionCert;
    }

    public String getDistributionKey() {
        return distributionKey;
    }

    public String getDistributionKeyPassword() {
        return distributionKeyPassword;
    }

    public String getDistributionProfile() {
        return distributionProfile;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }
}
