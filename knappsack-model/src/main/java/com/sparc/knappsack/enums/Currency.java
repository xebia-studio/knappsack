package com.sparc.knappsack.enums;

public enum Currency {
    USD("desktop.enum.currency.usd", "desktop.enum.currency.usd.symbol");

    private final String messageKey;
    private final String symbolKey;

    private Currency(String messageKey, String symbolKey) {
        this.messageKey = messageKey;
        this.symbolKey = symbolKey;
    }

    @SuppressWarnings("unused")
    public String getMessageKey() {
        return messageKey;
    }

    @SuppressWarnings("unused")
    public String getSymbolKey() {
        return symbolKey;
    }
}
