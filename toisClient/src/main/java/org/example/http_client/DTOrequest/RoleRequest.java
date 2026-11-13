package org.example.http_client.DTOrequest;

import java.util.Set;

public record RoleRequest(
        String name,
        Set<Integer> permissionIds
) {
}
