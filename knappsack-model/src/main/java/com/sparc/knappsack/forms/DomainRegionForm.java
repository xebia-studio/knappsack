package com.sparc.knappsack.forms;

import java.util.HashSet;
import java.util.Set;

public class DomainRegionForm {
    private Long id;
    private Long domainId;
    private String name;
    private Set<String> emails;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getEmails() {
        if (emails == null) {
            emails = new HashSet<String>();
        }
        return emails;
    }

    public void setEmails(Set<String> emails) {
        this.emails = emails;
    }
}
