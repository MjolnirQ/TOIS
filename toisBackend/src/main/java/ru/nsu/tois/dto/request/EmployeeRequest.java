package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.nsu.tois.entity.employees.EmployeePosition;

public record EmployeeRequest(
        @NotBlank
        String fullName,

        @NotNull
        EmployeePosition position,

        @NotNull
        @Min(0)
        Integer salary
) {
}
