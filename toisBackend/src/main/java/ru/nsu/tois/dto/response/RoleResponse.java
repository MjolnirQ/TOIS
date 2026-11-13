package ru.nsu.tois.dto.response;

import java.util.Set;

public record RoleResponse(
        Integer id,
        String name,
        Set<Integer> permissionIds
) {
}
