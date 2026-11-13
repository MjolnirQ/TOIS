package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.NotNull;

public record SectionRequest(
        @NotNull
        Integer departmentStoreId
) {
}
