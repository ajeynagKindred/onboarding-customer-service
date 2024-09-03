package org.example.customerservice.exceptionHandler;



public class DBException extends RuntimeException {

    // Default constructor with a standard error message
    public DBException() {
        super("DB Exception");
    }

    // Constructor that allows a custom error message
    public DBException(String message) {
        super(message);
    }

    // Constructor that allows a custom message and a cause (another throwable)
    public DBException(String message, Throwable cause) {
        super(message, cause);
    }
}