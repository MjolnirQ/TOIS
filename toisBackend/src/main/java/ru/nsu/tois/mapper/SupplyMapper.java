package ru.nsu.tois.mapper;

import org.springframework.stereotype.Component;
import ru.nsu.tois.dto.request.*;
import ru.nsu.tois.dto.response.*;
import ru.nsu.tois.entity.catalog.Product;
import ru.nsu.tois.entity.employees.TradeManager;
import ru.nsu.tois.entity.suppliers.Supplier;
import ru.nsu.tois.entity.supply.*;
import ru.nsu.tois.entity.tradePoints.TradePoint;

@Component
public class SupplyMapper {
    public SupplyRequestResponse toDto(SupplyRequest supplyRequest) {
        return new SupplyRequestResponse(
                supplyRequest.getId(),
                supplyRequest.getRequestDate(),
                supplyRequest.getTradePoint() == null ? null : supplyRequest.getTradePoint().getId(),
                supplyRequest.getStatus()
        );
    }

    public SupplyRequest toEntity(SupplyRequestRequest request) {
        SupplyRequest supplyRequest = new SupplyRequest();
        supplyRequest.setRequestDate(request.requestDate());
        supplyRequest.setTradePoint(tradePointReference(request.tradePointId()));
        supplyRequest.setStatus(request.status());
        return supplyRequest;
    }

    public SupplyRequestItemResponse toDto(SupplyRequestItem item) {
        return new SupplyRequestItemResponse(
                item.getId(),
                item.getSupplyRequest() == null ? null : item.getSupplyRequest().getId(),
                item.getProduct() == null ? null : item.getProduct().getId(),
                item.getRequestedQuantity()
        );
    }

    public SupplyRequestItem toEntity(SupplyRequestItemRequest request) {
        SupplyRequestItem item = new SupplyRequestItem();
        item.setSupplyRequest(supplyRequestReference(request.supplyRequestId()));
        item.setProduct(productReference(request.productId()));
        item.setRequestedQuantity(request.requestedQuantity());
        return item;
    }

    public SupplierOrderResponse toDto(SupplierOrder supplierOrder) {
        return new SupplierOrderResponse(
                supplierOrder.getId(),
                supplierOrder.getOrderDate(),
                supplierOrder.getOrderNumber(),
                supplierOrder.getManager() == null ? null : supplierOrder.getManager().getId(),
                supplierOrder.getSupplier() == null ? null : supplierOrder.getSupplier().getId(),
                supplierOrder.getSupplyRequest() == null ? null : supplierOrder.getSupplyRequest().getId(),
                supplierOrder.getStatus()
        );
    }

    public SupplierOrder toEntity(SupplierOrderRequest request) {
        SupplierOrder supplierOrder = new SupplierOrder();
        supplierOrder.setOrderDate(request.orderDate());
        supplierOrder.setOrderNumber(request.orderNumber());
        supplierOrder.setManager(tradeManagerReference(request.managerId()));
        supplierOrder.setSupplier(supplierReference(request.supplierId()));
        supplierOrder.setSupplyRequest(supplyRequestReference(request.supplyRequestId()));
        supplierOrder.setStatus(request.status());
        return supplierOrder;
    }

    public SupplierOrderItemResponse toDto(SupplierOrderItem item) {
        return new SupplierOrderItemResponse(
                item.getId(),
                item.getSupplierOrder() == null ? null : item.getSupplierOrder().getId(),
                item.getProduct() == null ? null : item.getProduct().getId(),
                item.getQuantity(),
                item.getPurchasePrice()
        );
    }

    public SupplierOrderItem toEntity(SupplierOrderItemRequest request) {
        SupplierOrderItem item = new SupplierOrderItem();
        item.setSupplierOrder(supplierOrderReference(request.supplierOrderId()));
        item.setProduct(productReference(request.productId()));
        item.setQuantity(request.quantity());
        item.setPurchasePrice(request.purchasePrice());
        return item;
    }

    public DeliveryResponse toDto(Delivery delivery) {
        return new DeliveryResponse(
                delivery.getId(),
                delivery.getSupplier() == null ? null : delivery.getSupplier().getId(),
                delivery.getSupplierOrder() == null ? null : delivery.getSupplierOrder().getId(),
                delivery.getDeliveryDate()
        );
    }

    public Delivery toEntity(DeliveryRequest request) {
        Delivery delivery = new Delivery();
        delivery.setSupplier(supplierReference(request.supplierId()));
        delivery.setSupplierOrder(supplierOrderReference(request.supplierOrderId()));
        delivery.setDeliveryDate(request.deliveryDate());
        return delivery;
    }

    public DeliveryItemResponse toDto(DeliveryItem item) {
        return new DeliveryItemResponse(
                item.getId(),
                item.getDelivery() == null ? null : item.getDelivery().getId(),
                item.getProduct() == null ? null : item.getProduct().getId(),
                item.getQuantity(),
                item.getDeliveryPrice()
        );
    }

    public DeliveryItem toEntity(DeliveryItemRequest request) {
        DeliveryItem item = new DeliveryItem();
        item.setDelivery(deliveryReference(request.deliveryId()));
        item.setProduct(productReference(request.productId()));
        item.setQuantity(request.quantity());
        item.setDeliveryPrice(request.deliveryPrice());
        return item;
    }

    private SupplyRequest supplyRequestReference(Integer id) {
        SupplyRequest supplyRequest = new SupplyRequest();
        supplyRequest.setId(id);
        return supplyRequest;
    }

    private SupplierOrder supplierOrderReference(Integer id) {
        SupplierOrder supplierOrder = new SupplierOrder();
        supplierOrder.setId(id);
        return supplierOrder;
    }

    private Delivery deliveryReference(Integer id) {
        Delivery delivery = new Delivery();
        delivery.setId(id);
        return delivery;
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

    private TradeManager tradeManagerReference(Integer id) {
        TradeManager tradeManager = new TradeManager();
        tradeManager.setId(id);
        return tradeManager;
    }

    private Supplier supplierReference(Integer id) {
        Supplier supplier = new Supplier();
        supplier.setId(id);
        return supplier;
    }
}
