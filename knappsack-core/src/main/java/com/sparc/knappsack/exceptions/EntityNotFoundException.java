package com.sparc.knappsack.exceptions;

public class EntityNotFoundException extends RuntimeException {

    private static final String ENTITY_NOT_FOUND_EXCEPTION = "Entity not found:";

    public EntityNotFoundException(String msg, Throwable t) {
        super(msg, t);
    }

    public EntityNotFoundException(String msg) {
        super(ENTITY_NOT_FOUND_EXCEPTION + " " + msg);
    }

    public EntityNotFoundException() {
        super(ENTITY_NOT_FOUND_EXCEPTION);
    }

}
