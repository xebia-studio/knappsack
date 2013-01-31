package com.sparc.knappsack.models;

import java.util.ArrayList;
import java.util.List;

public class Contacts {
    private String domainName;
    private List<Contact> contacts;

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public List<Contact> getContacts() {
        if(contacts == null) {
            contacts = new ArrayList<Contact>();
        }
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }
}
