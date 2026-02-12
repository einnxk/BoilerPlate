package com.thencproject.papership.loader;

public class DeprecatedUrlException extends RuntimeException {

    public DeprecatedUrlException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeprecatedUrlException(Throwable cause) {
        super(cause);
    }
}