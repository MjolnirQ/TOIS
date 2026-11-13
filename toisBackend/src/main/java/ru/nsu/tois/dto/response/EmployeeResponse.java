package ru.nsu.tois.dto.response;

import ru.nsu.tois.entity.employees.EmployeePosition;

public record EmployeeResponse(
        Integer id,
        String fullName,
        EmployeePosition position,
        Integer salary
) {
}
