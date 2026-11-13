package org.example.http_client.DTOrequest;

public record UserAccountCredentialsUpdateRequest(
        String login,
        String password
) {
}
