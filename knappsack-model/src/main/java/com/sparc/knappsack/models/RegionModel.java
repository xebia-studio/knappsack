package com.sparc.knappsack.models;

import java.util.Set;
import java.util.TreeSet;

public class RegionModel {
    private Long id;
    private String name;
    private Set<String> emails;

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

    public Set<String> getEmails() {
        if (emails == null) {
            emails = new TreeSet<String>();
        }
        return emails;
    }

    public void setEmails(Set<String> emails) {
        this.emails = emails;
    }
}
