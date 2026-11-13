package org.example.http_client.DTOrequest;

public record UserAccountRequest(
        String login,
        String password,
        Integer roleId,
        Integer employeeId,
        Boolean isActive,
        String createdAt
) {
}
