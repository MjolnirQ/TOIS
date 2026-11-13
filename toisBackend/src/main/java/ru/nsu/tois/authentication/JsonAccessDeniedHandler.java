package ru.nsu.tois.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;

@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        write(
                response,
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "Not enough permissions to perform this operation.",
                request
        );
    }

    private void write(
            HttpServletResponse response,
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("""
                {"timestamp":"%s","status":%d,"error":"%s","code":"%s","message":"%s","path":"%s","method":"%s","fieldErrors":[]}
                """.formatted(
                OffsetDateTime.now(),
                status.value(),
                escape(status.getReasonPhrase()),
                escape(code),
                escape(message),
                escape(request.getRequestURI()),
                escape(request.getMethod())
        ));
    }

    private String escape(String value) {
        return value == null ? "" : value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
