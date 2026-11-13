package ru.nsu.tois.client.controller.manager;

import java.util.EnumSet;
import java.util.List;

record ManagerAction(
        String group,
        String title,
        String method,
        String pathTemplate,
        List<FieldDef> fields,
        ItemSpec itemSpec
) {
    enum FieldType {
        TEXT,
        INTEGER,
        DECIMAL,
        BOOLEAN,
        DATE,
        ENUM
    }

    enum FieldScope {
        PATH,
        QUERY,
        BODY
    }

    enum LookupSource {
        PRODUCT,
        TRADE_POINT,
        CUSTOMER,
        DEPARTMENT_STORE,
        SUPPLIER,
        SUPPLIER_PRODUCT,
        TRADE_POINT_PRICE,
        TRADE_POINT_STOCK,
        EMPLOYEE,
        SELLER,
        SECTION_MANAGER,
        HALL,
        SECTION,
        SUPPLY_REQUEST,
        SUPPLY_REQUEST_ITEM,
        SUPPLIER_ORDER,
        SUPPLIER_ORDER_ITEM,
        DELIVERY,
        DELIVERY_ITEM,
        TRANSFER,
        TRANSFER_ITEM,
        SALE,
        SALE_ITEM,
        SELLER_ASSIGNMENT,
        SECTION_MANAGER_ASSIGNMENT
    }

    record FieldDef(
            String key,
            String label,
            FieldType type,
            EnumSet<FieldScope> scopes,
            boolean required,
            String defaultValue,
            List<String> options,
            LookupSource lookupSource
    ) {
    }

    record ItemSpec(
            String bodyKey,
            String title,
            List<FieldDef> fields
    ) {
    }

}
