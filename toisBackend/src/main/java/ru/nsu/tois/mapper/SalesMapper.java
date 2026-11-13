package ru.nsu.tois.mapper;

import org.springframework.stereotype.Component;
import ru.nsu.tois.dto.request.SaleItemRequest;
import ru.nsu.tois.dto.request.SaleRequest;
import ru.nsu.tois.dto.response.SaleItemResponse;
import ru.nsu.tois.dto.response.SaleResponse;
import ru.nsu.tois.entity.catalog.Customer;
import ru.nsu.tois.entity.catalog.Product;
import ru.nsu.tois.entity.employees.Seller;
import ru.nsu.tois.entity.sales.Sale;
import ru.nsu.tois.entity.sales.SaleItem;
import ru.nsu.tois.entity.tradePoints.TradePoint;

@Component
public class SalesMapper {
    public SaleResponse toDto(Sale sale) {
        return new SaleResponse(
                sale.getId(),
                sale.getSaleDate(),
                sale.getTradePoint() == null ? null : sale.getTradePoint().getId(),
                sale.getSeller() == null ? null : sale.getSeller().getId(),
                sale.getCustomer() == null ? null : sale.getCustomer().getId()
        );
    }

    public Sale toEntity(SaleRequest request) {
        Sale sale = new Sale();
        sale.setSaleDate(request.saleDate());
        sale.setTradePoint(tradePointReference(request.tradePointId()));
        sale.setSeller(sellerReference(request.sellerId()));
        sale.setCustomer(request.customerId() == null ? null : customerReference(request.customerId()));
        return sale;
    }

    public SaleItemResponse toDto(SaleItem saleItem) {
        return new SaleItemResponse(
                saleItem.getId(),
                saleItem.getSale() == null ? null : saleItem.getSale().getId(),
                saleItem.getProduct() == null ? null : saleItem.getProduct().getId(),
                saleItem.getQuantity(),
                saleItem.getUnitPrice()
        );
    }

    public SaleItem toEntity(SaleItemRequest request) {
        SaleItem saleItem = new SaleItem();
        saleItem.setSale(saleReference(request.saleId()));
        saleItem.setProduct(productReference(request.productId()));
        saleItem.setQuantity(request.quantity());
        saleItem.setUnitPrice(request.unitPrice());
        return saleItem;
    }

    private Sale saleReference(Integer id) {
        Sale sale = new Sale();
        sale.setId(id);
        return sale;
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

    private Seller sellerReference(Integer id) {
        Seller seller = new Seller();
        seller.setId(id);
        return seller;
    }

    private Customer customerReference(Integer id) {
        Customer customer = new Customer();
        customer.setId(id);
        return customer;
    }
}
