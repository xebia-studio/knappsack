package com.sparc.knappsack.forms;

import com.sparc.knappsack.enums.ApplicationType;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class KeyVaultEntryForm {

    //Generic fields
    private Long id;
    private String name;
    private List<Long> childDomainIds;
    private ApplicationType applicationType;

    //IOS fields
    private MultipartFile distributionCert;
    private MultipartFile distributionKey;
    private MultipartFile distributionProfile;
    private String distributionKeyPassword;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Long> getChildDomainIds() {
        if (childDomainIds == null) {
            childDomainIds = new ArrayList<Long>();
        }
        return childDomainIds;
    }

    public void setChildDomainIds(List<Long> childDomainIds) {
        this.childDomainIds = childDomainIds;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

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
}
