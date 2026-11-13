package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PermissionRequest(
        @NotBlank
        String code
) {
}
