package com.sparc.knappsack.exceptions;

public class ImageException extends Exception {
    private static final String IMAGE_EXCEPTION = "ImageException creating thumbnail:";

    public ImageException(String msg, Throwable t) {
        super(msg, t);
    }

    public ImageException(String msg) {
        super(IMAGE_EXCEPTION + " " + msg);
    }

    public ImageException() {
        super(IMAGE_EXCEPTION);
    }
}
