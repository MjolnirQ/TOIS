package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
        @NotBlank
        String fullName,

        String phoneNumber
) {
}
