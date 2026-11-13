package ru.nsu.tois.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.nsu.tois.authentication.PermissionCodes;
import ru.nsu.tois.dto.request.*;
import ru.nsu.tois.dto.response.*;
import ru.nsu.tois.service.SupplyService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager")
public class ManagerSupplyController {
    private final SupplyService supplyService;

    @GetMapping("/supply-requests")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLY_REQUESTS_READ + "')")
    public PagedResponse<SupplyRequestResponse> findAllSupplyRequests(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return supplyService.findAllSupplyRequests(page, size);
    }

    @GetMapping("/supply-requests/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLY_REQUESTS_READ + "')")
    public SupplyRequestResponse findSupplyRequestById(@PathVariable Integer id) {
        return supplyService.findSupplyRequestById(id);
    }

    @PostMapping("/supply-requests")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLY_REQUESTS_WRITE + "')")
    public SupplyRequestWithItemsResponse createSupplyRequest(
            @Valid @RequestBody CreateSupplyRequestWithItemsRequest request
    ) {
        return supplyService.createSupplyRequestWithItems(request);
    }

    @PutMapping("/supply-requests/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLY_REQUESTS_WRITE + "')")
    public SupplyRequestResponse updateSupplyRequest(
            @PathVariable Integer id,
            @Valid @RequestBody SupplyRequestRequest request
    ) {
        return supplyService.updateSupplyRequest(id, request);
    }

    @DeleteMapping("/supply-requests/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLY_REQUESTS_WRITE + "')")
    public ResponseEntity<Void> deleteSupplyRequest(@PathVariable Integer id) {
        supplyService.deleteSupplyRequest(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/supply-requests/{id}/status")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLY_REQUESTS_WRITE + "')")
    public SupplyRequestResponse updateSupplyRequestStatus(
            @PathVariable Integer id,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        return supplyService.updateSupplyRequestStatus(id, request);
    }

    @PostMapping("/supply-requests/{supplyRequestId}/supplier-orders")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLIER_ORDERS_WRITE + "')")
    public SupplierOrderWithItemsResponse createSupplierOrder(
            @PathVariable Integer supplyRequestId,
            @Valid @RequestBody CreateSupplierOrderWithItemsRequest request
    ) {
        return supplyService.createCurrentManagerSupplierOrder(supplyRequestId, request);
    }

    @GetMapping("/supplier-orders")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLIER_ORDERS_READ + "')")
    public PagedResponse<SupplierOrderResponse> findAllSupplierOrders(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return supplyService.findAllSupplierOrders(page, size);
    }

    @GetMapping("/supplier-orders/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLIER_ORDERS_READ + "')")
    public SupplierOrderResponse findSupplierOrderById(@PathVariable Integer id) {
        return supplyService.findSupplierOrderById(id);
    }

    @PutMapping("/supplier-orders/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLIER_ORDERS_WRITE + "')")
    public SupplierOrderResponse updateSupplierOrder(
            @PathVariable Integer id,
            @Valid @RequestBody SupplierOrderRequest request
    ) {
        return supplyService.updateSupplierOrder(id, request);
    }

    @DeleteMapping("/supplier-orders/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLIER_ORDERS_WRITE + "')")
    public ResponseEntity<Void> deleteSupplierOrder(@PathVariable Integer id) {
        supplyService.deleteSupplierOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/supplier-orders/{supplierOrderId}/deliveries")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_DELIVERIES_WRITE + "')")
    public DeliveryWithItemsResponse createDelivery(
            @PathVariable Integer supplierOrderId,
            @Valid @RequestBody CreateDeliveryWithItemsRequest request
    ) {
        return supplyService.createDeliveryForSupplierOrder(supplierOrderId, request);
    }

    @GetMapping("/deliveries")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_DELIVERIES_READ + "')")
    public PagedResponse<DeliveryResponse> findAllDeliveries(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return supplyService.findAllDeliveries(page, size);
    }

    @GetMapping("/deliveries/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_DELIVERIES_READ + "')")
    public DeliveryResponse findDeliveryById(@PathVariable Integer id) {
        return supplyService.findDeliveryById(id);
    }

    @PutMapping("/deliveries/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_DELIVERIES_WRITE + "')")
    public DeliveryResponse updateDelivery(
            @PathVariable Integer id,
            @Valid @RequestBody DeliveryRequest request
    ) {
        return supplyService.updateDelivery(id, request);
    }

    @DeleteMapping("/deliveries/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_DELIVERIES_WRITE + "')")
    public ResponseEntity<Void> deleteDelivery(@PathVariable Integer id) {
        supplyService.deleteDelivery(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/supply-request-items")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLY_REQUESTS_READ + "')")
    public PagedResponse<SupplyRequestItemResponse> findAllSupplyRequestItems(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return supplyService.findAllSupplyRequestItems(page, size);
    }

    @GetMapping("/supply-request-items/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLY_REQUESTS_READ + "')")
    public SupplyRequestItemResponse findSupplyRequestItemById(@PathVariable Integer id) {
        return supplyService.findSupplyRequestItemById(id);
    }

    @PostMapping("/supply-request-items")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLY_REQUESTS_WRITE + "')")
    public SupplyRequestItemResponse createSupplyRequestItem(@Valid @RequestBody SupplyRequestItemRequest request) {
        return supplyService.createSupplyRequestItem(request);
    }

    @PutMapping("/supply-request-items/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLY_REQUESTS_WRITE + "')")
    public SupplyRequestItemResponse updateSupplyRequestItem(
            @PathVariable Integer id,
            @Valid @RequestBody SupplyRequestItemRequest request
    ) {
        return supplyService.updateSupplyRequestItem(id, request);
    }

    @DeleteMapping("/supply-request-items/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLY_REQUESTS_WRITE + "')")
    public ResponseEntity<Void> deleteSupplyRequestItem(@PathVariable Integer id) {
        supplyService.deleteSupplyRequestItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/supplier-order-items")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLIER_ORDERS_READ + "')")
    public PagedResponse<SupplierOrderItemResponse> findAllSupplierOrderItems(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return supplyService.findAllSupplierOrderItems(page, size);
    }

    @GetMapping("/supplier-order-items/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLIER_ORDERS_READ + "')")
    public SupplierOrderItemResponse findSupplierOrderItemById(@PathVariable Integer id) {
        return supplyService.findSupplierOrderItemById(id);
    }

    @PostMapping("/supplier-order-items")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLIER_ORDERS_WRITE + "')")
    public SupplierOrderItemResponse createSupplierOrderItem(@Valid @RequestBody SupplierOrderItemRequest request) {
        return supplyService.createSupplierOrderItem(request);
    }

    @PutMapping("/supplier-order-items/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLIER_ORDERS_WRITE + "')")
    public SupplierOrderItemResponse updateSupplierOrderItem(
            @PathVariable Integer id,
            @Valid @RequestBody SupplierOrderItemRequest request
    ) {
        return supplyService.updateSupplierOrderItem(id, request);
    }

    @DeleteMapping("/supplier-order-items/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLIER_ORDERS_WRITE + "')")
    public ResponseEntity<Void> deleteSupplierOrderItem(@PathVariable Integer id) {
        supplyService.deleteSupplierOrderItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/delivery-items")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_DELIVERIES_READ + "')")
    public PagedResponse<DeliveryItemResponse> findAllDeliveryItems(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return supplyService.findAllDeliveryItems(page, size);
    }

    @GetMapping("/delivery-items/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_DELIVERIES_READ + "')")
    public DeliveryItemResponse findDeliveryItemById(@PathVariable Integer id) {
        return supplyService.findDeliveryItemById(id);
    }

    @PostMapping("/delivery-items")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_DELIVERIES_WRITE + "')")
    public DeliveryItemResponse createDeliveryItem(@Valid @RequestBody DeliveryItemRequest request) {
        return supplyService.createDeliveryItem(request);
    }

    @PutMapping("/delivery-items/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_DELIVERIES_WRITE + "')")
    public DeliveryItemResponse updateDeliveryItem(
            @PathVariable Integer id,
            @Valid @RequestBody DeliveryItemRequest request
    ) {
        return supplyService.updateDeliveryItem(id, request);
    }

    @DeleteMapping("/delivery-items/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_DELIVERIES_WRITE + "')")
    public ResponseEntity<Void> deleteDeliveryItem(@PathVariable Integer id) {
        supplyService.deleteDeliveryItem(id);
        return ResponseEntity.noContent().build();
    }
}
