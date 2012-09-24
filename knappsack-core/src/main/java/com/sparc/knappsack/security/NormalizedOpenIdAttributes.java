package com.sparc.knappsack.security;

import java.io.Serializable;

public class NormalizedOpenIdAttributes implements Serializable {

    private static final long serialVersionUID = 1845787140422499576L;

    private String userLocalIdentifier;
    private String emailAddress;
    private String firstName;
    private String lastName;
    private String fullName;
    private String loginReplacement;

    public NormalizedOpenIdAttributes(String userLocalIdentifier, String emailAddress, String firstName, String lastname, String fullName, String loginReplacement) {
        this.userLocalIdentifier = userLocalIdentifier;
        this.emailAddress = emailAddress;
        this.firstName = firstName;
        this.lastName = lastname;
        this.fullName = fullName;
        this.loginReplacement = loginReplacement;
    }

    @SuppressWarnings("unused")
    public String getUserLocalIdentifier() {
        return userLocalIdentifier;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @SuppressWarnings("unused")
    public String getFullName() {
        return fullName;
    }

    @SuppressWarnings("unused")
    public String getLoginReplacement() {
        return loginReplacement;
    }

    @SuppressWarnings("unused")
    public void setUserLocalIdentifier(String userLocalIdentifier) {
        this.userLocalIdentifier = userLocalIdentifier;
    }

    @SuppressWarnings("unused")
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @SuppressWarnings("unused")
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @SuppressWarnings("unused")
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @SuppressWarnings("unused")
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @SuppressWarnings("unused")
    public void setLoginReplacement(String loginReplacement) {
        this.loginReplacement = loginReplacement;
    }
}
