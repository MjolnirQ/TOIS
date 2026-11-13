package ru.nsu.tois.mapper;

import org.springframework.stereotype.Component;
import ru.nsu.tois.dto.request.*;
import ru.nsu.tois.dto.response.*;
import ru.nsu.tois.entity.catalog.*;
import ru.nsu.tois.entity.tradePoints.TradePoint;

@Component
public class CatalogMapper {
    public CustomerResponse toDto(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getFullName(), customer.getPhoneNumber());
    }

    public Customer toEntity(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setFullName(request.fullName());
        customer.setPhoneNumber(request.phoneNumber());
        return customer;
    }

    public ProductResponse toDto(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getUnits());
    }

    public Product toEntity(ProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setUnits(request.units());
        return product;
    }

    public TradePointProductPriceResponse toDto(TradePointProductPrice price) {
        return new TradePointProductPriceResponse(
                price.getId() == null ? null : price.getId().getTradePointId(),
                price.getId() == null ? null : price.getId().getProductId(),
                price.getPrice()
        );
    }

    public TradePointProductPrice toEntity(TradePointProductPriceRequest request) {
        TradePointProductPrice price = new TradePointProductPrice();
        price.setId(new TradePointProductPriceId(request.tradePointId(), request.productId()));
        price.setTradePoint(tradePointReference(request.tradePointId()));
        price.setProduct(productReference(request.productId()));
        price.setPrice(request.price());
        return price;
    }

    public TradePointProductStockResponse toDto(TradePointProductStock stock) {
        return new TradePointProductStockResponse(
                stock.getId() == null ? null : stock.getId().getTradePointId(),
                stock.getId() == null ? null : stock.getId().getProductId(),
                stock.getQuantity()
        );
    }

    public TradePointProductStock toEntity(TradePointProductStockRequest request) {
        TradePointProductStock stock = new TradePointProductStock();
        stock.setId(new TradePointProductStockId(request.tradePointId(), request.productId()));
        stock.setTradePoint(tradePointReference(request.tradePointId()));
        stock.setProduct(productReference(request.productId()));
        stock.setQuantity(request.quantity());
        return stock;
    }

    private Product productReference(Integer id) {
        Product product = new Product();
        product.setId(id);
        return product;
    }

    private TradePoint tradePointReference(Integer id) {
        TradePoint tradePoint = new TradePoint();
        tradePoint.setId(id);
        return tradePoint;
    }
}
