package ru.nsu.tois.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.nsu.tois.dto.response.ApiErrorResponse;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String POSTGRES_UNIQUE_VIOLATION = "23505";
    private static final String POSTGRES_FOREIGN_KEY_VIOLATION = "23503";
    private static final String POSTGRES_NOT_NULL_VIOLATION = "23502";
    private static final String POSTGRES_CHECK_VIOLATION = "23514";

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResource(HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", "Endpoint not found", request);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFound(
            EntityNotFoundException exception,
            HttpServletRequest request
    ) {
        return error(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                "Requested record not found: " + safeMessage(exception),
                request
        );
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class,
            MethodArgumentTypeMismatchException.class,
            ConversionFailedException.class,
            MissingServletRequestParameterException.class,
            MissingPathVariableException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
        return error(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                badRequestMessage(exception),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        return validationError(bindingErrors(exception.getBindingResult().getFieldErrors(), exception.getBindingResult().getGlobalErrors()), request);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBindException(BindException exception, HttpServletRequest request) {
        return validationError(bindingErrors(exception.getBindingResult().getFieldErrors(), exception.getBindingResult().getGlobalErrors()), request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<ApiErrorResponse.FieldErrorResponse> fieldErrors = exception.getConstraintViolations().stream()
                .map(violation -> new ApiErrorResponse.FieldErrorResponse(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ))
                .toList();
        return validationError(fieldErrors, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        SQLException sqlException = findCause(exception, SQLException.class);
        if (sqlException == null) {
            return error(
                    HttpStatus.CONFLICT,
                    "DATA_INTEGRITY_VIOLATION",
                    "Data integrity constraint violated.",
                    request
            );
        }

        return switch (sqlException.getSQLState()) {
            case POSTGRES_UNIQUE_VIOLATION -> error(
                    HttpStatus.CONFLICT,
                    "UNIQUE_CONSTRAINT_VIOLATION",
                    "A record with these values already exists.",
                    request
            );
            case POSTGRES_FOREIGN_KEY_VIOLATION -> error(
                    HttpStatus.CONFLICT,
                    "FOREIGN_KEY_CONSTRAINT_VIOLATION",
                    "A referenced record is missing or the record is still in use.",
                    request
            );
            case POSTGRES_NOT_NULL_VIOLATION -> error(
                    HttpStatus.BAD_REQUEST,
                    "REQUIRED_FIELD_MISSING",
                    "A required field is missing.",
                    request
            );
            case POSTGRES_CHECK_VIOLATION -> error(
                    HttpStatus.BAD_REQUEST,
                    "CHECK_CONSTRAINT_VIOLATION",
                    "A database check constraint was violated.",
                    request
            );
            default -> error(
                    HttpStatus.CONFLICT,
                    "DATA_INTEGRITY_VIOLATION",
                    "Data integrity constraint violated.",
                    request
            );
        };
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleDataAccess(DataAccessException exception, HttpServletRequest request) {
        log.error("Database error while processing {} {}", request.getMethod(), request.getRequestURI(), exception);
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "DATABASE_ERROR",
                "Database operation failed.",
                request
        );
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ApiErrorResponse> handleTransactionSystem(
            TransactionSystemException exception,
            HttpServletRequest request
    ) {
        ConstraintViolationException constraintViolation = findCause(exception, ConstraintViolationException.class);
        if (constraintViolation != null) {
            return handleConstraintViolation(constraintViolation, request);
        }
        log.error("Transaction error while processing {} {}", request.getMethod(), request.getRequestURI(), exception);
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "TRANSACTION_ERROR",
                "Transaction failed.",
                request
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(
            AuthenticationException exception,
            HttpServletRequest request
    ) {
        String message = exception instanceof BadCredentialsException
                ? "Invalid login or password."
                : "Authentication failed.";
        return error(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", message, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return error(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "Access denied.",
                request
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        return error(
                HttpStatus.METHOD_NOT_ALLOWED,
                "METHOD_NOT_ALLOWED",
                "HTTP method not supported for this endpoint.",
                request
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request
    ) {
        return error(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "UNSUPPORTED_MEDIA_TYPE",
                "Unsupported request content type.",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unexpected error while processing {} {}", request.getMethod(), request.getRequestURI(), exception);
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Internal server error.",
                request
        );
    }

    private ResponseEntity<ApiErrorResponse> validationError(
            List<ApiErrorResponse.FieldErrorResponse> fieldErrors,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = ApiErrorResponse.withFieldErrors(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "VALIDATION_FAILED",
                "Request validation failed.",
                request.getRequestURI(),
                request.getMethod(),
                fieldErrors
        );
        return ResponseEntity.badRequest().body(response);
    }

    private ResponseEntity<ApiErrorResponse> error(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status).body(ApiErrorResponse.of(
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                request.getRequestURI(),
                request.getMethod()
        ));
    }

    private List<ApiErrorResponse.FieldErrorResponse> bindingErrors(
            List<FieldError> fieldErrors,
            List<ObjectError> globalErrors
    ) {
        List<ApiErrorResponse.FieldErrorResponse> result = new ArrayList<>();
        fieldErrors.forEach(error -> result.add(new ApiErrorResponse.FieldErrorResponse(
                error.getField(),
                error.getDefaultMessage()
        )));
        globalErrors.forEach(error -> result.add(new ApiErrorResponse.FieldErrorResponse(
                error.getObjectName(),
                error.getDefaultMessage()
        )));
        return result;
    }

    private String badRequestMessage(Exception exception) {
        if (exception instanceof MethodArgumentTypeMismatchException mismatch) {
            return "Invalid value for parameter " + mismatch.getName() + ".";
        }
        if (exception instanceof MissingServletRequestParameterException missingParameter) {
            return "Required query parameter is missing: " + missingParameter.getParameterName() + ".";
        }
        if (exception instanceof MissingPathVariableException missingPathVariable) {
            return "Required path variable is missing: " + missingPathVariable.getVariableName() + ".";
        }
        if (exception instanceof HttpMessageNotReadableException) {
            return "Request body is missing or contains invalid JSON.";
        }
        return safeMessage(exception);
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? "Invalid request." : message;
    }

    private <T extends Throwable> T findCause(Throwable throwable, Class<T> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }
}
