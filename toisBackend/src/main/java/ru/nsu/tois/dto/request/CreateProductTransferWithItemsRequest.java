package ru.nsu.tois.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CreateProductTransferWithItemsRequest(
        @NotNull
        LocalDate transferDate,

        @NotNull
        Integer senderTradePointId,

        @NotNull
        Integer receiverTradePointId,

        @NotEmpty
        List<@Valid ProductTransferLineRequest> items
) {
}
