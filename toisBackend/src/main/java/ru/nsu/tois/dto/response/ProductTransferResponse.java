package ru.nsu.tois.dto.response;

import java.time.LocalDate;

public record ProductTransferResponse(
        Integer id,
        LocalDate transferDate,
        Integer senderTradePointId,
        Integer receiverTradePointId
) {
}
