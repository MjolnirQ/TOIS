package ru.nsu.tois.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        String method,
        List<FieldErrorResponse> fieldErrors
) {
    public ApiErrorResponse {
        fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
    }

    public static ApiErrorResponse of(
            int status,
            String error,
            String code,
            String message,
            String path,
            String method
    ) {
        return new ApiErrorResponse(
                OffsetDateTime.now(),
                status,
                error,
                code,
                message,
                path,
                method,
                List.of()
        );
    }

    public static ApiErrorResponse withFieldErrors(
            int status,
            String error,
            String code,
            String message,
            String path,
            String method,
            List<FieldErrorResponse> fieldErrors
    ) {
        return new ApiErrorResponse(
                OffsetDateTime.now(),
                status,
                error,
                code,
                message,
                path,
                method,
                fieldErrors
        );
    }

    public record FieldErrorResponse(
            String field,
            String message
    ) {
    }
}
