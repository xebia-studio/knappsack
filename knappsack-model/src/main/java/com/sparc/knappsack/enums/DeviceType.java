package com.sparc.knappsack.enums;

import java.util.ArrayList;
import java.util.List;

public enum DeviceType {
    IPAD_1("deviceType.ipad_1", true, true),
    IPAD_2("deviceType.ipad_2", true, true),
    IPAD_3("deviceType.ipad_3", true, true),
    IPAD_4("deviceType.ipad_4", true, true),
    IPAD_MINI("deviceType.ipad_mini", true, true);

    private final String messageKey;
    private final boolean iOS;
    private final boolean iPad;

    private DeviceType(String messageKey, boolean iOS, boolean iPad) {
        this.messageKey = messageKey;
        this.iOS = iOS;
        this.iPad = iPad;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public boolean isiOS() {
        return iOS;
    }

    public boolean isiPad() {
        return iPad;
    }

    public static List<DeviceType> getAlliPad() {
        List<DeviceType> deviceTypes = new ArrayList<DeviceType>();
        for (DeviceType deviceType : DeviceType.values()) {
            if (deviceType.isiPad()) {
                deviceTypes.add(deviceType);
            }
        }

        return deviceTypes;
    }
}
