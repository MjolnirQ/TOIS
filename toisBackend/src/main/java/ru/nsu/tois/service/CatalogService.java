package ru.nsu.tois.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.tois.dto.request.*;
import ru.nsu.tois.dto.response.*;
import ru.nsu.tois.dto.response.PagedResponse;
import ru.nsu.tois.entity.catalog.*;
import ru.nsu.tois.mapper.CatalogMapper;
import ru.nsu.tois.repository.catalog.*;
import ru.nsu.tois.repository.sales.SaleRepository;
import ru.nsu.tois.repository.tradePoints.TradePointRepository;
import ru.nsu.tois.util.PaginationUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CatalogService {
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final TradePointProductPriceRepository tradePointProductPriceRepository;
    private final TradePointProductStockRepository tradePointProductStockRepository;
    private final TradePointRepository tradePointRepository;
    private final SaleRepository saleRepository;
    private final JdbcTemplate jdbcTemplate;
    private final StockService stockService;
    private final CatalogMapper catalogMapper;

    @Transactional(readOnly = true)
    public PagedResponse<CustomerResponse> findAllCustomers(Integer page, Integer size) {
        return PaginationUtils.toResponse(customerRepository
                .findAll(PaginationUtils.request(page, size, "id"))
                .map(catalogMapper::toDto));
    }

    @Transactional(readOnly = true)
    public CustomerResponse findCustomerById(Integer id) {
        return catalogMapper.toDto(requireCustomer(id));
    }

    public CustomerResponse createCustomer(CustomerRequest request) {
        String fullName = normalizeRequired(request.fullName(), "Customer fullName must not be blank");
        String phoneNumber = normalize(request.phoneNumber());

        return customerRepository.findByFullNameAndPhoneNumber(fullName, phoneNumber)
                .map(catalogMapper::toDto)
                .orElseGet(() -> catalogMapper.toDto(customerRepository.save(catalogMapper.toEntity(
                        new CustomerRequest(fullName, phoneNumber)
                ))));
    }

    public CustomerResponse updateCustomer(Integer id, CustomerRequest request) {
        requireCustomer(id);
        Customer customer = catalogMapper.toEntity(request);
        customer.setId(id);
        return catalogMapper.toDto(customerRepository.save(customer));
    }

    public void deleteCustomer(Integer id) {
        Customer customer = requireCustomer(id);
        saleRepository.clearCustomer(id);
        customerRepository.delete(customer);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> findAllProducts(Integer page, Integer size) {
        return PaginationUtils.toResponse(productRepository
                .findAll(PaginationUtils.request(page, size, "id"))
                .map(catalogMapper::toDto));
    }

    @Transactional(readOnly = true)
    public ProductResponse findProductById(Integer id) {
        return catalogMapper.toDto(requireProduct(id));
    }

    public ProductResponse createProduct(ProductRequest request) {
        return catalogMapper.toDto(productRepository.save(catalogMapper.toEntity(request)));
    }

    public ProductResponse updateProduct(Integer id, ProductRequest request) {
        requireProduct(id);
        Product product = catalogMapper.toEntity(request);
        product.setId(id);
        return catalogMapper.toDto(productRepository.save(product));
    }

    public void deleteProduct(Integer id) {
        requireProduct(id);
        ensureProductHasNoHistory(id);
        jdbcTemplate.update("delete from supplier_product where product_id = ?", id);
        jdbcTemplate.update("delete from trade_point_product_price where product_id = ?", id);
        jdbcTemplate.update("delete from trade_point_product_stock where product_id = ?", id);
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TradePointProductPriceResponse> findAllTradePointProductPrices(Integer page, Integer size) {
        return PaginationUtils.toResponse(tradePointProductPriceRepository
                .findAll(PaginationUtils.request(page, size, "id.tradePointId", "id.productId"))
                .map(catalogMapper::toDto));
    }

    @Transactional(readOnly = true)
    public List<TradePointProductPriceResponse> findTradePointProductPricesByTradePoint(Integer tradePointId) {
        tradePointRepository.findById(tradePointId).orElseThrow(() -> notFound("TradePoint", tradePointId));
        return tradePointProductPriceRepository.findByIdTradePointId(tradePointId).stream()
                .map(catalogMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TradePointProductPriceResponse findTradePointProductPriceById(Integer tradePointId, Integer productId) {
        return catalogMapper.toDto(requireTradePointProductPrice(new TradePointProductPriceId(tradePointId, productId)));
    }

    public TradePointProductPriceResponse createTradePointProductPrice(TradePointProductPriceRequest request) {
        validateTradePointProduct(request.tradePointId(), request.productId());
        return catalogMapper.toDto(tradePointProductPriceRepository.save(catalogMapper.toEntity(request)));
    }

    public TradePointProductPriceResponse updateTradePointProductPrice(Integer tradePointId, Integer productId, BigDecimal priceValue) {
        TradePointProductPriceId id = new TradePointProductPriceId(tradePointId, productId);
        TradePointProductPrice price = requireTradePointProductPrice(id);
        price.setPrice(priceValue);
        return catalogMapper.toDto(tradePointProductPriceRepository.save(price));
    }

    public void deleteTradePointProductPrice(Integer tradePointId, Integer productId) {
        TradePointProductPriceId id = new TradePointProductPriceId(tradePointId, productId);
        requireTradePointProductPrice(id);
        tradePointProductPriceRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TradePointProductStockResponse> findAllTradePointProductStocks(Integer page, Integer size) {
        return PaginationUtils.toResponse(tradePointProductStockRepository
                .findAll(PaginationUtils.request(page, size, "id.tradePointId", "id.productId"))
                .map(catalogMapper::toDto));
    }

    @Transactional(readOnly = true)
    public List<TradePointProductStockResponse> findTradePointProductStocksByTradePoint(Integer tradePointId) {
        tradePointRepository.findById(tradePointId).orElseThrow(() -> notFound("TradePoint", tradePointId));
        return tradePointProductStockRepository.findByIdTradePointId(tradePointId).stream()
                .map(catalogMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TradePointProductStockResponse findTradePointProductStockById(Integer tradePointId, Integer productId) {
        return catalogMapper.toDto(requireTradePointProductStock(new TradePointProductStockId(tradePointId, productId)));
    }

    public TradePointProductStockResponse createTradePointProductStock(TradePointProductStockRequest request) {
        validateTradePointProduct(request.tradePointId(), request.productId());
        TradePointProductStockId id = new TradePointProductStockId(request.tradePointId(), request.productId());
        if (!stockService.create(request.tradePointId(), request.productId(), request.quantity())) {
            throw new IllegalStateException("Stock already exists for this product and trade point");
        }
        return catalogMapper.toDto(requireTradePointProductStock(id));
    }

    public TradePointProductStockResponse updateTradePointProductStock(TradePointProductStockRequest request) {
        TradePointProductStockId id = new TradePointProductStockId(request.tradePointId(), request.productId());
        TradePointProductStock stock = requireStockForUpdate(id);
        stock.setQuantity(request.quantity());
        return catalogMapper.toDto(stock);
    }

    public void deleteTradePointProductStock(Integer tradePointId, Integer productId) {
        TradePointProductStockId id = new TradePointProductStockId(tradePointId, productId);
        TradePointProductStock stock = requireStockForUpdate(id);
        if (stock.getQuantity() != 0) {
            throw new IllegalStateException("Only a zero stock balance can be deleted");
        }
        tradePointProductStockRepository.delete(stock);
    }

    private TradePointProductStock requireStockForUpdate(TradePointProductStockId id) {
        return tradePointProductStockRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new EntityNotFoundException("Trade point stock not found"));
    }

    private void validateTradePointProduct(Integer tradePointId, Integer productId) {
        tradePointRepository.findById(tradePointId).orElseThrow(() -> notFound("TradePoint", tradePointId));
        requireProduct(productId);
    }

    private Customer requireCustomer(Integer id) {
        return customerRepository.findById(id).orElseThrow(() -> notFound("Customer", id));
    }

    private Product requireProduct(Integer id) {
        return productRepository.findById(id).orElseThrow(() -> notFound("Product", id));
    }

    private void ensureProductHasNoHistory(Integer id) {
        int historyRows = count("""
                select
                    (select count(*) from sale_item where product_id = ?) +
                    (select count(*) from supply_request_item where product_id = ?) +
                    (select count(*) from supplier_order_item where product_id = ?) +
                    (select count(*) from delivery_item where product_id = ?) +
                    (select count(*) from product_transfer_item where product_id = ?)
                """, id, id, id, id, id);
        if (historyRows > 0) {
            throw new IllegalStateException("Cannot delete a role assigned to user accounts.");
        }
    }

    private int count(String sql, Object... params) {
        Number result = jdbcTemplate.queryForObject(sql, Number.class, params);
        return result == null ? 0 : result.intValue();
    }

    private TradePointProductPrice requireTradePointProductPrice(TradePointProductPriceId id) {
        return tradePointProductPriceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TradePointProductPrice not found: " + id));
    }

    private TradePointProductStock requireTradePointProductStock(TradePointProductStockId id) {
        return tradePointProductStockRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TradePointProductStock not found: " + id));
    }

    private EntityNotFoundException notFound(String entityName, Integer id) {
        return new EntityNotFoundException(entityName + " not found: " + id);
    }

    private String normalizeRequired(String value, String errorMessage) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        return normalized;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
