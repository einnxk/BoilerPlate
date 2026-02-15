package com.thencproject.papership.bind;

/**
 * An Exception while downloading the mysql-connector or
 * hikariCP for the @ImproviseDependencies annotation
 * Makes it more easily to spot errors related to that
 * kind of problems
 */
public class DeprecatedUrlException extends RuntimeException {

    /**
     * Basic constructor
     * @param message An additional message in front of the
     *                stacktrace
     * @param cause an exception that is thrown
     */
    public DeprecatedUrlException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Basic constructor
     * @param cause an exception that is thrown
     */
    public DeprecatedUrlException(Throwable cause) {
        super(cause);
    }
}