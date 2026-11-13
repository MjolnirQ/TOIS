package org.example.exception;

import org.example.http_client.DTOresponse.ErrorResponse;

public class ApiException extends RuntimeException {
    private final int statusCode;
    private final ErrorResponse errorResponse;

    public ApiException(int statusCode, ErrorResponse errorResponse) {
        super(errorResponse.toString());
        this.statusCode = statusCode;
        this.errorResponse = errorResponse;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
