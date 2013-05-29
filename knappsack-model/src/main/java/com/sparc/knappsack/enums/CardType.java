package com.sparc.knappsack.enums;

import java.util.HashMap;
import java.util.Map;

public enum CardType {

    VISA("Visa"),
    AMERICAN_EXPRESS("American Express"),
    MASTERCARD("MasterCard"),
    DISCOVER("Discover"),
    JCB("JCB"),
    DINERS_CLUB("Diners Club"),
    UNKNOWN("Unknown");

    private final String value;

    private CardType(String value) {
        this.value = value;
    }

    @SuppressWarnings("unused")
    public String getValue() {
        return value;
    }

    private static final Map<String, CardType> stringToEnum =
            new HashMap<String, CardType>();

    static {
        for (CardType r : values()) {
            stringToEnum.put(r.toString(), r);
        }
    }

    public static CardType getByValue(String value){
        return stringToEnum.get(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
