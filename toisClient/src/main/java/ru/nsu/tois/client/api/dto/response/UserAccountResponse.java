package ru.nsu.tois.client.api.dto.response;

public record UserAccountResponse(
        Integer id,
        String login,
        Integer roleId,
        Integer employeeId,
        Boolean isActive,
        String createdAt
) {
}
