package ru.nsu.tois.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.tois.dto.request.*;
import ru.nsu.tois.dto.response.*;
import ru.nsu.tois.dto.response.PagedResponse;
import ru.nsu.tois.entity.employees.Employee;
import ru.nsu.tois.entity.employees.EmployeePosition;
import ru.nsu.tois.entity.supply.*;
import ru.nsu.tois.mapper.SupplyMapper;
import ru.nsu.tois.repository.catalog.ProductRepository;
import ru.nsu.tois.repository.employees.EmployeeRepository;
import ru.nsu.tois.repository.suppliers.SupplierRepository;
import ru.nsu.tois.repository.supply.*;
import ru.nsu.tois.repository.tradePoints.TradePointRepository;
import ru.nsu.tois.util.PaginationUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplyService {
    private final SupplyRequestRepository supplyRequestRepository;
    private final SupplyRequestItemRepository supplyRequestItemRepository;
    private final SupplierOrderRepository supplierOrderRepository;
    private final SupplierOrderItemRepository supplierOrderItemRepository;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryItemRepository deliveryItemRepository;
    private final TradePointRepository tradePointRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;
    private final SupplierRepository supplierRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrentUserService currentUserService;
    private final ProductResolverService productResolverService;
    private final SupplyMapper supplyMapper;

    @Transactional(readOnly = true)
    public PagedResponse<SupplyRequestResponse> findAllSupplyRequests(Integer page, Integer size) {
        return PaginationUtils.toResponse(supplyRequestRepository
                .findAll(PaginationUtils.request(page, size, "id"))
                .map(supplyMapper::toDto));
    }

    @Transactional(readOnly = true)
    public SupplyRequestResponse findSupplyRequestById(Integer id) {
        return supplyMapper.toDto(requireSupplyRequest(id));
    }

    public SupplyRequestResponse createSupplyRequest(SupplyRequestRequest request) {
        tradePointRepository.findById(request.tradePointId()).orElseThrow(() -> notFound("TradePoint", request.tradePointId()));
        return supplyMapper.toDto(supplyRequestRepository.save(supplyMapper.toEntity(request)));
    }

    public SupplyRequestResponse updateSupplyRequest(Integer id, SupplyRequestRequest request) {
        SupplyRequest existing = requireSupplyRequest(id);
        if (!existing.getTradePoint().getId().equals(request.tradePointId())) {
            throw new IllegalStateException("The trade point of a supply request cannot be changed");
        }
        tradePointRepository.findById(request.tradePointId()).orElseThrow(() -> notFound("TradePoint", request.tradePointId()));
        SupplyRequest supplyRequest = supplyMapper.toEntity(request);
        supplyRequest.setId(id);
        return supplyMapper.toDto(supplyRequestRepository.save(supplyRequest));
    }

    public void deleteSupplyRequest(Integer id) {
        requireSupplyRequest(id);
        for (SupplierOrder supplierOrder : supplierOrderRepository.findBySupplyRequest_Id(id)) {
            deleteSupplierOrder(supplierOrder.getId());
        }
        supplyRequestRepository.deleteById(id);
    }

    public SupplyRequestWithItemsResponse createSupplyRequestWithItems(CreateSupplyRequestWithItemsRequest request) {
        String status = request.status() == null || request.status().isBlank() ? "NEW" : request.status();
        SupplyRequestResponse supplyRequest = createSupplyRequest(new SupplyRequestRequest(
                request.requestDate(),
                request.tradePointId(),
                status
        ));

        List<SupplyRequestItemResponse> items = request.items().stream()
                .map(item -> {
                    Integer productId = productResolverService.requireOrCreateProduct(
                            item.productId(),
                            item.productName(),
                            item.units()
                    ).getId();
                    return createSupplyRequestItem(new SupplyRequestItemRequest(
                            supplyRequest.id(),
                            productId,
                            item.requestedQuantity()
                    ));
                })
                .toList();

        return new SupplyRequestWithItemsResponse(supplyRequest, items);
    }

    public SupplyRequestResponse updateSupplyRequestStatus(Integer id, StatusUpdateRequest request) {
        SupplyRequest supplyRequest = requireSupplyRequest(id);
        supplyRequest.setStatus(request.status());
        return supplyMapper.toDto(supplyRequestRepository.save(supplyRequest));
    }

    @Transactional(readOnly = true)
    public PagedResponse<SupplyRequestItemResponse> findAllSupplyRequestItems(Integer page, Integer size) {
        return PaginationUtils.toResponse(supplyRequestItemRepository
                .findAll(PaginationUtils.request(page, size, "id"))
                .map(supplyMapper::toDto));
    }

    @Transactional(readOnly = true)
    public SupplyRequestItemResponse findSupplyRequestItemById(Integer id) {
        return supplyMapper.toDto(requireSupplyRequestItem(id));
    }

    public SupplyRequestItemResponse createSupplyRequestItem(SupplyRequestItemRequest request) {
        validateSupplyRequestItemRequest(request);
        return supplyMapper.toDto(supplyRequestItemRepository.save(supplyMapper.toEntity(request)));
    }

    public SupplyRequestItemResponse updateSupplyRequestItem(Integer id, SupplyRequestItemRequest request) {
        requireSupplyRequestItem(id);
        validateSupplyRequestItemRequest(request);
        SupplyRequestItem item = supplyMapper.toEntity(request);
        item.setId(id);
        return supplyMapper.toDto(supplyRequestItemRepository.save(item));
    }

    public void deleteSupplyRequestItem(Integer id) {
        requireSupplyRequestItem(id);
        supplyRequestItemRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PagedResponse<SupplierOrderResponse> findAllSupplierOrders(Integer page, Integer size) {
        return PaginationUtils.toResponse(supplierOrderRepository
                .findAll(PaginationUtils.request(page, size, "id"))
                .map(supplyMapper::toDto));
    }

    @Transactional(readOnly = true)
    public SupplierOrderResponse findSupplierOrderById(Integer id) {
        return supplyMapper.toDto(requireSupplierOrder(id));
    }

    public SupplierOrderResponse createSupplierOrder(SupplierOrderRequest request) {
        validateSupplierOrderRequest(request);
        return supplyMapper.toDto(supplierOrderRepository.save(supplyMapper.toEntity(request)));
    }

    public SupplierOrderResponse updateSupplierOrder(Integer id, SupplierOrderRequest request) {
        SupplierOrder existing = requireSupplierOrder(id);
        if (!existing.getSupplier().getId().equals(request.supplierId())
                || !existing.getSupplyRequest().getId().equals(request.supplyRequestId())) {
            throw new IllegalStateException("The supplier and supply request of an order cannot be changed");
        }
        validateSupplierOrderRequest(request);
        SupplierOrder supplierOrder = supplyMapper.toEntity(request);
        supplierOrder.setId(id);
        return supplyMapper.toDto(supplierOrderRepository.save(supplierOrder));
    }

    public void deleteSupplierOrder(Integer id) {
        requireSupplierOrder(id);
        for (Delivery delivery : deliveryRepository.findBySupplierOrder_Id(id)) {
            deleteDelivery(delivery.getId());
        }
        supplierOrderRepository.deleteById(id);
    }

    public SupplierOrderWithItemsResponse createCurrentManagerSupplierOrder(
            Integer supplyRequestId,
            CreateSupplierOrderWithItemsRequest request
    ) {
        Employee manager = currentUserService.requireCurrentEmployee(EmployeePosition.TRADE_MANAGER);
        SupplierOrderResponse supplierOrder = createSupplierOrder(new SupplierOrderRequest(
                request.orderDate(),
                request.orderNumber(),
                manager.getId(),
                request.supplierId(),
                supplyRequestId,
                request.status()
        ));

        List<SupplierOrderItemResponse> items = request.items().stream()
                .map(item -> {
                    Integer productId = productResolverService.requireOrCreateProduct(
                            item.productId(),
                            item.productName(),
                            item.units()
                    ).getId();
                    return createSupplierOrderItem(new SupplierOrderItemRequest(
                            supplierOrder.id(),
                            productId,
                            item.quantity(),
                            item.purchasePrice()
                    ));
                })
                .toList();

        return new SupplierOrderWithItemsResponse(supplierOrder, items);
    }

    @Transactional(readOnly = true)
    public PagedResponse<SupplierOrderItemResponse> findAllSupplierOrderItems(Integer page, Integer size) {
        return PaginationUtils.toResponse(supplierOrderItemRepository
                .findAll(PaginationUtils.request(page, size, "id"))
                .map(supplyMapper::toDto));
    }

    @Transactional(readOnly = true)
    public SupplierOrderItemResponse findSupplierOrderItemById(Integer id) {
        return supplyMapper.toDto(requireSupplierOrderItem(id));
    }

    public SupplierOrderItemResponse createSupplierOrderItem(SupplierOrderItemRequest request) {
        validateSupplierOrderItemRequest(request);
        return supplyMapper.toDto(supplierOrderItemRepository.save(supplyMapper.toEntity(request)));
    }

    public SupplierOrderItemResponse updateSupplierOrderItem(Integer id, SupplierOrderItemRequest request) {
        requireSupplierOrderItem(id);
        validateSupplierOrderItemRequest(request);
        SupplierOrderItem item = supplyMapper.toEntity(request);
        item.setId(id);
        return supplyMapper.toDto(supplierOrderItemRepository.save(item));
    }

    public void deleteSupplierOrderItem(Integer id) {
        requireSupplierOrderItem(id);
        supplierOrderItemRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PagedResponse<DeliveryResponse> findAllDeliveries(Integer page, Integer size) {
        return PaginationUtils.toResponse(deliveryRepository
                .findAll(PaginationUtils.request(page, size, "id"))
                .map(supplyMapper::toDto));
    }

    @Transactional(readOnly = true)
    public DeliveryResponse findDeliveryById(Integer id) {
        return supplyMapper.toDto(requireDelivery(id));
    }

    public DeliveryResponse createDelivery(DeliveryRequest request) {
        validateDeliveryRequest(request);
        return supplyMapper.toDto(deliveryRepository.save(supplyMapper.toEntity(request)));
    }

    public DeliveryResponse updateDelivery(Integer id, DeliveryRequest request) {
        Delivery existing = requireDeliveryForUpdate(id);
        if (!existing.getSupplierOrder().getId().equals(request.supplierOrderId())) {
            throw new IllegalStateException("The supplier order of a delivery cannot be changed");
        }
        validateDeliveryRequest(request);
        Delivery delivery = supplyMapper.toEntity(request);
        delivery.setId(id);
        return supplyMapper.toDto(deliveryRepository.save(delivery));
    }

    public void deleteDelivery(Integer id) {
        requireDeliveryForUpdate(id);
        for (DeliveryItem item : deliveryItemRepository.findByDelivery_Id(id)) {
            deleteDeliveryItem(item.getId());
        }
        deliveryRepository.deleteById(id);
    }

    public DeliveryWithItemsResponse createDeliveryForSupplierOrder(
            Integer supplierOrderId,
            CreateDeliveryWithItemsRequest request
    ) {
        SupplierOrder supplierOrder = requireSupplierOrder(supplierOrderId);
        Integer supplierId = supplierOrder.getSupplier() == null ? null : supplierOrder.getSupplier().getId();
        DeliveryResponse delivery = createDelivery(new DeliveryRequest(
                supplierId,
                supplierOrderId,
                request.deliveryDate()
        ));

        List<DeliveryItemResponse> items = new ArrayList<>();
        for (DeliveryLineRequest item : request.items()) {
            Integer productId = productResolverService.requireOrCreateProduct(
                    item.productId(),
                    item.productName(),
                    item.units()
            ).getId();
            DeliveryItemResponse deliveryItem = createDeliveryItem(new DeliveryItemRequest(
                    delivery.id(),
                    productId,
                    item.quantity(),
                    item.deliveryPrice()
            ));
            items.add(deliveryItem);
        }

        return new DeliveryWithItemsResponse(delivery, items);
    }

    @Transactional(readOnly = true)
    public PagedResponse<DeliveryItemResponse> findAllDeliveryItems(Integer page, Integer size) {
        return PaginationUtils.toResponse(deliveryItemRepository
                .findAll(PaginationUtils.request(page, size, "id"))
                .map(supplyMapper::toDto));
    }

    @Transactional(readOnly = true)
    public DeliveryItemResponse findDeliveryItemById(Integer id) {
        return supplyMapper.toDto(requireDeliveryItem(id));
    }

    public DeliveryItemResponse createDeliveryItem(DeliveryItemRequest request) {
        Delivery delivery = requireDeliveryForUpdate(request.deliveryId());
        validateDeliveryProduct(request.productId());
        DeliveryItem item = deliveryItemRepository.save(supplyMapper.toEntity(request));
        stockService.increase(targetTradePointId(delivery), request.productId(), request.quantity());
        return supplyMapper.toDto(item);
    }

    public DeliveryItemResponse updateDeliveryItem(Integer id, DeliveryItemRequest request) {
        Delivery delivery = requireDeliveryForUpdate(request.deliveryId());
        DeliveryItem existing = requireDeliveryItem(id);
        if (!existing.getDelivery().getId().equals(request.deliveryId())
                || !existing.getProduct().getId().equals(request.productId())) {
            throw new IllegalStateException("The delivery and product of a delivery item cannot be changed");
        }

        int tradePointId = targetTradePointId(delivery);
        int difference = request.quantity() - existing.getQuantity();
        if (difference > 0) {
            stockService.increase(tradePointId, request.productId(), difference);
        } else if (difference < 0) {
            stockService.decrease(tradePointId, request.productId(), -difference);
        }

        existing.setQuantity(request.quantity());
        existing.setDeliveryPrice(request.deliveryPrice());
        return supplyMapper.toDto(existing);
    }

    public void deleteDeliveryItem(Integer id) {
        Integer deliveryId = deliveryItemRepository.findDeliveryIdByItemId(id)
                .orElseThrow(() -> notFound("DeliveryItem", id));
        requireDeliveryForUpdate(deliveryId);
        DeliveryItem item = requireDeliveryItem(id);
        stockService.decrease(targetTradePointId(item.getDelivery()), item.getProduct().getId(), item.getQuantity());
        deliveryItemRepository.delete(item);
    }

    private void validateSupplyRequestItemRequest(SupplyRequestItemRequest request) {
        requireSupplyRequest(request.supplyRequestId());
        productRepository.findById(request.productId()).orElseThrow(() -> notFound("Product", request.productId()));
    }

    private void validateSupplierOrderRequest(SupplierOrderRequest request) {
        requireEmployeeWithPosition(request.managerId(), EmployeePosition.TRADE_MANAGER);
        supplierRepository.findById(request.supplierId()).orElseThrow(() -> notFound("Supplier", request.supplierId()));
        requireSupplyRequest(request.supplyRequestId());
    }

    private void validateSupplierOrderItemRequest(SupplierOrderItemRequest request) {
        requireSupplierOrder(request.supplierOrderId());
        productRepository.findById(request.productId()).orElseThrow(() -> notFound("Product", request.productId()));
    }

    private void validateDeliveryRequest(DeliveryRequest request) {
        SupplierOrder order = requireSupplierOrder(request.supplierOrderId());
        if (!order.getSupplier().getId().equals(request.supplierId())) {
            throw new IllegalArgumentException("Delivery supplier must match the supplier order");
        }
    }

    private void validateDeliveryProduct(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw notFound("Product", productId);
        }
    }

    private Delivery requireDeliveryForUpdate(Integer id) {
        return deliveryRepository.findLockedById(id).orElseThrow(() -> notFound("Delivery", id));
    }

    private Integer targetTradePointId(Delivery delivery) {
        SupplierOrder order = requireSupplierOrder(delivery.getSupplierOrder().getId());
        SupplyRequest supplyRequest = requireSupplyRequest(order.getSupplyRequest().getId());
        return supplyRequest.getTradePoint().getId();
    }

    private Employee requireEmployeeWithPosition(Integer id, EmployeePosition position) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> notFound("Employee", id));
        if (employee.getPosition() != position) {
            throw new IllegalArgumentException("Employee " + id + " is not " + position);
        }
        return employee;
    }

    private SupplyRequest requireSupplyRequest(Integer id) {
        return supplyRequestRepository.findById(id).orElseThrow(() -> notFound("SupplyRequest", id));
    }

    private SupplyRequestItem requireSupplyRequestItem(Integer id) {
        return supplyRequestItemRepository.findById(id).orElseThrow(() -> notFound("SupplyRequestItem", id));
    }

    private SupplierOrder requireSupplierOrder(Integer id) {
        return supplierOrderRepository.findById(id).orElseThrow(() -> notFound("SupplierOrder", id));
    }

    private SupplierOrderItem requireSupplierOrderItem(Integer id) {
        return supplierOrderItemRepository.findById(id).orElseThrow(() -> notFound("SupplierOrderItem", id));
    }

    private Delivery requireDelivery(Integer id) {
        return deliveryRepository.findById(id).orElseThrow(() -> notFound("Delivery", id));
    }

    private DeliveryItem requireDeliveryItem(Integer id) {
        return deliveryItemRepository.findById(id).orElseThrow(() -> notFound("DeliveryItem", id));
    }

    private EntityNotFoundException notFound(String entityName, Integer id) {
        return new EntityNotFoundException(entityName + " not found: " + id);
    }
}
