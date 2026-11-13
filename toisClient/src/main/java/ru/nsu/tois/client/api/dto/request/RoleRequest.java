package ru.nsu.tois.client.api.dto.request;

import java.util.Set;

public record RoleRequest(
        String name,
        Set<Integer> permissionIds
) {
}
