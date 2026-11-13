package ru.nsu.tois.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.nsu.tois.authentication.PermissionCodes;
import ru.nsu.tois.dto.request.CreateProductTransferWithItemsRequest;
import ru.nsu.tois.dto.request.ProductTransferItemRequest;
import ru.nsu.tois.dto.request.ProductTransferRequest;
import ru.nsu.tois.dto.response.PagedResponse;
import ru.nsu.tois.dto.response.ProductTransferItemResponse;
import ru.nsu.tois.dto.response.ProductTransferResponse;
import ru.nsu.tois.dto.response.ProductTransferWithItemsResponse;
import ru.nsu.tois.service.TransferService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager")
public class ManagerTransferController {
    private final TransferService transferService;

    @GetMapping("/transfers")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRANSFERS_READ + "')")
    public PagedResponse<ProductTransferResponse> findAllTransfers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return transferService.findAllProductTransfers(page, size);
    }

    @GetMapping("/transfers/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRANSFERS_READ + "')")
    public ProductTransferResponse findTransferById(@PathVariable Integer id) {
        return transferService.findProductTransferById(id);
    }

    @PostMapping("/transfers")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRANSFERS_WRITE + "')")
    public ProductTransferWithItemsResponse createTransfer(
            @Valid @RequestBody CreateProductTransferWithItemsRequest request
    ) {
        return transferService.createProductTransferWithItems(request);
    }

    @PutMapping("/transfers/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRANSFERS_WRITE + "')")
    public ProductTransferResponse updateTransfer(
            @PathVariable Integer id,
            @Valid @RequestBody ProductTransferRequest request
    ) {
        return transferService.updateProductTransfer(id, request);
    }

    @DeleteMapping("/transfers/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRANSFERS_WRITE + "')")
    public ResponseEntity<Void> deleteTransfer(@PathVariable Integer id) {
        transferService.deleteProductTransfer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/transfer-items")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRANSFERS_READ + "')")
    public PagedResponse<ProductTransferItemResponse> findAllTransferItems(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return transferService.findAllProductTransferItems(page, size);
    }

    @GetMapping("/transfer-items/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRANSFERS_READ + "')")
    public ProductTransferItemResponse findTransferItemById(@PathVariable Integer id) {
        return transferService.findProductTransferItemById(id);
    }

    @PostMapping("/transfer-items")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRANSFERS_WRITE + "')")
    public ProductTransferItemResponse createTransferItem(@Valid @RequestBody ProductTransferItemRequest request) {
        return transferService.createProductTransferItem(request);
    }

    @PutMapping("/transfer-items/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRANSFERS_WRITE + "')")
    public ProductTransferItemResponse updateTransferItem(
            @PathVariable Integer id,
            @Valid @RequestBody ProductTransferItemRequest request
    ) {
        return transferService.updateProductTransferItem(id, request);
    }

    @DeleteMapping("/transfer-items/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRANSFERS_WRITE + "')")
    public ResponseEntity<Void> deleteTransferItem(@PathVariable Integer id) {
        transferService.deleteProductTransferItem(id);
        return ResponseEntity.noContent().build();
    }
}
