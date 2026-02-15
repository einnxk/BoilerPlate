package com.thencproject.papership.bind;

/**
 * An Exception while initializing the paper logger by class annotated
 * with the @EnableDebug or @EnableVerboseDebug annotation
 * Makes it more easily to spot errors related to that
 * kind of problems
 */
public class ParentLoggerInitializeException extends RuntimeException {

    /**
     * @param message message without an exception
     */
    public ParentLoggerInitializeException(String message) {
        super(message);
    }

    /**
     * @param cause exception without a message
     */
    public ParentLoggerInitializeException(Throwable cause) {
        super(cause);
    }

    /**
     * Just this exception with no throwable
     */
    public ParentLoggerInitializeException() {}
}