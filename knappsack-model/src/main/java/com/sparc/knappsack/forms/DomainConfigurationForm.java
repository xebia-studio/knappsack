package com.sparc.knappsack.forms;

import com.sparc.knappsack.enums.DomainType;

public class DomainConfigurationForm {

    private Long id;
    private Long domainId;
    private DomainType domainType;
    private boolean disabled;
    private boolean disableLimitValidations;
    private long userLimit;
    private long applicationLimit;
    private long applicationVersionLimit;
    private long megabyteStorageLimit;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(Long domainId) {
        this.domainId = domainId;
    }

    public DomainType getDomainType() {
        return domainType;
    }

    public void setDomainType(DomainType domainType) {
        this.domainType = domainType;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isDisableLimitValidations() {
        return disableLimitValidations;
    }

    public void setDisableLimitValidations(boolean disableLimitValidations) {
        this.disableLimitValidations = disableLimitValidations;
    }

    public long getUserLimit() {
        return userLimit;
    }

    public void setUserLimit(long userLimit) {
        this.userLimit = userLimit;
    }

    public long getApplicationLimit() {
        return applicationLimit;
    }

    public void setApplicationLimit(long applicationLimit) {
        this.applicationLimit = applicationLimit;
    }

    public long getApplicationVersionLimit() {
        return applicationVersionLimit;
    }

    public void setApplicationVersionLimit(long applicationVersionLimit) {
        this.applicationVersionLimit = applicationVersionLimit;
    }

    public long getMegabyteStorageLimit() {
        return megabyteStorageLimit;
    }

    public void setMegabyteStorageLimit(long megabyteStorageLimit) {
        this.megabyteStorageLimit = megabyteStorageLimit;
    }
}
