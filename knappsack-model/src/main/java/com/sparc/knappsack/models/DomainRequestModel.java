package com.sparc.knappsack.models;

import com.sparc.knappsack.enums.DeviceType;
import com.sparc.knappsack.enums.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class DomainRequestModel {

    private long id;
    private long domainId;
    private String firstName = "";
    private String lastName = "";
    private String companyName = "";
    private String phoneNumber = "";
    private String address = "";
    private String emailAddress = "";
    private DeviceType deviceType;
    private Set<Language> languages;
    private RegionModel region = new RegionModel();
    private List<DomainModel> assignableDomains = new ArrayList<DomainModel>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDomainId() {
        return domainId;
    }

    public void setDomainId(long domainId) {
        this.domainId = domainId;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public Set<Language> getLanguages() {
        if (languages == null) {
            languages = new TreeSet<Language>();
        }
        return languages;
    }

    public void setLanguages(Set<Language> languages) {
        this.languages = languages;
    }

    public RegionModel getRegion() {
        return region;
    }

    public void setRegion(RegionModel region) {
        this.region = region;
    }

    public List<DomainModel> getAssignableDomains() {
        return assignableDomains;
    }

    public void setAssignableDomains(List<DomainModel> assignableDomains) {
        this.assignableDomains = assignableDomains;
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }
}
