package ru.nsu.tois.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.nsu.tois.client.api.dto.request.CreateSupplyRequestWithItemsRequest;
import ru.nsu.tois.client.api.dto.request.CustomerRequest;
import ru.nsu.tois.client.api.dto.request.SellerSaleRequest;
import ru.nsu.tois.client.api.dto.response.CustomerResponse;
import ru.nsu.tois.client.api.dto.response.SaleResponse;
import ru.nsu.tois.client.api.dto.response.SaleWithItemsResponse;
import ru.nsu.tois.client.api.dto.response.SupplyRequestWithItemsResponse;
import ru.nsu.tois.client.api.dto.response.TradePointProductPriceResponse;
import ru.nsu.tois.client.api.dto.response.TradePointResponse;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;

public class SellerClient extends BaseHttpClient {
    public SellerClient(
            String baseUrl,
            HttpClient httpClient,
            ObjectMapper objectMapper
    ) {
        super(baseUrl, httpClient, objectMapper);
    }

    public List<TradePointResponse> findAssignedTradePoints(String token) {
        return get("/seller/trade-points", token, new TypeReference<>() {
        });
    }

    public CustomerResponse createCustomer(String token, String fullName, String phoneNumber) {
        return post("/seller/customers", token, new CustomerRequest(fullName, phoneNumber), CustomerResponse.class);
    }

    public List<Map<String, Object>> findStocks(String token, Integer tradePointId) {
        return get("/seller/trade-points/" + tradePointId + "/stocks", token, new TypeReference<>() {
        });
    }

    public List<TradePointProductPriceResponse> findPrices(String token, Integer tradePointId) {
        return get("/seller/trade-points/" + tradePointId + "/prices", token, new TypeReference<>() {
        });
    }

    public SaleWithItemsResponse createSale(String token, SellerSaleRequest request) {
        return post("/seller/sales", token, request, SaleWithItemsResponse.class);
    }

    public List<SaleResponse> findCurrentSellerSales(String token) {
        return get("/seller/sales", token, new TypeReference<>() {
        });
    }

    public SupplyRequestWithItemsResponse createSupplyRequest(
            String token,
            CreateSupplyRequestWithItemsRequest request
    ) {
        return post("/seller/supply-requests", token, request, SupplyRequestWithItemsResponse.class);
    }

    public List<Map<String, Object>> findOwnOutput(String token, Integer tradePointId, String from, String to) {
        return get(
                "/seller/trade-points/" + tradePointId + "/output?from=" + from + "&to=" + to,
                token,
                new TypeReference<>() {
                }
        );
    }
}
