package com.sparc.knappsack.enums;

public enum AppFileType {
    INSTALL("install"),
    ICON("icon"),
    SCREENSHOT("screenshot"),
    KEY_VAULT_ENTRY("key_vault_entry");

    private final String pathName;

    AppFileType(String s) {
        this.pathName = s;
    }

    public String getPathName() {
        return pathName;
    }
}
