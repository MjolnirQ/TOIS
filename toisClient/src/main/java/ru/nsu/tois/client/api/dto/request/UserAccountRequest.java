package ru.nsu.tois.client.api.dto.request;

public record UserAccountRequest(
        String login,
        String password,
        Integer roleId,
        Integer employeeId,
        Boolean isActive,
        String createdAt
) {
}
