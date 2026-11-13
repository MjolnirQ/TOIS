package org.example.http_client.DTOresponse;

public record CustomerResponse(
        Integer id,
        String fullName,
        String phoneNumber
) {
}
