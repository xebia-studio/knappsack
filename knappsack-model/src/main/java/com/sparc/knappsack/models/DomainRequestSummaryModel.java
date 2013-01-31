package com.sparc.knappsack.models;

public class DomainRequestSummaryModel {

    private long domainId;
    private String domainName;
    private long requestAmount;

    public long getDomainId() {
        return domainId;
    }

    public void setDomainId(long domainId) {
        this.domainId = domainId;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public long getRequestAmount() {
        return requestAmount;
    }

    public void setRequestAmount(long requestAmount) {
        this.requestAmount = requestAmount;
    }
}
