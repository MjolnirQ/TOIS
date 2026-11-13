package ru.nsu.tois.dto.request;

public record UserAccountCredentialsUpdateRequest(
        String login,
        String password
) {
}
