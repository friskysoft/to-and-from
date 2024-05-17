package com.friskysoft.tools.taf.models;

public class ToAndFromException extends RuntimeException {

    public ToAndFromException(final String message) {
        super(message);
    }

    public ToAndFromException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
