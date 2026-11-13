package ru.nsu.tois.client.controller.manager;

import ru.nsu.tois.client.api.ApiException;
import ru.nsu.tois.client.api.ManagerClient;
import ru.nsu.tois.client.controller.manager.ManagerAction.LookupSource;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static ru.nsu.tois.client.controller.manager.ManagerValues.toInteger;
import static ru.nsu.tois.client.controller.manager.ManagerValues.toMapList;
import static ru.nsu.tois.client.controller.manager.ManagerValues.valueToString;

final class ManagerLookupCatalog {
    private static final int PAGE_SIZE = 500;

    private final Map<LookupSource, List<LookupOption>> optionsBySource = new EnumMap<>(LookupSource.class);
    private final Map<String, List<Map<String, Object>>> rowsByPath = new HashMap<>();
    private final ManagerClient client;
    private final String token;

    ManagerLookupCatalog(ManagerClient client, String token) {
        this.client = client;
        this.token = token;
    }

    void reload() {
        optionsBySource.clear();
        rowsByPath.clear();

        loadLookup(LookupSource.PRODUCT, "/manager/products", row -> true,
                row -> row.get("name") + " (" + valueToString(row.get("units")) + ")");
        loadLookup(LookupSource.TRADE_POINT, "/manager/trade-points", row -> true,
                row -> row.get("name") + " - " + row.get("address") + " [" + row.get("type") + "]");
        loadLookup(LookupSource.CUSTOMER, "/manager/customers", row -> true,
                row -> valueToString(row.get("fullName"))
                        + (valueToString(row.get("phoneNumber")).isBlank() ? "" : ", " + row.get("phoneNumber")));
        loadLookup(LookupSource.DEPARTMENT_STORE, "/manager/trade-points",
                row -> Objects.equals(valueToString(row.get("type")), "DEPARTMENT_STORE"),
                row -> row.get("name") + " - " + row.get("address"));
        loadLookup(LookupSource.SUPPLIER, "/manager/suppliers", row -> true,
                row -> row.get("organizationName") + (Boolean.FALSE.equals(row.get("isActive")) ? " (неактивен)" : ""));
        loadRowLookup(LookupSource.SUPPLIER_PRODUCT, "/manager/supplier-products", row -> true,
                row -> lookupLabel(LookupSource.SUPPLIER, row.get("supplierId"))
                        + " / " + lookupLabel(LookupSource.PRODUCT, row.get("productId"))
                        + ", цена " + valueToString(row.get("supplierPrice")));
        loadRowLookup(LookupSource.TRADE_POINT_PRICE, "/manager/trade-point-prices", row -> true,
                row -> lookupLabel(LookupSource.TRADE_POINT, row.get("tradePointId"))
                        + " / " + lookupLabel(LookupSource.PRODUCT, row.get("productId"))
                        + ", цена " + valueToString(row.get("price")));
        loadRowLookup(LookupSource.TRADE_POINT_STOCK, "/manager/trade-point-stocks", row -> true,
                row -> lookupLabel(LookupSource.TRADE_POINT, row.get("tradePointId"))
                        + " / " + lookupLabel(LookupSource.PRODUCT, row.get("productId"))
                        + ", остаток " + valueToString(row.get("quantity")));
        loadLookup(LookupSource.EMPLOYEE, "/manager/employees", row -> true,
                row -> row.get("fullName") + " [" + row.get("position") + "]");
        loadLookup(LookupSource.SELLER, "/manager/employees",
                row -> Objects.equals(valueToString(row.get("position")), "SELLER"),
                row -> valueToString(row.get("fullName")));
        loadLookup(LookupSource.SECTION_MANAGER, "/manager/employees",
                row -> Objects.equals(valueToString(row.get("position")), "SECTION_MANAGER"),
                row -> valueToString(row.get("fullName")));
        loadLookup(LookupSource.HALL, "/manager/halls", row -> true,
                row -> lookupLabel(LookupSource.TRADE_POINT, row.get("tradePointId"))
                        + ", зал: этаж " + valueToString(row.get("floor"))
                        + ", площадь " + valueToString(row.get("area")));
        loadLookup(LookupSource.SECTION, "/manager/sections", row -> true,
                row -> "Секция: " + lookupLabel(LookupSource.DEPARTMENT_STORE, row.get("departmentStoreId")));
        loadLookup(LookupSource.SUPPLY_REQUEST, "/manager/supply-requests", row -> true,
                row -> "Заявка от " + row.get("requestDate") + ", "
                        + lookupLabel(LookupSource.TRADE_POINT, row.get("tradePointId"))
                        + ", статус " + row.get("status"));
        loadLookup(LookupSource.SUPPLY_REQUEST_ITEM, "/manager/supply-request-items", row -> true,
                row -> "Позиция заявки: " + lookupLabel(LookupSource.PRODUCT, row.get("productId"))
                        + ", количество " + valueToString(row.get("requestedQuantity")));
        loadLookup(LookupSource.SUPPLIER_ORDER, "/manager/supplier-orders", row -> true,
                row -> "Заказ " + row.get("orderNumber") + " от " + row.get("orderDate")
                        + ", " + lookupLabel(LookupSource.SUPPLIER, row.get("supplierId")));
        loadLookup(LookupSource.SUPPLIER_ORDER_ITEM, "/manager/supplier-order-items", row -> true,
                row -> "Позиция заказа: " + lookupLabel(LookupSource.PRODUCT, row.get("productId"))
                        + ", количество " + valueToString(row.get("quantity")));
        loadLookup(LookupSource.DELIVERY, "/manager/deliveries", row -> true,
                row -> "Поставка от " + row.get("deliveryDate")
                        + ", " + lookupLabel(LookupSource.SUPPLIER, row.get("supplierId")));
        loadLookup(LookupSource.DELIVERY_ITEM, "/manager/delivery-items", row -> true,
                row -> "Позиция поставки: " + lookupLabel(LookupSource.PRODUCT, row.get("productId"))
                        + ", количество " + valueToString(row.get("quantity")));
        loadLookup(LookupSource.TRANSFER, "/manager/transfers", row -> true,
                row -> "Перемещение от " + row.get("transferDate")
                        + ": " + lookupLabel(LookupSource.TRADE_POINT, row.get("senderTradePointId"))
                        + " -> " + lookupLabel(LookupSource.TRADE_POINT, row.get("receiverTradePointId")));
        loadLookup(LookupSource.TRANSFER_ITEM, "/manager/transfer-items", row -> true,
                row -> "Позиция перемещения: " + lookupLabel(LookupSource.PRODUCT, row.get("productId"))
                        + ", количество " + valueToString(row.get("quantity")));
        loadLookup(LookupSource.SALE, "/manager/sales", row -> true,
                row -> "Продажа от " + row.get("saleDate")
                        + ", " + lookupLabel(LookupSource.TRADE_POINT, row.get("tradePointId")));
        loadLookup(LookupSource.SALE_ITEM, "/manager/sale-items", row -> true,
                row -> "Позиция продажи: " + lookupLabel(LookupSource.PRODUCT, row.get("productId"))
                        + ", количество " + valueToString(row.get("quantity")));
        loadLookup(LookupSource.SELLER_ASSIGNMENT, "/manager/seller-assignments", row -> true,
                row -> lookupLabel(LookupSource.SELLER, row.get("sellerId"))
                        + " -> " + lookupLabel(LookupSource.TRADE_POINT, row.get("tradePointId")));
        loadLookup(LookupSource.SECTION_MANAGER_ASSIGNMENT, "/manager/section-manager-assignments", row -> true,
                row -> lookupLabel(LookupSource.SECTION_MANAGER, row.get("managerId"))
                        + " -> " + lookupLabel(LookupSource.SECTION, row.get("sectionId")));


        rowsByPath.clear();
    }

    List<LookupOption> options(LookupSource source) {
        return optionsBySource.getOrDefault(source, List.of());
    }

    String findLabel(LookupSource source, Integer value) {
        return options(source).stream()
                .filter(option -> Objects.equals(option.value(), value))
                .findFirst()
                .map(LookupOption::label)
                .orElse(null);
    }

    private void loadLookup(
            LookupSource source,
            String path,
            Predicate<Map<String, Object>> filter,
            Function<Map<String, Object>, String> labelBuilder
    ) {
        loadLookup(source, path, filter, labelBuilder, row -> toInteger(row.get("id")), true);
    }

    private void loadRowLookup(
            LookupSource source,
            String path,
            Predicate<Map<String, Object>> filter,
            Function<Map<String, Object>, String> labelBuilder
    ) {
        loadLookup(source, path, filter, labelBuilder, row -> null, false);
    }

    private void loadLookup(
            LookupSource source,
            String path,
            Predicate<Map<String, Object>> filter,
            Function<Map<String, Object>, String> labelBuilder,
            Function<Map<String, Object>, Integer> valueBuilder,
            boolean requireValue
    ) {
        List<LookupOption> options = rowsByPath.computeIfAbsent(path, this::fetchAvailableRows).stream()
                .filter(filter)
                .map(row -> new LookupOption(valueBuilder.apply(row), labelBuilder.apply(row), row))
                .filter(option -> !requireValue || option.value() != null)
                .toList();
        optionsBySource.put(source, options);
    }

    private List<Map<String, Object>> fetchAvailableRows(String path) {
        try {
            return fetchRows(path);
        } catch (ApiException exception) {
            if (exception.getStatusCode() == 403) {
                return List.of();
            }
            throw exception;
        }
    }

    private List<Map<String, Object>> fetchRows(String path) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int page = 0; ; page++) {
            Object response = client.execute(
                    token, "GET", path, Map.of("page", page, "size", PAGE_SIZE), Map.of()
            );
            List<Map<String, Object>> batch = toMapList(response);
            rows.addAll(batch);

            if (batch.isEmpty() || !(response instanceof Map<?, ?> result)
                    || !(result.get("totalCount") instanceof Number total)
                    || rows.size() >= total.longValue()) {
                return rows;
            }
        }
    }

    private String lookupLabel(LookupSource source, Object id) {
        Integer value = toInteger(id);
        if (value == null) {
            return "";
        }
        return Objects.requireNonNullElse(findLabel(source, value), "не найдено");
    }

    record LookupOption(
            Integer value,
            String label,
            Map<String, Object> row
    ) {
        @Override
        public String toString() {
            return label;
        }
    }
}
