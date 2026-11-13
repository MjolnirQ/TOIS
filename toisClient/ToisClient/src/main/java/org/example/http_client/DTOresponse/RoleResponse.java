package org.example.http_client.DTOresponse;

import java.util.Set;

public record RoleResponse(
        Integer id,
        String name,
        Set<Integer> permissionIds
) {
    @Override
    public String toString() {
        return id + " | " + name;
    }
}
