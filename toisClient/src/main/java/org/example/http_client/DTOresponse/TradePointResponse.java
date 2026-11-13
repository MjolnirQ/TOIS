package org.example.http_client.DTOresponse;

import java.math.BigDecimal;

public record TradePointResponse(
        Integer id,
        String name,
        String address,
        String type,
        BigDecimal tradeArea,
        BigDecimal utilityPayments,
        Integer counterCount,
        BigDecimal rentPayment
) {
    @Override
    public String toString() {
        return name + " | " + address;
    }
}
