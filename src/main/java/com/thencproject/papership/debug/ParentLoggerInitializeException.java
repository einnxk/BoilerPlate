package com.thencproject.papership.debug;

public class ParentLoggerInitializeException extends RuntimeException {

    public ParentLoggerInitializeException(String message) {
        super(message);
    }

    public ParentLoggerInitializeException(Throwable cause) {
        super(cause);
    }

    public ParentLoggerInitializeException() {}
}