package ru.nsu.tois.client.controller.manager;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import ru.nsu.tois.client.controller.manager.ManagerAction.FieldDef;
import ru.nsu.tois.client.controller.manager.ManagerAction.FieldScope;
import ru.nsu.tois.client.controller.manager.ManagerAction.FieldType;
import ru.nsu.tois.client.controller.manager.ManagerAction.ItemSpec;
import ru.nsu.tois.client.controller.manager.ManagerAction.LookupSource;

final class ManagerActionCatalog {
    private static final List<String> UNITS = List.of("kilograms", "grams", "liters", "milliliters");
    private static final List<String> TRADE_POINT_TYPES = List.of("DEPARTMENT_STORE", "SHOP", "STALL", "KIOSK");
    private static final List<String> EMPLOYEE_POSITIONS = List.of("SELLER", "SECTION_MANAGER", "TRADE_MANAGER");
    private static final List<String> SUPPLY_STATUSES = List.of("NEW", "APPROVED", "REJECTED", "ORDERED", "DELIVERED");
    static final String TODAY = "__today__";
    static final String MONTH_AGO = "__month_ago__";

    private static FieldDef pathInt(String key, String label) {
        return field(key, label, FieldType.INTEGER, true, null, List.of(), FieldScope.PATH);
    }

    private static FieldDef pathLookup(String key, String label, LookupSource lookupSource) {
        return lookupField(key, label, true, lookupSource, FieldScope.PATH);
    }

    private static FieldDef queryInt(String key, String label, boolean required) {
        return field(key, label, FieldType.INTEGER, required, null, List.of(), FieldScope.QUERY);
    }

    private static FieldDef queryLookup(String key, String label, boolean required, LookupSource lookupSource) {
        return lookupField(key, label, required, lookupSource, FieldScope.QUERY);
    }

    private static FieldDef queryText(String key, String label, boolean required) {
        return field(key, label, FieldType.TEXT, required, null, List.of(), FieldScope.QUERY);
    }

    private static FieldDef queryDate(String key, String label, boolean required, String defaultValue) {
        return field(key, label, FieldType.DATE, required, defaultValue, List.of(), FieldScope.QUERY);
    }

    private static FieldDef bodyText(String key, String label, boolean required) {
        return field(key, label, FieldType.TEXT, required, null, List.of(), FieldScope.BODY);
    }

    private static FieldDef bodyInt(String key, String label, boolean required) {
        return field(key, label, FieldType.INTEGER, required, null, List.of(), FieldScope.BODY);
    }

    private static FieldDef bodyLookup(String key, String label, boolean required, LookupSource lookupSource) {
        return lookupField(key, label, required, lookupSource, FieldScope.BODY);
    }

    private static FieldDef bodyDecimal(String key, String label, boolean required) {
        return field(key, label, FieldType.DECIMAL, required, null, List.of(), FieldScope.BODY);
    }

    private static FieldDef bodyDate(String key, String label, boolean required, String defaultValue) {
        return field(key, label, FieldType.DATE, required, defaultValue, List.of(), FieldScope.BODY);
    }

    private static FieldDef bodyBool(String key, String label, boolean required, String defaultValue) {
        return field(key, label, FieldType.BOOLEAN, required, defaultValue, List.of(), FieldScope.BODY);
    }

    private static FieldDef bodyEnum(String key, String label, boolean required, List<String> options) {
        return field(key, label, FieldType.ENUM, required, options.isEmpty() ? null : options.getFirst(), options, FieldScope.BODY);
    }

    private static FieldDef queryEnum(String key, String label, boolean required, List<String> options) {
        return field(key, label, FieldType.ENUM, required, null, options, FieldScope.QUERY);
    }

    private static FieldDef pathAndBodyInt(String key, String label) {
        return field(key, label, FieldType.INTEGER, true, null, List.of(), FieldScope.PATH, FieldScope.BODY);
    }

    private static FieldDef pathAndBodyLookup(String key, String label, LookupSource lookupSource) {
        return lookupField(key, label, true, lookupSource, FieldScope.PATH, FieldScope.BODY);
    }

    private static FieldDef field(
            String key,
            String label,
            FieldType type,
            boolean required,
            String defaultValue,
            List<String> options,
            FieldScope... scopes
    ) {
        return new FieldDef(key, label, type, EnumSet.copyOf(List.of(scopes)), required, defaultValue, options, null);
    }

    private static FieldDef lookupField(
            String key,
            String label,
            boolean required,
            LookupSource lookupSource,
            FieldScope... scopes
    ) {
        return new FieldDef(key, label, FieldType.INTEGER, EnumSet.copyOf(List.of(scopes)), required, null, List.of(), lookupSource);
    }

    static List<ManagerAction> actions() {
        List<ManagerAction> list = new ArrayList<>();
        addCatalogActions(list);
        addSalesActions(list);
        addSupplierActions(list);
        addSupplyActions(list);
        addTransferActions(list);
        addTradePointActions(list);
        addEmployeeActions(list);
        addReportActions(list);
        return List.copyOf(list);
    }

    private static void addCatalogActions(List<ManagerAction> list) {
        String group = "Товары и цены";
        list.add(pagedAction(group, "Получить все товары", "/manager/products"));
        list.add(action(group, "Получить товар", "GET", "/manager/products/{id}", pathLookup("id", "Товар", LookupSource.PRODUCT)));
        list.add(action(group, "Создать товар", "POST", "/manager/products",
                bodyText("name", "Название", true),
                bodyEnum("units", "Единицы", false, UNITS)));
        list.add(action(group, "Обновить товар", "PUT", "/manager/products/{id}",
                pathLookup("id", "Товар", LookupSource.PRODUCT),
                bodyText("name", "Название", true),
                bodyEnum("units", "Единицы", false, UNITS)));
        list.add(action(group, "Удалить товар", "DELETE", "/manager/products/{id}", pathLookup("id", "Товар", LookupSource.PRODUCT)));
        list.add(pagedAction(group, "Получить всех покупателей", "/manager/customers"));
        list.add(action(group, "Получить покупателя", "GET", "/manager/customers/{id}", pathLookup("id", "Покупатель", LookupSource.CUSTOMER)));
        list.add(action(group, "Создать покупателя", "POST", "/manager/customers",
                bodyText("fullName", "ФИО", true),
                bodyText("phoneNumber", "Телефон", false)));
        list.add(action(group, "Обновить покупателя", "PUT", "/manager/customers/{id}",
                pathLookup("id", "Покупатель", LookupSource.CUSTOMER),
                bodyText("fullName", "ФИО", true),
                bodyText("phoneNumber", "Телефон", false)));
        list.add(action(group, "Удалить покупателя", "DELETE", "/manager/customers/{id}", pathLookup("id", "Покупатель", LookupSource.CUSTOMER)));
        list.add(pagedAction(group, "Получить все цены", "/manager/trade-point-prices"));
        list.add(action(group, "Остатки торговой точки", "GET", "/manager/trade-points/{tradePointId}/stocks",
                pathLookup("tradePointId", "Торговая точка", LookupSource.TRADE_POINT)));
        list.add(pagedAction(group, "Получить все остатки", "/manager/trade-point-stocks"));
        list.add(action(group, "Цены торговой точки", "GET", "/manager/trade-points/{tradePointId}/prices",
                pathLookup("tradePointId", "Торговая точка", LookupSource.TRADE_POINT)));
        list.add(action(group, "Цена товара в торговой точке", "GET", "/manager/trade-points/{tradePointId}/products/{productId}/price",
                pathLookup("tradePointId", "Торговая точка", LookupSource.TRADE_POINT),
                pathLookup("productId", "Товар", LookupSource.PRODUCT)));
        list.add(action(group, "Создать цену товара", "POST", "/manager/trade-point-prices",
                bodyLookup("tradePointId", "Торговая точка", true, LookupSource.TRADE_POINT),
                bodyLookup("productId", "Товар", true, LookupSource.PRODUCT),
                bodyDecimal("price", "Цена", true)));
        list.add(action(group, "Обновить цену товара", "PATCH", "/manager/trade-points/{tradePointId}/products/{productId}/price",
                pathLookup("tradePointId", "Торговая точка", LookupSource.TRADE_POINT),
                pathLookup("productId", "Товар", LookupSource.PRODUCT),
                bodyDecimal("price", "Цена", true)));
        list.add(action(group, "Удалить цену товара", "DELETE", "/manager/trade-points/{tradePointId}/products/{productId}/price",
                pathLookup("tradePointId", "Торговая точка", LookupSource.TRADE_POINT),
                pathLookup("productId", "Товар", LookupSource.PRODUCT)));
        list.add(action(group, "Создать остаток товара", "POST", "/manager/trade-point-stocks",
                bodyLookup("tradePointId", "Торговая точка", true, LookupSource.TRADE_POINT),
                bodyLookup("productId", "Товар", true, LookupSource.PRODUCT),
                bodyInt("quantity", "Количество", true)));
        list.add(action(group, "Обновить остаток товара", "PUT", "/manager/trade-points/{tradePointId}/products/{productId}/stock",
                pathLookup("tradePointId", "Торговая точка", LookupSource.TRADE_POINT),
                pathLookup("productId", "Товар", LookupSource.PRODUCT),
                bodyInt("quantity", "Количество", true)));
        list.add(action(group, "Удалить остаток товара", "DELETE", "/manager/trade-points/{tradePointId}/products/{productId}/stock",
                pathLookup("tradePointId", "Торговая точка", LookupSource.TRADE_POINT),
                pathLookup("productId", "Товар", LookupSource.PRODUCT)));
    }

    private static void addSalesActions(List<ManagerAction> list) {
        String group = "Продажи и покупатели";
        list.add(pagedAction(group, "Получить все продажи", "/manager/sales"));
        list.add(action(group, "Получить продажу", "GET", "/manager/sales/{id}", pathLookup("id", "Продажа", LookupSource.SALE)));
        list.add(action(group, "Создать продажу", "POST", "/manager/sales",
                bodyDate("saleDate", "Дата продажи", true, TODAY),
                bodyLookup("tradePointId", "Торговая точка", true, LookupSource.TRADE_POINT),
                bodyLookup("sellerId", "Продавец", true, LookupSource.SELLER),
                bodyLookup("customerId", "Покупатель", false, LookupSource.CUSTOMER)));
        list.add(action(group, "Обновить продажу", "PUT", "/manager/sales/{id}",
                pathLookup("id", "Продажа", LookupSource.SALE),
                bodyDate("saleDate", "Дата продажи", true, TODAY),
                bodyLookup("tradePointId", "Торговая точка", true, LookupSource.TRADE_POINT),
                bodyLookup("sellerId", "Продавец", true, LookupSource.SELLER),
                bodyLookup("customerId", "Покупатель", false, LookupSource.CUSTOMER)));
        list.add(action(group, "Удалить продажу", "DELETE", "/manager/sales/{id}", pathLookup("id", "Продажа", LookupSource.SALE)));
        list.add(pagedAction(group, "Получить все позиции продаж", "/manager/sale-items"));
        list.add(action(group, "Получить позицию продажи", "GET", "/manager/sale-items/{id}", pathLookup("id", "Позиция продажи", LookupSource.SALE_ITEM)));
        list.add(action(group, "Создать позицию продажи", "POST", "/manager/sale-items",
                bodyLookup("saleId", "Продажа", true, LookupSource.SALE),
                bodyLookup("productId", "Товар", true, LookupSource.PRODUCT),
                bodyInt("quantity", "Количество", true),
                bodyDecimal("unitPrice", "Цена", true)));
        list.add(action(group, "Обновить позицию продажи", "PUT", "/manager/sale-items/{id}",
                pathLookup("id", "Позиция продажи", LookupSource.SALE_ITEM),
                bodyLookup("saleId", "Продажа", true, LookupSource.SALE),
                bodyLookup("productId", "Товар", true, LookupSource.PRODUCT),
                bodyInt("quantity", "Количество", true),
                bodyDecimal("unitPrice", "Цена", true)));
        list.add(action(group, "Удалить позицию продажи", "DELETE", "/manager/sale-items/{id}", pathLookup("id", "Позиция продажи", LookupSource.SALE_ITEM)));
    }

    private static void addSupplierActions(List<ManagerAction> list) {
        String group = "Поставщики";
        list.add(pagedAction(group, "Получить всех поставщиков", "/manager/suppliers"));
        list.add(action(group, "Получить поставщика", "GET", "/manager/suppliers/{id}", pathLookup("id", "Поставщик", LookupSource.SUPPLIER)));
        list.add(action(group, "Создать поставщика", "POST", "/manager/suppliers",
                bodyText("organizationName", "Организация", true),
                bodyBool("isActive", "Активен", false, "true")));
        list.add(action(group, "Обновить поставщика", "PUT", "/manager/suppliers/{id}",
                pathLookup("id", "Поставщик", LookupSource.SUPPLIER),
                bodyText("organizationName", "Организация", true),
                bodyBool("isActive", "Активен", false, "true")));
        list.add(action(group, "Удалить поставщика", "DELETE", "/manager/suppliers/{id}", pathLookup("id", "Поставщик", LookupSource.SUPPLIER)));
        list.add(pagedAction(group, "Получить товары поставщиков", "/manager/supplier-products"));
        list.add(action(group, "Получить товар поставщика", "GET", "/manager/suppliers/{supplierId}/products/{productId}",
                pathLookup("supplierId", "Поставщик", LookupSource.SUPPLIER),
                pathLookup("productId", "Товар", LookupSource.PRODUCT)));
        list.add(action(group, "Добавить товар поставщика", "POST", "/manager/supplier-products",
                bodyLookup("supplierId", "Поставщик", true, LookupSource.SUPPLIER),
                bodyLookup("productId", "Существующий товар", false, LookupSource.PRODUCT),
                bodyText("productName", "Название нового товара", false),
                bodyEnum("units", "Единицы нового товара", false, UNITS),
                bodyDecimal("supplierPrice", "Цена поставщика", true)));
        list.add(action(group, "Обновить товар поставщика", "PUT", "/manager/suppliers/{supplierId}/products/{productId}",
                pathAndBodyLookup("supplierId", "Поставщик", LookupSource.SUPPLIER),
                pathLookup("productId", "Товар", LookupSource.PRODUCT),
                bodyDecimal("supplierPrice", "Цена поставщика", true)));
        list.add(action(group, "Удалить товар поставщика", "DELETE", "/manager/suppliers/{supplierId}/products/{productId}",
                pathLookup("supplierId", "Поставщик", LookupSource.SUPPLIER),
                pathLookup("productId", "Товар", LookupSource.PRODUCT)));
    }

    private static void addSupplyActions(List<ManagerAction> list) {
        String group = "Заявки, заказы, поставки";
        list.add(pagedAction(group, "Получить все заявки на поставку", "/manager/supply-requests"));
        list.add(action(group, "Получить заявку на поставку", "GET", "/manager/supply-requests/{id}",
                pathLookup("id", "Заявка", LookupSource.SUPPLY_REQUEST)));
        list.add(actionWithItems(group, "Создать заявку на поставку", "POST", "/manager/supply-requests",
                List.of(
                        bodyDate("requestDate", "Дата заявки", true, TODAY),
                        bodyLookup("tradePointId", "Торговая точка", true, LookupSource.TRADE_POINT),
                        bodyText("status", "Статус", false)
                ),
                supplyItems()));
        list.add(action(group, "Изменить статус заявки", "PATCH", "/manager/supply-requests/{id}/status",
                pathLookup("id", "Заявка", LookupSource.SUPPLY_REQUEST),
                bodyText("status", "Статус", true)));
        list.add(action(group, "Обновить заявку", "PUT", "/manager/supply-requests/{id}",
                pathLookup("id", "Заявка", LookupSource.SUPPLY_REQUEST),
                bodyDate("requestDate", "Дата заявки", true, TODAY),
                bodyLookup("tradePointId", "Торговая точка", true, LookupSource.TRADE_POINT),
                bodyText("status", "Статус", true)));
        list.add(action(group, "Удалить заявку", "DELETE", "/manager/supply-requests/{id}",
                pathLookup("id", "Заявка", LookupSource.SUPPLY_REQUEST)));
        list.add(pagedAction(group, "Получить позиции заявок", "/manager/supply-request-items"));
        list.add(action(group, "Создать позицию заявки", "POST", "/manager/supply-request-items",
                bodyLookup("supplyRequestId", "Заявка", true, LookupSource.SUPPLY_REQUEST),
                bodyLookup("productId", "Товар", true, LookupSource.PRODUCT),
                bodyInt("requestedQuantity", "Количество", true)));
        list.add(action(group, "Обновить позицию заявки", "PUT", "/manager/supply-request-items/{id}",
                pathLookup("id", "Позиция заявки", LookupSource.SUPPLY_REQUEST_ITEM),
                bodyLookup("supplyRequestId", "Заявка", true, LookupSource.SUPPLY_REQUEST),
                bodyLookup("productId", "Товар", true, LookupSource.PRODUCT),
                bodyInt("requestedQuantity", "Количество", true)));
        list.add(action(group, "Удалить позицию заявки", "DELETE", "/manager/supply-request-items/{id}",
                pathLookup("id", "Позиция заявки", LookupSource.SUPPLY_REQUEST_ITEM)));
        list.add(pagedAction(group, "Получить все заказы поставщикам", "/manager/supplier-orders"));
        list.add(action(group, "Получить заказ поставщику", "GET", "/manager/supplier-orders/{id}",
                pathLookup("id", "Заказ поставщику", LookupSource.SUPPLIER_ORDER)));
        list.add(actionWithItems(group, "Создать заказ по заявке", "POST", "/manager/supply-requests/{supplyRequestId}/supplier-orders",
                List.of(
                        pathLookup("supplyRequestId", "Заявка", LookupSource.SUPPLY_REQUEST),
                        bodyDate("orderDate", "Дата заказа", true, TODAY),
                        bodyText("orderNumber", "Номер заказа", true),
                        bodyLookup("supplierId", "Поставщик", true, LookupSource.SUPPLIER),
                        bodyText("status", "Статус", true)
                ),
                supplierOrderItems()));
        list.add(action(group, "Обновить заказ поставщику", "PUT", "/manager/supplier-orders/{id}",
                pathLookup("id", "Заказ поставщику", LookupSource.SUPPLIER_ORDER),
                bodyDate("orderDate", "Дата заказа", true, TODAY),
                bodyText("orderNumber", "Номер заказа", true),
                bodyLookup("managerId", "Менеджер торговли", true, LookupSource.EMPLOYEE),
                bodyLookup("supplierId", "Поставщик", true, LookupSource.SUPPLIER),
                bodyLookup("supplyRequestId", "Заявка", true, LookupSource.SUPPLY_REQUEST),
                bodyText("status", "Статус", true)));
        list.add(action(group, "Удалить заказ поставщику", "DELETE", "/manager/supplier-orders/{id}",
                pathLookup("id", "Заказ поставщику", LookupSource.SUPPLIER_ORDER)));
        list.add(pagedAction(group, "Получить позиции заказов", "/manager/supplier-order-items"));
        list.add(action(group, "Создать позицию заказа", "POST", "/manager/supplier-order-items",
                bodyLookup("supplierOrderId", "Заказ поставщику", true, LookupSource.SUPPLIER_ORDER),
                bodyLookup("productId", "Товар", true, LookupSource.PRODUCT),
                bodyInt("quantity", "Количество", true),
                bodyDecimal("purchasePrice", "Закупочная цена", true)));
        list.add(action(group, "Обновить позицию заказа", "PUT", "/manager/supplier-order-items/{id}",
                pathLookup("id", "Позиция заказа", LookupSource.SUPPLIER_ORDER_ITEM),
                bodyLookup("supplierOrderId", "Заказ поставщику", true, LookupSource.SUPPLIER_ORDER),
                bodyLookup("productId", "Товар", true, LookupSource.PRODUCT),
                bodyInt("quantity", "Количество", true),
                bodyDecimal("purchasePrice", "Закупочная цена", true)));
        list.add(action(group, "Удалить позицию заказа", "DELETE", "/manager/supplier-order-items/{id}",
                pathLookup("id", "Позиция заказа", LookupSource.SUPPLIER_ORDER_ITEM)));
        list.add(pagedAction(group, "Получить все поставки", "/manager/deliveries"));
        list.add(action(group, "Получить поставку", "GET", "/manager/deliveries/{id}",
                pathLookup("id", "Поставка", LookupSource.DELIVERY)));
        list.add(actionWithItems(group, "Создать поставку по заказу", "POST", "/manager/supplier-orders/{supplierOrderId}/deliveries",
                List.of(
                        pathLookup("supplierOrderId", "Заказ поставщику", LookupSource.SUPPLIER_ORDER),
                        bodyDate("deliveryDate", "Дата поставки", true, TODAY)
                ),
                deliveryItems()));
        list.add(action(group, "Обновить поставку", "PUT", "/manager/deliveries/{id}",
                pathLookup("id", "Поставка", LookupSource.DELIVERY),
                bodyLookup("supplierId", "Поставщик", true, LookupSource.SUPPLIER),
                bodyLookup("supplierOrderId", "Заказ поставщику", true, LookupSource.SUPPLIER_ORDER),
                bodyDate("deliveryDate", "Дата поставки", true, TODAY)));
        list.add(action(group, "Удалить поставку", "DELETE", "/manager/deliveries/{id}",
                pathLookup("id", "Поставка", LookupSource.DELIVERY)));
        list.add(pagedAction(group, "Получить позиции поставок", "/manager/delivery-items"));
        list.add(action(group, "Создать позицию поставки", "POST", "/manager/delivery-items",
                bodyLookup("deliveryId", "Поставка", true, LookupSource.DELIVERY),
                bodyLookup("productId", "Товар", true, LookupSource.PRODUCT),
                bodyInt("quantity", "Количество", true),
                bodyDecimal("deliveryPrice", "Цена поставки", true)));
        list.add(action(group, "Обновить позицию поставки", "PUT", "/manager/delivery-items/{id}",
                pathLookup("id", "Позиция поставки", LookupSource.DELIVERY_ITEM),
                bodyLookup("deliveryId", "Поставка", true, LookupSource.DELIVERY),
                bodyLookup("productId", "Товар", true, LookupSource.PRODUCT),
                bodyInt("quantity", "Количество", true),
                bodyDecimal("deliveryPrice", "Цена поставки", true)));
        list.add(action(group, "Удалить позицию поставки", "DELETE", "/manager/delivery-items/{id}",
                pathLookup("id", "Позиция поставки", LookupSource.DELIVERY_ITEM)));
    }

    private static void addTransferActions(List<ManagerAction> list) {
        String group = "Перемещения";
        list.add(pagedAction(group, "Получить все перемещения", "/manager/transfers"));
        list.add(action(group, "Получить перемещение", "GET", "/manager/transfers/{id}",
                pathLookup("id", "Перемещение", LookupSource.TRANSFER)));
        list.add(actionWithItems(group, "Создать перемещение", "POST", "/manager/transfers",
                List.of(
                        bodyDate("transferDate", "Дата перемещения", true, TODAY),
                        bodyLookup("senderTradePointId", "Точка отправитель", true, LookupSource.TRADE_POINT),
                        bodyLookup("receiverTradePointId", "Точка получатель", true, LookupSource.TRADE_POINT)
                ),
                transferItems()));
        list.add(action(group, "Обновить перемещение", "PUT", "/manager/transfers/{id}",
                pathLookup("id", "Перемещение", LookupSource.TRANSFER),
                bodyDate("transferDate", "Дата перемещения", true, TODAY),
                bodyLookup("senderTradePointId", "Точка отправитель", true, LookupSource.TRADE_POINT),
                bodyLookup("receiverTradePointId", "Точка получатель", true, LookupSource.TRADE_POINT)));
        list.add(action(group, "Удалить перемещение", "DELETE", "/manager/transfers/{id}", pathLookup("id", "Перемещение", LookupSource.TRANSFER)));
        list.add(pagedAction(group, "Получить позиции перемещений", "/manager/transfer-items"));
        list.add(action(group, "Создать позицию перемещения", "POST", "/manager/transfer-items",
                bodyLookup("productTransferId", "Перемещение", true, LookupSource.TRANSFER),
                bodyLookup("productId", "Товар", true, LookupSource.PRODUCT),
                bodyInt("quantity", "Количество", true)));
        list.add(action(group, "Обновить позицию перемещения", "PUT", "/manager/transfer-items/{id}",
                pathLookup("id", "Позиция перемещения", LookupSource.TRANSFER_ITEM),
                bodyLookup("productTransferId", "Перемещение", true, LookupSource.TRANSFER),
                bodyLookup("productId", "Товар", true, LookupSource.PRODUCT),
                bodyInt("quantity", "Количество", true)));
        list.add(action(group, "Удалить позицию перемещения", "DELETE", "/manager/transfer-items/{id}",
                pathLookup("id", "Позиция перемещения", LookupSource.TRANSFER_ITEM)));
    }

    private static void addTradePointActions(List<ManagerAction> list) {
        String group = "Торговые точки";
        list.add(pagedAction(group, "Получить все торговые точки", "/manager/trade-points"));
        list.add(action(group, "Получить торговую точку", "GET", "/manager/trade-points/{id}", pathLookup("id", "Торговая точка", LookupSource.TRADE_POINT)));
        list.add(action(group, "Создать торговую точку", "POST", "/manager/trade-points",
                tradePointFields().toArray(FieldDef[]::new)));
        List<FieldDef> updateTradePointFields = new ArrayList<>();
        updateTradePointFields.add(pathLookup("id", "Торговая точка", LookupSource.TRADE_POINT));
        updateTradePointFields.addAll(tradePointFields());
        list.add(action(group, "Обновить торговую точку", "PUT", "/manager/trade-points/{id}",
                updateTradePointFields.toArray(FieldDef[]::new)));
        list.add(action(group, "Удалить торговую точку", "DELETE", "/manager/trade-points/{id}", pathLookup("id", "Торговая точка", LookupSource.TRADE_POINT)));
        list.add(pagedAction(group, "Получить все залы", "/manager/halls"));
        list.add(action(group, "Получить зал", "GET", "/manager/halls/{id}", pathLookup("id", "Зал", LookupSource.HALL)));
        list.add(action(group, "Создать зал", "POST", "/manager/halls",
                bodyLookup("tradePointId", "Торговая точка", true, LookupSource.TRADE_POINT),
                bodyInt("floor", "Этаж", false),
                bodyDecimal("area", "Площадь", false)));
        list.add(action(group, "Обновить зал", "PUT", "/manager/halls/{id}",
                pathLookup("id", "Зал", LookupSource.HALL),
                bodyLookup("tradePointId", "Торговая точка", true, LookupSource.TRADE_POINT),
                bodyInt("floor", "Этаж", false),
                bodyDecimal("area", "Площадь", false)));
        list.add(action(group, "Удалить зал", "DELETE", "/manager/halls/{id}", pathLookup("id", "Зал", LookupSource.HALL)));
        list.add(pagedAction(group, "Получить все секции", "/manager/sections"));
        list.add(action(group, "Получить секцию", "GET", "/manager/sections/{id}", pathLookup("id", "Секция", LookupSource.SECTION)));
        list.add(action(group, "Создать секцию", "POST", "/manager/sections",
                bodyLookup("departmentStoreId", "Универмаг", true, LookupSource.DEPARTMENT_STORE)));
        list.add(action(group, "Обновить секцию", "PUT", "/manager/sections/{id}",
                pathLookup("id", "Секция", LookupSource.SECTION),
                bodyLookup("departmentStoreId", "Универмаг", true, LookupSource.DEPARTMENT_STORE)));
        list.add(action(group, "Удалить секцию", "DELETE", "/manager/sections/{id}", pathLookup("id", "Секция", LookupSource.SECTION)));
    }

    private static void addEmployeeActions(List<ManagerAction> list) {
        String group = "Сотрудники и назначения";
        list.add(pagedAction(group, "Получить всех сотрудников", "/manager/employees"));
        list.add(action(group, "Получить сотрудника", "GET", "/manager/employees/{id}", pathLookup("id", "Сотрудник", LookupSource.EMPLOYEE)));
        list.add(action(group, "Создать сотрудника", "POST", "/manager/employees",
                bodyText("fullName", "ФИО", true),
                bodyEnum("position", "Должность", true, EMPLOYEE_POSITIONS),
                bodyInt("salary", "Зарплата", true)));
        list.add(action(group, "Обновить сотрудника", "PUT", "/manager/employees/{id}",
                pathLookup("id", "Сотрудник", LookupSource.EMPLOYEE),
                bodyText("fullName", "ФИО", true),
                bodyEnum("position", "Должность", true, EMPLOYEE_POSITIONS),
                bodyInt("salary", "Зарплата", true)));
        list.add(action(group, "Удалить сотрудника", "DELETE", "/manager/employees/{id}", pathLookup("id", "Сотрудник", LookupSource.EMPLOYEE)));
        list.add(pagedAction(group, "Получить назначения продавцов", "/manager/seller-assignments"));
        list.add(action(group, "Получить назначение продавца", "GET", "/manager/seller-assignments/{id}",
                pathLookup("id", "Назначение продавца", LookupSource.SELLER_ASSIGNMENT)));
        list.add(action(group, "Назначить продавца", "POST", "/manager/seller-assignments",
                bodyLookup("sellerId", "Продавец", true, LookupSource.SELLER),
                bodyLookup("tradePointId", "Торговая точка", true, LookupSource.TRADE_POINT),
                bodyLookup("hallId", "Зал", false, LookupSource.HALL)));
        list.add(action(group, "Обновить назначение продавца", "PUT", "/manager/seller-assignments/{id}",
                pathLookup("id", "Назначение продавца", LookupSource.SELLER_ASSIGNMENT),
                bodyLookup("sellerId", "Продавец", true, LookupSource.SELLER),
                bodyLookup("tradePointId", "Торговая точка", true, LookupSource.TRADE_POINT),
                bodyLookup("hallId", "Зал", false, LookupSource.HALL)));
        list.add(action(group, "Удалить назначение продавца", "DELETE", "/manager/seller-assignments/{id}", pathLookup("id", "Назначение продавца", LookupSource.SELLER_ASSIGNMENT)));
        list.add(pagedAction(group, "Получить назначения менеджеров секций", "/manager/section-manager-assignments"));
        list.add(action(group, "Получить назначение менеджера секции", "GET", "/manager/section-manager-assignments/{id}",
                pathLookup("id", "Назначение менеджера секции", LookupSource.SECTION_MANAGER_ASSIGNMENT)));
        list.add(action(group, "Назначить менеджера секции", "POST", "/manager/section-manager-assignments",
                bodyLookup("managerId", "Менеджер секции", true, LookupSource.SECTION_MANAGER),
                bodyLookup("tradePointId", "Торговая точка", true, LookupSource.TRADE_POINT),
                bodyLookup("sectionId", "Секция", true, LookupSource.SECTION)));
        list.add(action(group, "Обновить назначение менеджера секции", "PUT", "/manager/section-manager-assignments/{id}",
                pathLookup("id", "Назначение менеджера секции", LookupSource.SECTION_MANAGER_ASSIGNMENT),
                bodyLookup("managerId", "Менеджер секции", true, LookupSource.SECTION_MANAGER),
                bodyLookup("tradePointId", "Торговая точка", true, LookupSource.TRADE_POINT),
                bodyLookup("sectionId", "Секция", true, LookupSource.SECTION)));
        list.add(action(group, "Удалить назначение менеджера секции", "DELETE", "/manager/section-manager-assignments/{id}", pathLookup("id", "Назначение менеджера секции", LookupSource.SECTION_MANAGER_ASSIGNMENT)));
    }

    private static void addReportActions(List<ManagerAction> list) {
        String group = "Отчёты";
        list.add(action(group, "1. Поставщики товара", "GET", "/manager/reports/suppliers-for-product",
                queryLookup("productId", "Товар", true, LookupSource.PRODUCT)));
        list.add(action(group, "2. Поставщики поставившие товар", "GET", "/manager/reports/suppliers-delivered-product",
                queryLookup("productId", "Товар", true, LookupSource.PRODUCT),
                queryInt("minQuantity", "Минимальное количество", true),
                queryDate("from", "Дата с", false, null),
                queryDate("to", "Дата по", false, null)));
        list.add(action(group, "3. Покупатели товара за период", "GET", "/manager/reports/customers-bought-product",
                queryLookup("productId", "Товар", true, LookupSource.PRODUCT),
                queryDate("from", "Дата с", true, MONTH_AGO),
                queryDate("to", "Дата по", true, TODAY)));
        list.add(action(group, "4. Покупатели товара не менее количества", "GET", "/manager/reports/customers-bought-product-at-least",
                queryLookup("productId", "Товар", true, LookupSource.PRODUCT),
                queryInt("minQuantity", "Минимальное количество", true)));
        list.add(action(group, "5. Номенклатура и объём торговой точки", "GET", "/manager/trade-points/{tradePointId}/stocks",
                pathLookup("tradePointId", "Торговая точка", LookupSource.TRADE_POINT)));
        list.add(action(group, "6. Объём и цены товара по всем точкам", "GET", "/manager/reports/stock-prices",
                queryLookup("productId", "Товар", true, LookupSource.PRODUCT)));
        list.add(action(group, "7. Объём и цены товара по типу точки", "GET", "/manager/reports/stock-prices",
                queryLookup("productId", "Товар", true, LookupSource.PRODUCT),
                queryEnum("tradePointType", "Тип торговой точки", true, TRADE_POINT_TYPES)));
        list.add(action(group, "8. Объём и цены товара по конкретной точке", "GET", "/manager/reports/stock-prices",
                queryLookup("productId", "Товар", true, LookupSource.PRODUCT),
                queryLookup("tradePointId", "Торговая точка", true, LookupSource.TRADE_POINT)));
        list.add(action(group, "8. Выработка на продавца", "GET", "/manager/reports/sales-per-seller",
                queryDate("from", "Дата с", true, MONTH_AGO),
                queryDate("to", "Дата по", true, TODAY),
                queryEnum("tradePointType", "Тип торговой точки", false, TRADE_POINT_TYPES)));
        list.add(action(group, "9. Выработка продавца по точке", "GET", "/manager/reports/seller-output",
                queryLookup("sellerId", "Продавец", true, LookupSource.SELLER),
                queryLookup("tradePointId", "Торговая точка", true, LookupSource.TRADE_POINT),
                queryDate("from", "Дата с", true, MONTH_AGO),
                queryDate("to", "Дата по", true, TODAY)));
        list.add(action(group, "10. Зарплаты продавцов", "GET", "/manager/reports/seller-salaries",
                queryEnum("tradePointType", "Тип торговой точки", false, TRADE_POINT_TYPES),
                queryLookup("tradePointId", "Торговая точка", false, LookupSource.TRADE_POINT)));
        list.add(action(group, "11. Поставки товара поставщиком", "GET", "/manager/reports/supplier-deliveries-for-product",
                queryLookup("supplierId", "Поставщик", true, LookupSource.SUPPLIER),
                queryLookup("productId", "Товар", true, LookupSource.PRODUCT),
                queryDate("from", "Дата с", false, null),
                queryDate("to", "Дата по", false, null)));
        list.add(action(group, "12. Продажи к площади", "GET", "/manager/reports/sales-to-area",
                queryEnum("tradePointType", "Тип торговой точки", false, TRADE_POINT_TYPES),
                queryDate("from", "Дата с", true, MONTH_AGO),
                queryDate("to", "Дата по", true, TODAY)));
        list.add(action(group, "12. Продажи к числу залов", "GET", "/manager/reports/sales-to-halls",
                queryEnum("tradePointType", "Тип торговой точки", false, TRADE_POINT_TYPES),
                queryDate("from", "Дата с", true, MONTH_AGO),
                queryDate("to", "Дата по", true, TODAY)));
        list.add(action(group, "12. Продажи к числу прилавков", "GET", "/manager/reports/sales-to-counters",
                queryEnum("tradePointType", "Тип торговой точки", false, TRADE_POINT_TYPES),
                queryDate("from", "Дата с", true, MONTH_AGO),
                queryDate("to", "Дата по", true, TODAY)));
        list.add(action(group, "13. Рентабельность торговой точки", "GET", "/manager/reports/profitability",
                queryLookup("tradePointId", "Торговая точка", true, LookupSource.TRADE_POINT),
                queryDate("from", "Дата с", true, MONTH_AGO),
                queryDate("to", "Дата по", true, TODAY)));
        list.add(action(group, "14. Поставки по номеру заказа", "GET", "/manager/reports/deliveries-by-order-number",
                queryText("orderNumber", "Номер заказа", true)));
        list.add(action(group, "15. Покупатели товара", "GET", "/manager/reports/product-customers",
                queryLookup("productId", "Товар", true, LookupSource.PRODUCT),
                queryDate("from", "Дата с", false, null),
                queryDate("to", "Дата по", false, null),
                queryEnum("tradePointType", "Тип торговой точки", false, TRADE_POINT_TYPES),
                queryLookup("tradePointId", "Торговая точка", false, LookupSource.TRADE_POINT)));
        list.add(action(group, "16. Активные покупатели", "GET", "/manager/reports/active-customers",
                queryDate("from", "Дата с", false, null),
                queryDate("to", "Дата по", false, null),
                queryEnum("tradePointType", "Тип торговой точки", false, TRADE_POINT_TYPES),
                queryLookup("tradePointId", "Торговая точка", false, LookupSource.TRADE_POINT)));
        list.add(action(group, "17. Товарооборот", "GET", "/manager/reports/turnover",
                queryDate("from", "Дата с", true, MONTH_AGO),
                queryDate("to", "Дата по", true, TODAY),
                queryEnum("tradePointType", "Тип торговой точки", false, TRADE_POINT_TYPES),
                queryLookup("tradePointId", "Торговая точка", false, LookupSource.TRADE_POINT)));
        list.add(action(group, "Объём продаж товара", "GET", "/manager/reports/product-sales-volume",
                queryLookup("productId", "Товар", true, LookupSource.PRODUCT),
                queryDate("from", "Дата с", true, MONTH_AGO),
                queryDate("to", "Дата по", true, TODAY),
                queryEnum("tradePointType", "Тип торговой точки", false, TRADE_POINT_TYPES),
                queryLookup("tradePointId", "Торговая точка", false, LookupSource.TRADE_POINT)));
    }

    private static List<FieldDef> tradePointFields() {
        return List.of(
                bodyText("name", "Название", true),
                bodyText("address", "Адрес", true),
                bodyEnum("type", "Тип", true, TRADE_POINT_TYPES),
                bodyDecimal("tradeArea", "Торговая площадь", false),
                bodyDecimal("utilityPayments", "Коммунальные платежи", false),
                bodyInt("counterCount", "Число прилавков", false),
                bodyDecimal("rentPayment", "Аренда", false)
        );
    }

    private static ItemSpec supplyItems() {
        return new ItemSpec("items", "Позиции заявки", List.of(
                bodyLookup("productId", "Существующий товар", false, LookupSource.PRODUCT),
                field("productName", "Название нового товара", FieldType.TEXT, false, null, List.of(), FieldScope.BODY),
                field("units", "Единицы", FieldType.ENUM, false, null, UNITS, FieldScope.BODY),
                field("requestedQuantity", "Количество", FieldType.INTEGER, true, null, List.of(), FieldScope.BODY)
        ));
    }

    private static ItemSpec supplierOrderItems() {
        return new ItemSpec("items", "Позиции заказа", List.of(
                bodyLookup("productId", "Существующий товар", false, LookupSource.PRODUCT),
                field("productName", "Название нового товара", FieldType.TEXT, false, null, List.of(), FieldScope.BODY),
                field("units", "Единицы", FieldType.ENUM, false, null, UNITS, FieldScope.BODY),
                field("quantity", "Количество", FieldType.INTEGER, true, null, List.of(), FieldScope.BODY),
                field("purchasePrice", "Закупочная цена", FieldType.DECIMAL, true, null, List.of(), FieldScope.BODY)
        ));
    }

    private static ItemSpec deliveryItems() {
        return new ItemSpec("items", "Позиции поставки", List.of(
                bodyLookup("productId", "Существующий товар", false, LookupSource.PRODUCT),
                field("productName", "Название нового товара", FieldType.TEXT, false, null, List.of(), FieldScope.BODY),
                field("units", "Единицы", FieldType.ENUM, false, null, UNITS, FieldScope.BODY),
                field("quantity", "Количество", FieldType.INTEGER, true, null, List.of(), FieldScope.BODY),
                field("deliveryPrice", "Цена поставки", FieldType.DECIMAL, true, null, List.of(), FieldScope.BODY)
        ));
    }

    private static ItemSpec transferItems() {
        return new ItemSpec("items", "Позиции перемещения", List.of(
                bodyLookup("productId", "Товар", true, LookupSource.PRODUCT),
                field("quantity", "Количество", FieldType.INTEGER, true, null, List.of(), FieldScope.BODY)
        ));
    }

    private static ManagerAction action(String group, String title, String method, String pathTemplate, FieldDef... fields) {
        return new ManagerAction(group, title, method, pathTemplate, List.of(fields), null);
    }

    private static ManagerAction pagedAction(String group, String title, String pathTemplate) {
        return action(
                group,
                title,
                "GET",
                pathTemplate,
                queryInt("page", "Страница с 0", false),
                queryInt("size", "Размер страницы", false)
        );
    }

    private static ManagerAction actionWithItems(
            String group,
            String title,
            String method,
            String pathTemplate,
            List<FieldDef> fields,
            ItemSpec itemSpec
    ) {
        return new ManagerAction(group, title, method, pathTemplate, fields, itemSpec);
    }

}
