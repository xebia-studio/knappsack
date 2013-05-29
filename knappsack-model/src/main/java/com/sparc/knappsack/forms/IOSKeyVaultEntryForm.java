package com.sparc.knappsack.forms;

import com.sparc.knappsack.enums.ApplicationType;
import org.springframework.web.multipart.MultipartFile;

public class IOSKeyVaultEntryForm extends AbstractKeyVaultEntryForm {
    private MultipartFile distributionCert;
    private MultipartFile distributionKey;
    private MultipartFile distributionProfile;
    private String distributionKeyPassword;

    public MultipartFile getDistributionCert() {
        return distributionCert;
    }

    public void setDistributionCert(MultipartFile distributionCert) {
        this.distributionCert = distributionCert;
    }

    public MultipartFile getDistributionKey() {
        return distributionKey;
    }

    public void setDistributionKey(MultipartFile distributionKey) {
        this.distributionKey = distributionKey;
    }

    public MultipartFile getDistributionProfile() {
        return distributionProfile;
    }

    public void setDistributionProfile(MultipartFile distributionProfile) {
        this.distributionProfile = distributionProfile;
    }

    public String getDistributionKeyPassword() {
        return distributionKeyPassword;
    }

    public void setDistributionKeyPassword(String distributionKeyPassword) {
        this.distributionKeyPassword = distributionKeyPassword;
    }

    @Override
    public ApplicationType getApplicationType() {
        return ApplicationType.IOS;
    }
}
