package ru.nsu.tois.client.api.dto.request;

public record LoginRequest(
        String login,
        String password
) {}
