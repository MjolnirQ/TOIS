package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.NotBlank;

public record StatusUpdateRequest(
        @NotBlank
        String status
) {
}
