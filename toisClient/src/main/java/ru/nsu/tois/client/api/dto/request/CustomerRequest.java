package ru.nsu.tois.client.api.dto.request;

public record CustomerRequest(
        String fullName,
        String phoneNumber
) {
}
