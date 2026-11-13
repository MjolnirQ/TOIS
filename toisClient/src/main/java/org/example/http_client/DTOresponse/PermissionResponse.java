package org.example.http_client.DTOresponse;

public record PermissionResponse(
        Integer id,
        String code
) {
    @Override
    public String toString() {
        return id + " | " + code;
    }
}
