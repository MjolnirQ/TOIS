package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record DeliveryLineRequest(
        Integer productId,

        String productName,

        @Pattern(regexp = "kilograms|grams|liters|milliliters")
        String units,

        @NotNull
        @Min(1)
        Integer quantity,

        @NotNull
        @DecimalMin("0")
        BigDecimal deliveryPrice
) {
}
