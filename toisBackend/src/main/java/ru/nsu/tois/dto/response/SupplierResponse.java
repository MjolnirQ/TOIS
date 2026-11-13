package ru.nsu.tois.dto.response;

public record SupplierResponse(
        Integer id,
        String organizationName,
        Boolean isActive
) {
}
