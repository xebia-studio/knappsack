package com.sparc.knappsack.components.events.composits;

public class OrganizationRegistrationComposite implements EventComposite {

    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String userName;

    public OrganizationRegistrationComposite(Long userId, String firstName, String lastName, String email, String userName) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.userName = userName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
