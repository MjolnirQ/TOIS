package org.example.http_client.DTOrequest;

public record CustomerRequest(
        String fullName,
        String phoneNumber
) {
}
