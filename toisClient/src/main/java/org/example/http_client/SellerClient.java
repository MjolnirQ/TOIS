package org.example.http_client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.http_client.DTOrequest.CreateSupplyRequestWithItemsRequest;
import org.example.http_client.DTOrequest.CustomerRequest;
import org.example.http_client.DTOrequest.SellerSaleRequest;
import org.example.http_client.DTOresponse.CustomerResponse;
import org.example.http_client.DTOresponse.SaleResponse;
import org.example.http_client.DTOresponse.SaleWithItemsResponse;
import org.example.http_client.DTOresponse.SupplyRequestWithItemsResponse;
import org.example.http_client.DTOresponse.TradePointProductPriceResponse;
import org.example.http_client.DTOresponse.TradePointResponse;

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
