package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SupplyRequestLineRequest(
        Integer productId,

        String productName,

        @Pattern(regexp = "kilograms|grams|liters|milliliters")
        String units,

        @NotNull
        @Min(1)
        Integer requestedQuantity
) {
}
