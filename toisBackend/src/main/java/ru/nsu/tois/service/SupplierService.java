package ru.nsu.tois.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.tois.dto.request.SupplierProductRequest;
import ru.nsu.tois.dto.request.SupplierRequest;
import ru.nsu.tois.dto.response.PagedResponse;
import ru.nsu.tois.dto.response.SupplierProductResponse;
import ru.nsu.tois.dto.response.SupplierResponse;
import ru.nsu.tois.entity.suppliers.Supplier;
import ru.nsu.tois.entity.suppliers.SupplierProduct;
import ru.nsu.tois.entity.suppliers.SupplierProductId;
import ru.nsu.tois.mapper.SupplierMapper;
import ru.nsu.tois.repository.suppliers.SupplierProductRepository;
import ru.nsu.tois.repository.suppliers.SupplierRepository;
import ru.nsu.tois.util.PaginationUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final SupplierProductRepository supplierProductRepository;
    private final ProductResolverService productResolverService;
    private final SupplierMapper supplierMapper;

    @Transactional(readOnly = true)
    public PagedResponse<SupplierResponse> findAllSuppliers(Integer page, Integer size) {
        return PaginationUtils.toResponse(supplierRepository
                .findAll(PaginationUtils.request(page, size, "id"))
                .map(supplierMapper::toDto));
    }

    @Transactional(readOnly = true)
    public SupplierResponse findSupplierById(Integer id) {
        return supplierMapper.toDto(requireSupplier(id));
    }

    public SupplierResponse createSupplier(SupplierRequest request) {
        Supplier supplier = supplierMapper.toEntity(request);
        applyDefaults(supplier);
        return supplierMapper.toDto(supplierRepository.save(supplier));
    }

    public SupplierResponse updateSupplier(Integer id, SupplierRequest request) {
        requireSupplier(id);
        Supplier supplier = supplierMapper.toEntity(request);
        supplier.setId(id);
        applyDefaults(supplier);
        return supplierMapper.toDto(supplierRepository.save(supplier));
    }

    public void deleteSupplier(Integer id) {
        Supplier supplier = requireSupplier(id);
        supplier.setIsActive(false);
        supplierRepository.save(supplier);
    }

    @Transactional(readOnly = true)
    public PagedResponse<SupplierProductResponse> findAllSupplierProducts(Integer page, Integer size) {
        return PaginationUtils.toResponse(supplierProductRepository
                .findAll(PaginationUtils.request(page, size, "id.supplierId", "id.productId"))
                .map(supplierMapper::toDto));
    }

    @Transactional(readOnly = true)
    public SupplierProductResponse findSupplierProductById(Integer supplierId, Integer productId) {
        return supplierMapper.toDto(requireSupplierProduct(new SupplierProductId(supplierId, productId)));
    }

    public SupplierProductResponse createSupplierProduct(SupplierProductRequest request) {
        SupplierProductRequest resolvedRequest = resolveSupplierProductRequest(request);
        return supplierMapper.toDto(supplierProductRepository.save(supplierMapper.toEntity(resolvedRequest)));
    }

    public SupplierProductResponse updateSupplierProduct(SupplierProductRequest request) {
        SupplierProductRequest resolvedRequest = resolveSupplierProductRequest(request);
        SupplierProductId id = new SupplierProductId(resolvedRequest.supplierId(), resolvedRequest.productId());
        requireSupplierProduct(id);
        return supplierMapper.toDto(supplierProductRepository.save(supplierMapper.toEntity(resolvedRequest)));
    }

    public void deleteSupplierProduct(Integer supplierId, Integer productId) {
        SupplierProductId id = new SupplierProductId(supplierId, productId);
        requireSupplierProduct(id);
        supplierProductRepository.deleteById(id);
    }

    private SupplierProductRequest resolveSupplierProductRequest(SupplierProductRequest request) {
        requireSupplier(request.supplierId());
        Integer productId = productResolverService.requireOrCreateProduct(
                request.productId(),
                request.productName(),
                request.units()
        ).getId();
        return new SupplierProductRequest(
                request.supplierId(),
                productId,
                request.productName(),
                request.units(),
                request.supplierPrice()
        );
    }

    private void applyDefaults(Supplier supplier) {
        if (supplier.getIsActive() == null) {
            supplier.setIsActive(true);
        }
    }

    private Supplier requireSupplier(Integer id) {
        return supplierRepository.findById(id).orElseThrow(() -> notFound("Supplier", id));
    }

    private SupplierProduct requireSupplierProduct(SupplierProductId id) {
        return supplierProductRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SupplierProduct not found: " + id));
    }

    private EntityNotFoundException notFound(String entityName, Integer id) {
        return new EntityNotFoundException(entityName + " not found: " + id);
    }
}
