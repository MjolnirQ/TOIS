package ru.nsu.tois.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record SellerSaleRequest(
        @NotNull
        LocalDate saleDate,

        @NotNull
        Integer tradePointId,

        Integer customerId,

        @NotEmpty
        List<@Valid SaleLineRequest> items
) {
}
