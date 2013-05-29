package com.sparc.knappsack.models;

public class IOSKeyVaultEntryModel {

    private Long id;
    private AppFileModel installFile;
    private AppFileModel distributionCert;
    private AppFileModel distributionKey;
    private String distributionKeyPassword;
    private AppFileModel distributionProfile;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AppFileModel getInstallFile() {
        return installFile;
    }

    public void setInstallFile(AppFileModel installFile) {
        this.installFile = installFile;
    }

    public AppFileModel getDistributionCert() {
        return distributionCert;
    }

    public void setDistributionCert(AppFileModel distributionCert) {
        this.distributionCert = distributionCert;
    }

    public AppFileModel getDistributionKey() {
        return distributionKey;
    }

    public void setDistributionKey(AppFileModel distributionKey) {
        this.distributionKey = distributionKey;
    }

    public String getDistributionKeyPassword() {
        return distributionKeyPassword;
    }

    public void setDistributionKeyPassword(String distributionKeyPassword) {
        this.distributionKeyPassword = distributionKeyPassword;
    }

    public AppFileModel getDistributionProfile() {
        return distributionProfile;
    }

    public void setDistributionProfile(AppFileModel distributionProfile) {
        this.distributionProfile = distributionProfile;
    }
}
