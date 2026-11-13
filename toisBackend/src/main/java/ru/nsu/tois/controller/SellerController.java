package ru.nsu.tois.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.nsu.tois.authentication.PermissionCodes;
import ru.nsu.tois.dto.request.CreateSupplyRequestWithItemsRequest;
import ru.nsu.tois.dto.request.CustomerRequest;
import ru.nsu.tois.dto.request.SellerSaleRequest;
import ru.nsu.tois.dto.response.*;
import ru.nsu.tois.entity.employees.Employee;
import ru.nsu.tois.entity.employees.EmployeePosition;
import ru.nsu.tois.mapper.TradePointMapper;
import ru.nsu.tois.repository.assignments.SellerAssignmentRepository;
import ru.nsu.tois.service.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller")
public class SellerController {
    private final CatalogService catalogService;
    private final SalesService salesService;
    private final SupplyService supplyService;
    private final ReportService reportService;
    private final CurrentUserService currentUserService;
    private final SellerAssignmentRepository sellerAssignmentRepository;
    private final TradePointMapper tradePointMapper;

    @GetMapping("/trade-points")
    @PreAuthorize("hasAuthority('" + PermissionCodes.SELLER_CATALOG_READ + "')")
    public List<TradePointResponse> findAssignedTradePoints() {
        Employee seller = currentUserService.requireCurrentEmployee(EmployeePosition.SELLER);
        return sellerAssignmentRepository.findAssignedTradePoints(seller.getId()).stream()
                .map(tradePointMapper::toDto)
                .toList();
    }

    @PostMapping("/customers")
    @PreAuthorize("hasAuthority('" + PermissionCodes.SELLER_CUSTOMERS_WRITE + "')")
    public CustomerResponse createCustomer(@Valid @RequestBody CustomerRequest request) {
        return catalogService.createCustomer(request);
    }

    @GetMapping("/trade-points/{tradePointId}/stocks")
    @PreAuthorize("hasAuthority('" + PermissionCodes.SELLER_CATALOG_READ + "')")
    public List<Map<String, Object>> findStocks(@PathVariable Integer tradePointId) {
        requireAssignedTradePoint(tradePointId);
        return reportService.sellerTradePointStock(tradePointId);
    }

    @GetMapping("/trade-points/{tradePointId}/prices")
    @PreAuthorize("hasAuthority('" + PermissionCodes.SELLER_CATALOG_READ + "')")
    public List<TradePointProductPriceResponse> findPrices(@PathVariable Integer tradePointId) {
        requireAssignedTradePoint(tradePointId);
        return catalogService.findTradePointProductPricesByTradePoint(tradePointId);
    }

    @PostMapping("/sales")
    @PreAuthorize("hasAuthority('" + PermissionCodes.SELLER_SALES_WRITE + "')")
    public SaleWithItemsResponse createSale(@Valid @RequestBody SellerSaleRequest request) {
        return salesService.createCurrentSellerSale(request);
    }

    @GetMapping("/sales")
    @PreAuthorize("hasAuthority('" + PermissionCodes.SELLER_SALES_WRITE + "')")
    public List<SaleResponse> findCurrentSellerSales() {
        return salesService.findCurrentSellerSales();
    }

    @PostMapping("/supply-requests")
    @PreAuthorize("hasAuthority('" + PermissionCodes.SELLER_SUPPLY_REQUESTS_WRITE + "')")
    public SupplyRequestWithItemsResponse createSupplyRequest(
            @Valid @RequestBody CreateSupplyRequestWithItemsRequest request
    ) {
        requireAssignedTradePoint(request.tradePointId());
        return supplyService.createSupplyRequestWithItems(request);
    }

    @GetMapping("/trade-points/{tradePointId}/output")
    @PreAuthorize("hasAuthority('" + PermissionCodes.SELLER_REPORTS_READ + "')")
    public List<Map<String, Object>> findOwnOutput(
            @PathVariable Integer tradePointId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        Employee seller = requireAssignedTradePoint(tradePointId);
        return reportService.sellerOutput(seller.getId(), tradePointId, from, to);
    }

    private Employee requireAssignedTradePoint(Integer tradePointId) {
        Employee seller = currentUserService.requireCurrentEmployee(EmployeePosition.SELLER);
        if (!sellerAssignmentRepository.existsBySeller_IdAndTradePoint_Id(seller.getId(), tradePointId)) {
            throw new IllegalArgumentException("Seller is not assigned to trade point " + tradePointId);
        }
        return seller;
    }
}
