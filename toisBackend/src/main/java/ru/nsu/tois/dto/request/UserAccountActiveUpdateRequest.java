package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.NotNull;

public record UserAccountActiveUpdateRequest(
@NotNull Boolean isActive
) {}
