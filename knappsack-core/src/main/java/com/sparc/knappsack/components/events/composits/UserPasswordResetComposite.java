package com.sparc.knappsack.components.events.composits;

public class UserPasswordResetComposite implements EventComposite {
    private String password;

    public UserPasswordResetComposite(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}
