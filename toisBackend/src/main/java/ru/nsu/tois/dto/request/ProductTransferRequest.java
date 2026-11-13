package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ProductTransferRequest(
        @NotNull
        LocalDate transferDate,

        @NotNull
        Integer senderTradePointId,

        @NotNull
        Integer receiverTradePointId
) {
}
