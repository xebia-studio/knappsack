package com.sparc.knappsack.enums;

public enum BillingInterval {
    MONTH("desktop.enum.billingInterval.month"),
    YEAR("desktop.enum.billingInterval.year");

    private final String messageKey;

    private BillingInterval(String messageKey) {
        this.messageKey = messageKey;
    }

    @SuppressWarnings("unused")
    public String getMessageKey() {
        return messageKey;
    }
}
