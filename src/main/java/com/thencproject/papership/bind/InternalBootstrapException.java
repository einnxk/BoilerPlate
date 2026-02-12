package com.thencproject.papership.bind;

public class InternalBootstrapException extends RuntimeException {

    public InternalBootstrapException(String message) {
        super(message);
    }

    public InternalBootstrapException() {
        super();
    }

    public InternalBootstrapException(String message, Throwable cause) {
        super(message, cause);
    }
}