package ru.nsu.tois.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.nsu.tois.authentication.PermissionCodes;
import ru.nsu.tois.dto.request.SaleItemRequest;
import ru.nsu.tois.dto.request.SaleRequest;
import ru.nsu.tois.dto.response.PagedResponse;
import ru.nsu.tois.dto.response.SaleItemResponse;
import ru.nsu.tois.dto.response.SaleResponse;
import ru.nsu.tois.service.SalesService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager")
public class ManagerSalesController {
    private final SalesService salesService;

    @GetMapping("/sales")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SALES_READ + "')")
    public PagedResponse<SaleResponse> findAllSales(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return salesService.findAllSales(page, size);
    }

    @GetMapping("/sales/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SALES_READ + "')")
    public SaleResponse findSaleById(@PathVariable Integer id) {
        return salesService.findSaleById(id);
    }

    @PostMapping("/sales")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SALES_WRITE + "')")
    public SaleResponse createSale(@Valid @RequestBody SaleRequest request) {
        return salesService.createSale(request);
    }

    @PutMapping("/sales/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SALES_WRITE + "')")
    public SaleResponse updateSale(@PathVariable Integer id, @Valid @RequestBody SaleRequest request) {
        return salesService.updateSale(id, request);
    }

    @DeleteMapping("/sales/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SALES_WRITE + "')")
    public ResponseEntity<Void> deleteSale(@PathVariable Integer id) {
        salesService.deleteSale(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sale-items")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SALES_READ + "')")
    public PagedResponse<SaleItemResponse> findAllSaleItems(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return salesService.findAllSaleItems(page, size);
    }

    @GetMapping("/sale-items/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SALES_READ + "')")
    public SaleItemResponse findSaleItemById(@PathVariable Integer id) {
        return salesService.findSaleItemById(id);
    }

    @PostMapping("/sale-items")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SALES_WRITE + "')")
    public SaleItemResponse createSaleItem(@Valid @RequestBody SaleItemRequest request) {
        return salesService.createSaleItem(request);
    }

    @PutMapping("/sale-items/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SALES_WRITE + "')")
    public SaleItemResponse updateSaleItem(@PathVariable Integer id, @Valid @RequestBody SaleItemRequest request) {
        return salesService.updateSaleItem(id, request);
    }

    @DeleteMapping("/sale-items/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SALES_WRITE + "')")
    public ResponseEntity<Void> deleteSaleItem(@PathVariable Integer id) {
        salesService.deleteSaleItem(id);
        return ResponseEntity.noContent().build();
    }
}
