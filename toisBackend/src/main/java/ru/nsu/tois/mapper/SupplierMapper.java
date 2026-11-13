package ru.nsu.tois.mapper;

import org.springframework.stereotype.Component;
import ru.nsu.tois.dto.request.SupplierProductRequest;
import ru.nsu.tois.dto.request.SupplierRequest;
import ru.nsu.tois.dto.response.SupplierProductResponse;
import ru.nsu.tois.dto.response.SupplierResponse;
import ru.nsu.tois.entity.catalog.Product;
import ru.nsu.tois.entity.suppliers.Supplier;
import ru.nsu.tois.entity.suppliers.SupplierProduct;
import ru.nsu.tois.entity.suppliers.SupplierProductId;

@Component
public class SupplierMapper {
    public SupplierResponse toDto(Supplier supplier) {
        return new SupplierResponse(supplier.getId(), supplier.getOrganizationName(), supplier.getIsActive());
    }

    public Supplier toEntity(SupplierRequest request) {
        Supplier supplier = new Supplier();
        supplier.setOrganizationName(request.organizationName());
        supplier.setIsActive(request.isActive());
        return supplier;
    }

    public SupplierProductResponse toDto(SupplierProduct supplierProduct) {
        return new SupplierProductResponse(
                supplierProduct.getId() == null ? null : supplierProduct.getId().getSupplierId(),
                supplierProduct.getId() == null ? null : supplierProduct.getId().getProductId(),
                supplierProduct.getSupplierPrice()
        );
    }

    public SupplierProduct toEntity(SupplierProductRequest request) {
        SupplierProduct supplierProduct = new SupplierProduct();
        supplierProduct.setId(new SupplierProductId(request.supplierId(), request.productId()));
        supplierProduct.setSupplier(supplierReference(request.supplierId()));
        supplierProduct.setProduct(productReference(request.productId()));
        supplierProduct.setSupplierPrice(request.supplierPrice());
        return supplierProduct;
    }

    private Supplier supplierReference(Integer id) {
        Supplier supplier = new Supplier();
        supplier.setId(id);
        return supplier;
    }

    private Product productReference(Integer id) {
        Product product = new Product();
        product.setId(id);
        return product;
    }
}
