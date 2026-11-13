package ru.nsu.tois.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.tois.dto.request.SaleItemRequest;
import ru.nsu.tois.dto.request.SaleRequest;
import ru.nsu.tois.dto.request.SellerSaleRequest;
import ru.nsu.tois.dto.response.PagedResponse;
import ru.nsu.tois.dto.response.SaleItemResponse;
import ru.nsu.tois.dto.response.SaleResponse;
import ru.nsu.tois.dto.response.SaleWithItemsResponse;
import ru.nsu.tois.entity.catalog.TradePointProductPrice;
import ru.nsu.tois.entity.catalog.TradePointProductPriceId;
import ru.nsu.tois.entity.employees.Employee;
import ru.nsu.tois.entity.employees.EmployeePosition;
import ru.nsu.tois.entity.sales.Sale;
import ru.nsu.tois.entity.sales.SaleItem;
import ru.nsu.tois.entity.tradePoints.TradePoint;
import ru.nsu.tois.entity.tradePoints.TradePointType;
import ru.nsu.tois.mapper.SalesMapper;
import ru.nsu.tois.repository.assignments.SellerAssignmentRepository;
import ru.nsu.tois.repository.catalog.CustomerRepository;
import ru.nsu.tois.repository.catalog.ProductRepository;
import ru.nsu.tois.repository.catalog.TradePointProductPriceRepository;
import ru.nsu.tois.repository.employees.EmployeeRepository;
import ru.nsu.tois.repository.sales.SaleItemRepository;
import ru.nsu.tois.repository.sales.SaleRepository;
import ru.nsu.tois.repository.tradePoints.TradePointRepository;
import ru.nsu.tois.util.PaginationUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesService {
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final TradePointRepository tradePointRepository;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final TradePointProductPriceRepository tradePointProductPriceRepository;
    private final StockService stockService;
    private final SellerAssignmentRepository sellerAssignmentRepository;
    private final CurrentUserService currentUserService;
    private final SalesMapper salesMapper;

    @Transactional(readOnly = true)
    public PagedResponse<SaleResponse> findAllSales(Integer page, Integer size) {
        return PaginationUtils.toResponse(saleRepository
                .findAll(PaginationUtils.request(page, size, "id"))
                .map(salesMapper::toDto));
    }

    @Transactional(readOnly = true)
    public SaleResponse findSaleById(Integer id) {
        return salesMapper.toDto(requireSale(id));
    }

    public SaleResponse createSale(SaleRequest request) {
        validateSaleRequest(request);
        return salesMapper.toDto(saleRepository.save(salesMapper.toEntity(request)));
    }

    public SaleResponse updateSale(Integer id, SaleRequest request) {
        Sale existing = requireSaleForUpdate(id);
        validateSaleRequest(request);
        if (!existing.getTradePoint().getId().equals(request.tradePointId())) {
            throw new IllegalStateException("The trade point of a sale cannot be changed");
        }
        Sale sale = salesMapper.toEntity(request);
        sale.setId(id);
        return salesMapper.toDto(saleRepository.save(sale));
    }

    public void deleteSale(Integer id) {
        Sale sale = requireSaleForUpdate(id);
        Integer tradePointId = sale.getTradePoint().getId();
        List<SaleItem> items = saleItemRepository.findBySale_Id(id);
        for (SaleItem item : items) {
            stockService.increase(tradePointId, item.getProduct().getId(), item.getQuantity());
        }
        saleItemRepository.deleteAll(items);
        saleRepository.delete(sale);
    }

    @Transactional(readOnly = true)
    public PagedResponse<SaleItemResponse> findAllSaleItems(Integer page, Integer size) {
        return PaginationUtils.toResponse(saleItemRepository
                .findAll(PaginationUtils.request(page, size, "id"))
                .map(salesMapper::toDto));
    }

    @Transactional(readOnly = true)
    public SaleItemResponse findSaleItemById(Integer id) {
        return salesMapper.toDto(requireSaleItem(id));
    }

    public SaleItemResponse createSaleItem(SaleItemRequest request) {
        Sale sale = requireSaleForUpdate(request.saleId());
        validateSaleProduct(request.productId());
        stockService.decrease(sale.getTradePoint().getId(), request.productId(), request.quantity());
        return salesMapper.toDto(saleItemRepository.save(salesMapper.toEntity(request)));
    }

    public SaleItemResponse updateSaleItem(Integer id, SaleItemRequest request) {
        requireSaleForUpdate(request.saleId());
        SaleItem existing = requireSaleItem(id);
        if (!existing.getSale().getId().equals(request.saleId())
                || !existing.getProduct().getId().equals(request.productId())) {
            throw new IllegalStateException("The sale and product of a sale item cannot be changed");
        }

        Integer tradePointId = existing.getSale().getTradePoint().getId();
        int difference = request.quantity() - existing.getQuantity();
        if (difference > 0) {
            stockService.decrease(tradePointId, request.productId(), difference);
        } else if (difference < 0) {
            stockService.increase(tradePointId, request.productId(), -difference);
        }

        existing.setQuantity(request.quantity());
        existing.setUnitPrice(request.unitPrice());
        return salesMapper.toDto(existing);
    }

    public void deleteSaleItem(Integer id) {
        Integer saleId = saleItemRepository.findSaleIdByItemId(id)
                .orElseThrow(() -> notFound("SaleItem", id));
        requireSaleForUpdate(saleId);
        SaleItem item = requireSaleItem(id);
        stockService.increase(item.getSale().getTradePoint().getId(), item.getProduct().getId(), item.getQuantity());
        saleItemRepository.delete(item);
    }

    public SaleWithItemsResponse createCurrentSellerSale(SellerSaleRequest request) {
        Employee seller = currentUserService.requireCurrentEmployee(EmployeePosition.SELLER);
        validateSellerTradePoint(seller.getId(), request.tradePointId());

        SaleResponse saleResponse = createSale(new SaleRequest(
                request.saleDate(),
                request.tradePointId(),
                seller.getId(),
                request.customerId()
        ));

        List<SaleItemResponse> items = request.items().stream()
                .map(item -> createSaleItemWithServerPrice(
                        saleResponse.id(),
                        request.tradePointId(),
                        item.productId(),
                        item.quantity()
                ))
                .toList();

        return new SaleWithItemsResponse(saleResponse, items);
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> findCurrentSellerSales() {
        Employee seller = currentUserService.requireCurrentEmployee(EmployeePosition.SELLER);
        return saleRepository.findBySeller_Id(seller.getId()).stream()
                .map(salesMapper::toDto)
                .toList();
    }

    private SaleItemResponse createSaleItemWithServerPrice(
            Integer saleId,
            Integer tradePointId,
            Integer productId,
            Integer quantity
    ) {
        TradePointProductPrice price = tradePointProductPriceRepository
                .findById(new TradePointProductPriceId(tradePointId, productId))
                .orElseThrow(() -> new EntityNotFoundException(
                        "TradePointProductPrice not found: tradePoint=" + tradePointId + ", product=" + productId
                ));

        return createSaleItem(new SaleItemRequest(saleId, productId, quantity, price.getPrice()));
    }

    private void validateSellerTradePoint(Integer sellerId, Integer tradePointId) {
        if (!sellerAssignmentRepository.existsBySeller_IdAndTradePoint_Id(sellerId, tradePointId)) {
            throw new IllegalArgumentException("Seller " + sellerId + " is not assigned to trade point " + tradePointId);
        }
    }

    private void validateSaleRequest(SaleRequest request) {
        TradePoint tradePoint = tradePointRepository.findById(request.tradePointId())
                .orElseThrow(() -> notFound("TradePoint", request.tradePointId()));
        requireEmployeeWithPosition(request.sellerId(), EmployeePosition.SELLER);
        if (request.customerId() != null) {
            customerRepository.findById(request.customerId()).orElseThrow(() -> notFound("Customer", request.customerId()));
            if (tradePoint.getType() == TradePointType.KIOSK || tradePoint.getType() == TradePointType.STALL) {
                throw new IllegalArgumentException("Cannot create sale in kiosk or stall with customer");
            }
        }
    }

    private void validateSaleProduct(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw notFound("Product", productId);
        }
    }

    private Employee requireEmployeeWithPosition(Integer id, EmployeePosition position) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> notFound("Employee", id));
        if (employee.getPosition() != position) {
            throw new IllegalArgumentException("Employee " + id + " is not " + position);
        }
        return employee;
    }

    private Sale requireSale(Integer id) {
        return saleRepository.findById(id).orElseThrow(() -> notFound("Sale", id));
    }

    private Sale requireSaleForUpdate(Integer id) {
        return saleRepository.findByIdForUpdate(id).orElseThrow(() -> notFound("Sale", id));
    }

    private SaleItem requireSaleItem(Integer id) {
        return saleItemRepository.findById(id).orElseThrow(() -> notFound("SaleItem", id));
    }

    private EntityNotFoundException notFound(String entityName, Integer id) {
        return new EntityNotFoundException(entityName + " not found: " + id);
    }
}
