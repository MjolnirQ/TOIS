package org.example.http_client.DTOrequest;

public record LoginRequest(
        String login,
        String password
) {}
