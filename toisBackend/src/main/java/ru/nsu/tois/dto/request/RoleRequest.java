package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record RoleRequest(
        @NotBlank
        String name,

        Set<Integer> permissionIds
) {
}
