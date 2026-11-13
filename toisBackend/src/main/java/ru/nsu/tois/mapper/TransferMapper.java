package ru.nsu.tois.mapper;

import org.springframework.stereotype.Component;
import ru.nsu.tois.dto.request.ProductTransferItemRequest;
import ru.nsu.tois.dto.request.ProductTransferRequest;
import ru.nsu.tois.dto.response.ProductTransferItemResponse;
import ru.nsu.tois.dto.response.ProductTransferResponse;
import ru.nsu.tois.entity.catalog.Product;
import ru.nsu.tois.entity.tradePoints.TradePoint;
import ru.nsu.tois.entity.transfers.ProductTransfer;
import ru.nsu.tois.entity.transfers.ProductTransferItem;

@Component
public class TransferMapper {
    public ProductTransferResponse toDto(ProductTransfer productTransfer) {
        return new ProductTransferResponse(
                productTransfer.getId(),
                productTransfer.getTransferDate(),
                productTransfer.getSenderTradePoint() == null ? null : productTransfer.getSenderTradePoint().getId(),
                productTransfer.getReceiverTradePoint() == null ? null : productTransfer.getReceiverTradePoint().getId()
        );
    }

    public ProductTransfer toEntity(ProductTransferRequest request) {
        ProductTransfer productTransfer = new ProductTransfer();
        productTransfer.setTransferDate(request.transferDate());
        productTransfer.setSenderTradePoint(tradePointReference(request.senderTradePointId()));
        productTransfer.setReceiverTradePoint(tradePointReference(request.receiverTradePointId()));
        return productTransfer;
    }

    public ProductTransferItemResponse toDto(ProductTransferItem item) {
        return new ProductTransferItemResponse(
                item.getId(),
                item.getProductTransfer() == null ? null : item.getProductTransfer().getId(),
                item.getProduct() == null ? null : item.getProduct().getId(),
                item.getQuantity()
        );
    }

    public ProductTransferItem toEntity(ProductTransferItemRequest request) {
        ProductTransferItem item = new ProductTransferItem();
        item.setProductTransfer(productTransferReference(request.productTransferId()));
        item.setProduct(productReference(request.productId()));
        item.setQuantity(request.quantity());
        return item;
    }

    private ProductTransfer productTransferReference(Integer id) {
        ProductTransfer productTransfer = new ProductTransfer();
        productTransfer.setId(id);
        return productTransfer;
    }

    private Product productReference(Integer id) {
        Product product = new Product();
        product.setId(id);
        return product;
    }

    private TradePoint tradePointReference(Integer id) {
        TradePoint tradePoint = new TradePoint();
        tradePoint.setId(id);
        return tradePoint;
    }
}
