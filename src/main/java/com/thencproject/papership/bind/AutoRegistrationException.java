package com.thencproject.papership.bind;

/**
 * An Exception while registering commands or listeners
 * that are autowired
 * Makes it more easily to spot errors related to that
 * kind of problems
 */
public class AutoRegistrationException extends RuntimeException {

    /**
     * Basic constructor
     * @param message a message with the exception type
     */
    public AutoRegistrationException(String message) {
        super(message);
    }
    /**
     * Basic constructor
     * @param message a message with the exception type
     * @param cause another throwable exception
     */
    public AutoRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}