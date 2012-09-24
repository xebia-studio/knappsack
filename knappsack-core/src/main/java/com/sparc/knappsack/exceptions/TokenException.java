package com.sparc.knappsack.exceptions;

public class TokenException extends Exception {
    private static final String TOKEN_EXCEPTION = "Token Exception:";

    public TokenException(String msg, Throwable t) {
        super(msg, t);
    }

    public TokenException(String msg) {
        super(TOKEN_EXCEPTION + " " + msg);
    }

    public TokenException() {
        super(TOKEN_EXCEPTION);
    }
}
