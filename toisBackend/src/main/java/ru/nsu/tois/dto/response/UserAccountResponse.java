package ru.nsu.tois.dto.response;

import java.time.LocalDateTime;

public record UserAccountResponse(
        Integer id,
        String login,
        Integer roleId,
        Integer employeeId,
        Boolean isActive,
        LocalDateTime createdAt
) {
}
