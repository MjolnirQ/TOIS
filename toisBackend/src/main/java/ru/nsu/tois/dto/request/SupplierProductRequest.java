package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record SupplierProductRequest(
        @NotNull
        Integer supplierId,

        Integer productId,

        String productName,

        @Pattern(regexp = "kilograms|grams|liters|milliliters")
        String units,

        @DecimalMin("0")
        BigDecimal supplierPrice
) {
}
