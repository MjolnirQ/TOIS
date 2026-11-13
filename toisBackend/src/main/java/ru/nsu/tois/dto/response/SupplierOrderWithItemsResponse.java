package ru.nsu.tois.dto.response;

import java.util.List;

public record SupplierOrderWithItemsResponse(
        SupplierOrderResponse supplierOrder,
        List<SupplierOrderItemResponse> items
) {
}
