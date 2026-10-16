package com.ridelink.fare.exception;

import org.springframework.http.HttpStatus;

//Custom exception for API errors
public class ApiException extends RuntimeException {

    private final HttpStatus status;// Stores the HTTP status code related to the exception
    private final String error;//Stores the error code 

    
    public ApiException(HttpStatus status, String error, String message) {
        // Pass the error message to the parent RuntimeException class
        super(message);
        this.status = status;
        this.error = error;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }
}

