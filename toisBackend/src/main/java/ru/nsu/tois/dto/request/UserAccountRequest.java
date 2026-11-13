package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UserAccountRequest(
        @NotBlank
        String login,

        String password,

        @NotNull
        Integer roleId,

        @NotNull
        Integer employeeId,

        Boolean isActive,

        LocalDateTime createdAt
) {
}
