package ru.nsu.tois.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.nsu.tois.authentication.PermissionCodes;
import ru.nsu.tois.dto.request.CustomerRequest;
import ru.nsu.tois.dto.request.PriceUpdateRequest;
import ru.nsu.tois.dto.request.ProductRequest;
import ru.nsu.tois.dto.request.TradePointProductPriceRequest;
import ru.nsu.tois.dto.request.TradePointProductStockRequest;
import ru.nsu.tois.dto.response.CustomerResponse;
import ru.nsu.tois.dto.response.PagedResponse;
import ru.nsu.tois.dto.response.ProductResponse;
import ru.nsu.tois.dto.response.TradePointProductPriceResponse;
import ru.nsu.tois.dto.response.TradePointProductStockResponse;
import ru.nsu.tois.service.CatalogService;
import ru.nsu.tois.service.ReportService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager")
public class ManagerCatalogController {
    private final CatalogService catalogService;
    private final ReportService reportService;

    @GetMapping("/products")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_PRODUCTS_READ + "')")
    public PagedResponse<ProductResponse> findAllProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return catalogService.findAllProducts(page, size);
    }

    @GetMapping("/products/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_PRODUCTS_READ + "')")
    public ProductResponse findProductById(@PathVariable Integer id) {
        return catalogService.findProductById(id);
    }

    @PostMapping("/products")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_PRODUCTS_WRITE + "')")
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return catalogService.createProduct(request);
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_PRODUCTS_WRITE + "')")
    public ProductResponse updateProduct(@PathVariable Integer id, @Valid @RequestBody ProductRequest request) {
        return catalogService.updateProduct(id, request);
    }

    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_PRODUCTS_WRITE + "')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        catalogService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trade-point-prices")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_CATALOG_READ + "')")
    public PagedResponse<TradePointProductPriceResponse> findAllTradePointProductPrices(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return catalogService.findAllTradePointProductPrices(page, size);
    }

    @GetMapping("/trade-points/{tradePointId}/stocks")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_CATALOG_READ + "')")
    public List<Map<String, Object>> findStocks(@PathVariable Integer tradePointId) {
        return reportService.tradePointAssortment(tradePointId);
    }

    @GetMapping("/trade-points/{tradePointId}/prices")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_CATALOG_READ + "')")
    public List<TradePointProductPriceResponse> findPrices(@PathVariable Integer tradePointId) {
        return catalogService.findTradePointProductPricesByTradePoint(tradePointId);
    }

    @GetMapping("/trade-points/{tradePointId}/products/{productId}/price")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_CATALOG_READ + "')")
    public TradePointProductPriceResponse findTradePointProductPrice(
            @PathVariable Integer tradePointId,
            @PathVariable Integer productId
    ) {
        return catalogService.findTradePointProductPriceById(tradePointId, productId);
    }

    @PostMapping("/trade-point-prices")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_PRICES_WRITE + "')")
    public TradePointProductPriceResponse createTradePointProductPrice(
            @Valid @RequestBody TradePointProductPriceRequest request
    ) {
        return catalogService.createTradePointProductPrice(request);
    }

    @PatchMapping("/trade-points/{tradePointId}/products/{productId}/price")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_PRICES_WRITE + "')")
    public TradePointProductPriceResponse updatePrice(
            @PathVariable Integer tradePointId,
            @PathVariable Integer productId,
            @Valid @RequestBody PriceUpdateRequest request
    ) {
        return catalogService.updateTradePointProductPrice(tradePointId, productId, request.price());
    }

    @DeleteMapping("/trade-points/{tradePointId}/products/{productId}/price")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_PRICES_WRITE + "')")
    public ResponseEntity<Void> deleteTradePointProductPrice(
            @PathVariable Integer tradePointId,
            @PathVariable Integer productId
    ) {
        catalogService.deleteTradePointProductPrice(tradePointId, productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/customers")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_CUSTOMERS_READ + "')")
    public PagedResponse<CustomerResponse> findAllCustomers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return catalogService.findAllCustomers(page, size);
    }

    @GetMapping("/customers/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_CUSTOMERS_READ + "')")
    public CustomerResponse findCustomerById(@PathVariable Integer id) {
        return catalogService.findCustomerById(id);
    }

    @PostMapping("/customers")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_CUSTOMERS_WRITE + "')")
    public CustomerResponse createCustomer(@Valid @RequestBody CustomerRequest request) {
        return catalogService.createCustomer(request);
    }

    @PutMapping("/customers/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_CUSTOMERS_WRITE + "')")
    public CustomerResponse updateCustomer(@PathVariable Integer id, @Valid @RequestBody CustomerRequest request) {
        return catalogService.updateCustomer(id, request);
    }

    @DeleteMapping("/customers/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_CUSTOMERS_WRITE + "')")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Integer id) {
        catalogService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trade-point-stocks")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_STOCKS_READ + "')")
    public PagedResponse<TradePointProductStockResponse> findAllTradePointProductStocks(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return catalogService.findAllTradePointProductStocks(page, size);
    }

    @GetMapping("/trade-points/{tradePointId}/products/{productId}/stock")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_STOCKS_READ + "')")
    public TradePointProductStockResponse findTradePointProductStock(
            @PathVariable Integer tradePointId,
            @PathVariable Integer productId
    ) {
        return catalogService.findTradePointProductStockById(tradePointId, productId);
    }

    @PostMapping("/trade-point-stocks")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_STOCKS_WRITE + "')")
    public TradePointProductStockResponse createTradePointProductStock(
            @Valid @RequestBody TradePointProductStockRequest request
    ) {
        return catalogService.createTradePointProductStock(request);
    }

    @PutMapping("/trade-points/{tradePointId}/products/{productId}/stock")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_STOCKS_WRITE + "')")
    public TradePointProductStockResponse updateTradePointProductStock(
            @PathVariable Integer tradePointId,
            @PathVariable Integer productId,
            @Valid @RequestBody TradePointProductStockRequest request
    ) {
        return catalogService.updateTradePointProductStock(new TradePointProductStockRequest(
                tradePointId,
                productId,
                request.quantity()
        ));
    }

    @DeleteMapping("/trade-points/{tradePointId}/products/{productId}/stock")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_STOCKS_WRITE + "')")
    public ResponseEntity<Void> deleteTradePointProductStock(
            @PathVariable Integer tradePointId,
            @PathVariable Integer productId
    ) {
        catalogService.deleteTradePointProductStock(tradePointId, productId);
        return ResponseEntity.noContent().build();
    }
}
