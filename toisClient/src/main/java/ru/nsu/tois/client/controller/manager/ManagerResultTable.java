package ru.nsu.tois.client.controller.manager;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ru.nsu.tois.client.controller.manager.ManagerAction.LookupSource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static ru.nsu.tois.client.controller.manager.ManagerValues.toIntegerOrNull;
import static ru.nsu.tois.client.controller.manager.ManagerValues.toMapList;
import static ru.nsu.tois.client.controller.manager.ManagerValues.toStringKeyMap;
import static ru.nsu.tois.client.controller.manager.ManagerValues.valueToString;

final class ManagerResultTable {
    private final TableView<Map<String, Object>> table;
    private final Label statusLabel;

    ManagerResultTable(TableView<Map<String, Object>> table, Label statusLabel) {
        this.table = table;
        this.statusLabel = statusLabel;
    }

    void render(Object response, ManagerLookupCatalog lookups) {
        if (response instanceof Map<?, ?> map && map.containsKey("totalCount") && map.containsKey("rows")) {
            statusLabel.setText("Всего: " + map.get("totalCount"));
            renderRows(toMapList(map.get("rows")), "Строки не найдены", lookups);
            return;
        }

        if (response instanceof List<?> list) {
            renderRows(toMapList(list), "Строки не найдены", lookups);
            return;
        }

        if (response instanceof Map<?, ?> map) {
            renderRows(List.of(toStringKeyMap(map)), "Операция выполнена", lookups);
            return;
        }

        renderRows(List.of(Map.of("result", valueToString(response))), "Операция выполнена", lookups);
    }

    private void renderRows(List<Map<String, Object>> rows, String emptyText, ManagerLookupCatalog lookups) {
        table.getColumns().clear();
        table.getItems().clear();

        if (rows == null || rows.isEmpty()) {
            table.setPlaceholder(new Label(emptyText));
            return;
        }

        List<Map<String, Object>> displayRows = presentRows(rows, lookups);
        List<String> columnNames = displayRows.stream()
                .flatMap(row -> row.keySet().stream())
                .distinct()
                .toList();

        columnNames.forEach(columnName -> {
            TableColumn<Map<String, Object>, String> column = new TableColumn<>(columnName);
            column.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(
                    valueToString(cellData.getValue().get(columnName))
            ));
            column.setPrefWidth(150);
            table.getColumns().add(column);
        });
        table.setItems(FXCollections.observableArrayList(displayRows));
    }

    private List<Map<String, Object>> presentRows(List<Map<String, Object>> rows, ManagerLookupCatalog lookups) {
        return rows.stream()
                .map(row -> presentRow(row, lookups))
                .toList();
    }

    private Map<String, Object> presentRow(Map<String, Object> row, ManagerLookupCatalog lookups) {
        Map<String, Object> displayRow = new LinkedHashMap<>();
        row.forEach((key, value) -> {
            PresentedCell cell = presentCell(key, value, lookups);
            putUnique(displayRow, cell.columnName(), cell.value());
        });
        return displayRow;
    }

    private PresentedCell presentCell(String key, Object value, ManagerLookupCatalog lookups) {
        String normalizedKey = normalizeKey(key);
        return switch (normalizedKey) {
            case "productid" -> lookupCell("Товар", LookupSource.PRODUCT, value, lookups);
            case "tradepointid" -> lookupCell("Торговая точка", LookupSource.TRADE_POINT, value, lookups);
            case "customerid" -> lookupCell("Покупатель", LookupSource.CUSTOMER, value, lookups);
            case "sendertradepointid" -> lookupCell("Точка отправления", LookupSource.TRADE_POINT, value, lookups);
            case "receivertradepointid" -> lookupCell("Точка получения", LookupSource.TRADE_POINT, value, lookups);
            case "departmentstoreid" -> lookupCell("Универмаг", LookupSource.DEPARTMENT_STORE, value, lookups);
            case "supplierid" -> lookupCell("Поставщик", LookupSource.SUPPLIER, value, lookups);
            case "sellerid" -> lookupCell("Продавец", LookupSource.SELLER, value, lookups);
            case "managerid", "sectionmanagerid" -> new PresentedCell(
                    "Менеджер",
                    lookupLabelOrRaw(lookups, value, LookupSource.SECTION_MANAGER, LookupSource.EMPLOYEE)
            );
            case "employeeid" -> lookupCell("Сотрудник", LookupSource.EMPLOYEE, value, lookups);
            case "hallid" -> lookupCell("Торговый зал", LookupSource.HALL, value, lookups);
            case "sectionid" -> lookupCell("Секция", LookupSource.SECTION, value, lookups);
            case "saleid" -> lookupCell("Продажа", LookupSource.SALE, value, lookups);
            case "saleitemid" -> lookupCell("Позиция продажи", LookupSource.SALE_ITEM, value, lookups);
            case "supplyrequestid" -> lookupCell("Заявка на поставку", LookupSource.SUPPLY_REQUEST, value, lookups);
            case "supplyrequestitemid" -> lookupCell("Позиция заявки", LookupSource.SUPPLY_REQUEST_ITEM, value, lookups);
            case "supplierorderid" -> lookupCell("Заказ поставщику", LookupSource.SUPPLIER_ORDER, value, lookups);
            case "supplierorderitemid" -> lookupCell("Позиция заказа", LookupSource.SUPPLIER_ORDER_ITEM, value, lookups);
            case "deliveryid" -> lookupCell("Поставка", LookupSource.DELIVERY, value, lookups);
            case "deliveryitemid" -> lookupCell("Позиция поставки", LookupSource.DELIVERY_ITEM, value, lookups);
            case "producttransferid", "transferid" -> lookupCell("Перемещение", LookupSource.TRANSFER, value, lookups);
            case "producttransferitemid", "transferitemid" -> lookupCell("Позиция перемещения", LookupSource.TRANSFER_ITEM, value, lookups);
            case "sellerassignmentid" -> lookupCell("Назначение продавца", LookupSource.SELLER_ASSIGNMENT, value, lookups);
            case "sectionmanagerassignmentid" -> lookupCell(
                    "Назначение менеджера",
                    LookupSource.SECTION_MANAGER_ASSIGNMENT,
                    value, lookups
            );
            case "type", "tradepointtype" -> new PresentedCell("Тип торговой точки", translateTradePointType(value));
            case "position" -> new PresentedCell("Должность", translatePosition(value));
            case "status" -> new PresentedCell("Статус", translateSupplyStatus(value));
            case "units" -> new PresentedCell("Единицы измерения", translateUnits(value));
            default -> new PresentedCell(humanizeColumn(key), value);
        };
    }

    private PresentedCell lookupCell(String columnName, LookupSource source, Object value, ManagerLookupCatalog lookups) {
        return new PresentedCell(columnName, lookupLabelOrRaw(lookups, value, source));
    }

    private String lookupLabelOrRaw(ManagerLookupCatalog lookups, Object id, LookupSource... sources) {
        Integer value = toIntegerOrNull(id);
        if (value == null) {
            return valueToString(id);
        }

        for (LookupSource source : sources) {
            String label = lookups == null ? null : lookups.findLabel(source, value);
            if (label != null) {
                return label;
            }
        }
        return valueToString(id);
    }

    private void putUnique(Map<String, Object> row, String columnName, Object value) {
        if (!row.containsKey(columnName)) {
            row.put(columnName, value);
            return;
        }

        if (Objects.equals(valueToString(row.get(columnName)), valueToString(value))) {
            return;
        }

        int index = 2;
        String uniqueColumnName = columnName + " " + index;
        while (row.containsKey(uniqueColumnName)) {
            index++;
            uniqueColumnName = columnName + " " + index;
        }
        row.put(uniqueColumnName, value);
    }

    private String humanizeColumn(String key) {
        String normalizedKey = normalizeKey(key);
        return switch (normalizedKey) {
            case "id" -> "Номер";
            case "name" -> "Название";
            case "productname" -> "Название товара";
            case "tradepointname" -> "Название торговой точки";
            case "suppliername", "organizationname" -> "Название поставщика";
            case "sellername" -> "Продавец";
            case "managername", "sectionmanagername" -> "Менеджер";
            case "employeename", "fullname" -> "ФИО";
            case "customername" -> "Покупатель";
            case "phone", "phonenumber", "contactphone" -> "Телефон";
            case "contactperson" -> "Контактное лицо";
            case "address" -> "Адрес";
            case "description" -> "Описание";
            case "price" -> "Цена";
            case "purchaseprice" -> "Закупочная цена";
            case "supplierprice" -> "Цена поставщика";
            case "deliveryprice" -> "Цена поставки";
            case "unitprice" -> "Цена за единицу";
            case "quantity" -> "Количество";
            case "stock", "stockquantity", "currentstock" -> "Остаток";
            case "requestedquantity" -> "Запрошенное количество";
            case "soldquantity" -> "Проданное количество";
            case "deliveredquantity" -> "Поставленное количество";
            case "totalquantity" -> "Общее количество";
            case "totalamount", "amount" -> "Сумма";
            case "requestdate" -> "Дата заявки";
            case "orderdate" -> "Дата заказа";
            case "deliverydate" -> "Дата поставки";
            case "transferdate" -> "Дата перемещения";
            case "saledate" -> "Дата продажи";
            case "from", "datefrom", "startdate" -> "Дата с";
            case "to", "dateto", "enddate" -> "Дата по";
            case "ordernumber" -> "Номер заказа";
            case "floor" -> "Этаж";
            case "area" -> "Площадь";
            case "salesarea" -> "Площадь торговых залов";
            case "hallcount", "numberofhalls" -> "Количество залов";
            case "countercount", "numberofcounters" -> "Количество прилавков";
            case "salary" -> "Зарплата";
            case "isactive", "active" -> "Активен";
            case "ratio" -> "Отношение";
            case "metric" -> "Показатель";
            case "value", "result" -> "Значение";
            default -> prettifyUnknownColumn(key);
        };
    }

    private String prettifyUnknownColumn(String key) {
        String withSpaces = key.replace("_", " ")
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .trim();
        if (withSpaces.isEmpty()) {
            return key;
        }
        return withSpaces.substring(0, 1).toUpperCase(Locale.ROOT) + withSpaces.substring(1);
    }

    private String normalizeKey(String key) {
        return key == null
                ? ""
                : key.replace("_", "")
                        .replace("-", "")
                        .toLowerCase(Locale.ROOT);
    }

    private String translateTradePointType(Object value) {
        return switch (valueToString(value)) {
            case "DEPARTMENT_STORE" -> "универмаг";
            case "SHOP" -> "магазин";
            case "STALL" -> "палатка";
            case "KIOSK" -> "киоск";
            default -> valueToString(value);
        };
    }

    private String translatePosition(Object value) {
        return switch (valueToString(value)) {
            case "SELLER" -> "продавец";
            case "SECTION_MANAGER" -> "менеджер секции";
            case "TRADE_MANAGER" -> "менеджер торговли";
            default -> valueToString(value);
        };
    }

    private String translateSupplyStatus(Object value) {
        return switch (valueToString(value)) {
            case "NEW" -> "новая";
            case "APPROVED" -> "одобрена";
            case "REJECTED" -> "отклонена";
            case "ORDERED" -> "заказана";
            case "DELIVERED" -> "поставлена";
            default -> valueToString(value);
        };
    }

    private String translateUnits(Object value) {
        return switch (valueToString(value)) {
            case "kilograms" -> "кг";
            case "grams" -> "г";
            case "liters" -> "л";
            case "milliliters" -> "мл";
            default -> valueToString(value);
        };
    }

    void clear(String placeholder) {
        table.getColumns().clear();
        table.getItems().clear();
        table.setPlaceholder(new Label(placeholder));
    }

    private record PresentedCell(
            String columnName,
            Object value
    ) {
    }
}
