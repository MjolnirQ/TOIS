package ru.nsu.tois.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.nsu.tois.authentication.PermissionCodes;
import ru.nsu.tois.dto.request.SupplierProductRequest;
import ru.nsu.tois.dto.request.SupplierRequest;
import ru.nsu.tois.dto.response.PagedResponse;
import ru.nsu.tois.dto.response.SupplierProductResponse;
import ru.nsu.tois.dto.response.SupplierResponse;
import ru.nsu.tois.service.SupplierService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager")
public class ManagerSupplierController {
    private final SupplierService supplierService;

    @GetMapping("/suppliers")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLIERS_READ + "')")
    public PagedResponse<SupplierResponse> findAllSuppliers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return supplierService.findAllSuppliers(page, size);
    }

    @GetMapping("/suppliers/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLIERS_READ + "')")
    public SupplierResponse findSupplierById(@PathVariable Integer id) {
        return supplierService.findSupplierById(id);
    }

    @PostMapping("/suppliers")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLIERS_WRITE + "')")
    public SupplierResponse createSupplier(@Valid @RequestBody SupplierRequest request) {
        return supplierService.createSupplier(request);
    }

    @PutMapping("/suppliers/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLIERS_WRITE + "')")
    public SupplierResponse updateSupplier(@PathVariable Integer id, @Valid @RequestBody SupplierRequest request) {
        return supplierService.updateSupplier(id, request);
    }

    @DeleteMapping("/suppliers/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLIERS_WRITE + "')")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Integer id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/supplier-products")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLIER_PRODUCTS_READ + "')")
    public PagedResponse<SupplierProductResponse> findAllSupplierProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return supplierService.findAllSupplierProducts(page, size);
    }

    @GetMapping("/suppliers/{supplierId}/products/{productId}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLIER_PRODUCTS_READ + "')")
    public SupplierProductResponse findSupplierProductById(
            @PathVariable Integer supplierId,
            @PathVariable Integer productId
    ) {
        return supplierService.findSupplierProductById(supplierId, productId);
    }

    @PostMapping("/supplier-products")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLIER_PRODUCTS_WRITE + "')")
    public SupplierProductResponse createSupplierProduct(@Valid @RequestBody SupplierProductRequest request) {
        return supplierService.createSupplierProduct(request);
    }

    @PutMapping("/suppliers/{supplierId}/products/{productId}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLIER_PRODUCTS_WRITE + "')")
    public SupplierProductResponse updateSupplierProduct(
            @PathVariable Integer supplierId,
            @PathVariable Integer productId,
            @Valid @RequestBody SupplierProductRequest request
    ) {
        return supplierService.updateSupplierProduct(new SupplierProductRequest(
                supplierId,
                productId,
                null,
                null,
                request.supplierPrice()
        ));
    }

    @DeleteMapping("/suppliers/{supplierId}/products/{productId}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_SUPPLIER_PRODUCTS_WRITE + "')")
    public ResponseEntity<Void> deleteSupplierProduct(@PathVariable Integer supplierId, @PathVariable Integer productId) {
        supplierService.deleteSupplierProduct(supplierId, productId);
        return ResponseEntity.noContent().build();
    }
}
