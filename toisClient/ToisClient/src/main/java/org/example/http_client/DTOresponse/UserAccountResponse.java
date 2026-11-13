package org.example.http_client.DTOresponse;

public record UserAccountResponse(
        Integer id,
        String login,
        Integer roleId,
        Integer employeeId,
        Boolean isActive,
        String createdAt
) {
}
