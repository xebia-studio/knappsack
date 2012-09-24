package com.sparc.knappsack.util;

import com.sparc.knappsack.enums.ApplicationType;

public class UserAgentInfo extends UAgentInfo {

    public static final String deviceChrome = "chrome";
    public static final String deviceFirefox = "firefox";

    public UserAgentInfo() {
        super("", "");
    }

    public UserAgentInfo(String userAgent, String httpAccept) {
        super(userAgent, httpAccept);
    }

    public final boolean isChrome() {
        return getUserAgent().contains(deviceChrome);
    }

    public final boolean isFirefox() {
        return getUserAgent().contains(deviceFirefox);
    }

    public final ApplicationType getApplicationType() {
        if (detectIphoneOrIpod()) {
            return ApplicationType.IPHONE;
        } else if (detectIpad()) {
            return ApplicationType.IPAD;
        } else if (detectAndroid()) {
            return ApplicationType.ANDROID;
        } else if (detectWindowsPhone7()) {
            return ApplicationType.WINDOWSPHONE7;
        } else if (detectBlackBerry()) {
            return ApplicationType.BLACKBERRY;
        } else if (isChrome()) {
            return ApplicationType.CHROME;
        } else if (isFirefox()) {
            return ApplicationType.FIREFOX;
        }

        return ApplicationType.OTHER;
    }

    @Override
    public boolean detectMobileLong() {
        boolean isMobile = super.detectMobileLong();

        if (isMobile && (detectWindowsMobile() || detectWindowsPhone7())) {
            isMobile = false;
        }

        return isMobile;
    }
}
