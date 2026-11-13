package ru.nsu.tois.client.api.dto.response;

public record CustomerResponse(
        Integer id,
        String fullName,
        String phoneNumber
) {
}
