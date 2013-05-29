package com.sparc.knappsack.exceptions;

import com.sparc.knappsack.enums.ResignErrorType;

public class ApplicationVersionResignException extends Exception {

    private static final String APPLICATION_RESIGN_EXCEPTION = "Error resigning application version:";
    private ResignErrorType resignErrorType;

    public ApplicationVersionResignException(String msg, Throwable t, ResignErrorType resignErrorType) {
        super(msg, t);
        this.resignErrorType = resignErrorType;
    }

    public ApplicationVersionResignException(String msg, ResignErrorType resignErrorType) {
        super(APPLICATION_RESIGN_EXCEPTION + " " + msg);
        this.resignErrorType = resignErrorType;
    }

    public ApplicationVersionResignException(ResignErrorType resignErrorType) {
        super(APPLICATION_RESIGN_EXCEPTION);
        this.resignErrorType = resignErrorType;
    }

    public ResignErrorType getResignErrorType() {
        return this.resignErrorType;
    }
}
