package ru.nsu.tois.dto.response;

public record SectionManagerAssignmentResponse(
        Integer id,
        Integer managerId,
        Integer tradePointId,
        Integer sectionId
) {
}
