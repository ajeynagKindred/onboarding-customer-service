package org.example.customerservice.exceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// This annotation marks the exception with a specific HTTP status code.
@ResponseStatus(HttpStatus.BAD_REQUEST) // Marks the response with a 400 Bad Request status
public class CustomerAlreadyExistsException extends RuntimeException {

    // Default constructor with a standard error message
    public CustomerAlreadyExistsException() {
        super("Customer already exists.");
    }

    // Constructor that allows a custom error message
    public CustomerAlreadyExistsException(String message) {
        super(message);
    }

    // Constructor that allows a custom message and a cause (another throwable)
    public CustomerAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

