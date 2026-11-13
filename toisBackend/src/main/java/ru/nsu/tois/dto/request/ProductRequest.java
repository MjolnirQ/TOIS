package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ProductRequest(
        @NotBlank
        String name,

        @Pattern(regexp = "kilograms|grams|liters|milliliters")
        String units
) {
}
