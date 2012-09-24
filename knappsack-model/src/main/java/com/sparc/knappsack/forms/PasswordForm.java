package com.sparc.knappsack.forms;

public class PasswordForm {

    private String originalPassword;
    private String firstNewPassword;
    private String secondNewPassword;

    public String getOriginalPassword() {
        return originalPassword;
    }

    public void setOriginalPassword(String originalPassword) {
        this.originalPassword = originalPassword;
    }

    public String getFirstNewPassword() {
        return firstNewPassword;
    }

    public void setFirstNewPassword(String firstNewPassword) {
        this.firstNewPassword = firstNewPassword;
    }

    public String getSecondNewPassword() {
        return secondNewPassword;
    }

    public void setSecondNewPassword(String secondNewPassword) {
        this.secondNewPassword = secondNewPassword;
    }
}
