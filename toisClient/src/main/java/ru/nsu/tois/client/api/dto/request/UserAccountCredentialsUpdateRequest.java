package ru.nsu.tois.client.api.dto.request;

public record UserAccountCredentialsUpdateRequest(
        String login,
        String password
) {
}
