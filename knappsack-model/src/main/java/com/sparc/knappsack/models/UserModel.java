package com.sparc.knappsack.models;

public class UserModel {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String userName;
    private String password;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {

        StringBuilder builder = new StringBuilder("");
        if (firstName != null) {
            builder.append(firstName.trim());
            builder.append(" ");
        }
        if (lastName != null) {
            builder.append(lastName.trim());
        }

        return builder.toString().trim();
    }
}
