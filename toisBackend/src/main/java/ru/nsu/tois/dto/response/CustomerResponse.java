package ru.nsu.tois.dto.response;

public record CustomerResponse(
        Integer id,
        String fullName,
        String phoneNumber
) {
}
