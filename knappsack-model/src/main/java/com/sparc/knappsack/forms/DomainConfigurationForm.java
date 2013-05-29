package com.sparc.knappsack.forms;

public class DomainConfigurationForm {

    private Long id;
    private Long domainId;
    private boolean disabled;
    private boolean disableLimitValidations;
    private long userLimit;
    private long applicationLimit;
    private long applicationVersionLimit;
    private long megabyteStorageLimit;
    private boolean monitorBandwidth;
    private long megabyteBandwidthLimit;
    private boolean applicationResignerEnabled;
    private boolean customBrandingEnabled;

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

    public boolean isMonitorBandwidth() {
        return monitorBandwidth;
    }

    public void setMonitorBandwidth(boolean monitorBandwidth) {
        this.monitorBandwidth = monitorBandwidth;
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

    public long getMegabyteBandwidthLimit() {
        return megabyteBandwidthLimit;
    }

    public void setMegabyteBandwidthLimit(long megabyteBandwidthLimit) {
        this.megabyteBandwidthLimit = megabyteBandwidthLimit;
    }

    public boolean isApplicationResignerEnabled() {
        return applicationResignerEnabled;
    }

    public void setApplicationResignerEnabled(boolean applicationResignerEnabled) {
        this.applicationResignerEnabled = applicationResignerEnabled;
    }

    public boolean isCustomBrandingEnabled() {
        return customBrandingEnabled;
    }

    public void setCustomBrandingEnabled(boolean customBrandingEnabled) {
        this.customBrandingEnabled = customBrandingEnabled;
    }
}
