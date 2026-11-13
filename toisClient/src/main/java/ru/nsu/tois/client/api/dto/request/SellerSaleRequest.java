package ru.nsu.tois.client.api.dto.request;

import java.util.List;

public record SellerSaleRequest(
        String saleDate,
        Integer tradePointId,
        Integer customerId,
        List<SaleLineRequest> items
) {
}
