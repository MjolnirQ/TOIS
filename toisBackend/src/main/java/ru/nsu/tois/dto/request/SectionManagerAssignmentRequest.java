package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.NotNull;

public record SectionManagerAssignmentRequest(
        @NotNull
        Integer managerId,

        @NotNull
        Integer tradePointId,

        @NotNull
        Integer sectionId
) {
}
