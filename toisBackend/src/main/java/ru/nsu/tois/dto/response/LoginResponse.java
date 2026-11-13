package ru.nsu.tois.dto.response;

public record LoginResponse(
        String token,
        String role
) {}
