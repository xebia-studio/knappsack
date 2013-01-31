package com.sparc.knappsack.forms;

import com.sparc.knappsack.enums.DeviceType;
import com.sparc.knappsack.enums.Language;

import java.util.HashSet;
import java.util.Set;

public class DomainRequestForm {
    private String domainUUID;
    private String firstName;
    private String lastName;
    private String companyName;
    private String phoneNumber;
    private String emailAddress;
    private String address;
    private DeviceType deviceType;
    private Long region;
    private Set<Language> languages;

    public String getDomainUUID() {
        return domainUUID;
    }

    public void setDomainUUID(String domainUUID) {
        this.domainUUID = domainUUID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public Long getRegion() {
        return region;
    }

    public void setRegion(Long region) {
        this.region = region;
    }

    public Set<Language> getLanguages() {
        if (languages == null) {
            languages = new HashSet<Language>();
        }
        return languages;
    }

    public void setLanguages(Set<Language> languages) {
        this.languages = languages;
    }
}
