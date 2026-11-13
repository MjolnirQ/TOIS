package ru.nsu.tois.client.api.dto.response;

public record PermissionResponse(
        Integer id,
        String code
) {
    @Override
    public String toString() {
        return id + " | " + code;
    }
}
