package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SupplierRequest(
        @NotBlank
        String organizationName,

        Boolean isActive
) {
}
