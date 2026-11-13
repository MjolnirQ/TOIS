package org.example.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.example.exception.ApiException;
import org.example.http_client.DTOrequest.CreateSupplyRequestWithItemsRequest;
import org.example.http_client.DTOrequest.SaleLineRequest;
import org.example.http_client.DTOrequest.SellerSaleRequest;
import org.example.http_client.DTOrequest.SupplyRequestLineRequest;
import org.example.http_client.DTOresponse.CustomerResponse;
import org.example.http_client.DTOresponse.SaleResponse;
import org.example.http_client.DTOresponse.SaleWithItemsResponse;
import org.example.http_client.DTOresponse.SupplyRequestWithItemsResponse;
import org.example.http_client.DTOresponse.TradePointProductPriceResponse;
import org.example.http_client.DTOresponse.TradePointResponse;
import org.example.model.AppContext;
import org.example.model.AppContextAware;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class SellerController implements AppContextAware {
    private final ObservableList<CatalogRow> catalogRows = FXCollections.observableArrayList();
    private final ObservableList<SaleItemRow> saleRows = FXCollections.observableArrayList();
    private final ObservableList<SupplyItemRow> supplyRows = FXCollections.observableArrayList();
    private final Map<Integer, CustomerResponse> knownCustomers = new HashMap<>();

    private AppContext appContext;
    private Integer currentCustomerId;

    @FXML
    private ComboBox<TradePointResponse> tradePointComboBox;

    @FXML
    private Label sellerStatusLabel;

    @FXML
    private TableView<CatalogRow> catalogTable;

    @FXML
    private DatePicker saleDatePicker;

    @FXML
    private TextField customerFullNameField;

    @FXML
    private TextField customerPhoneField;

    @FXML
    private Label customerStateLabel;

    @FXML
    private Spinner<Integer> saleQuantitySpinner;

    @FXML
    private TableView<SaleItemRow> saleItemsTable;

    @FXML
    private Label saleResultLabel;

    @FXML
    private TableView<SaleResponse> salesHistoryTable;

    @FXML
    private DatePicker supplyRequestDatePicker;

    @FXML
    private ComboBox<String> supplyStatusComboBox;

    @FXML
    private TextField supplyProductNameField;

    @FXML
    private ComboBox<String> supplyUnitsComboBox;

    @FXML
    private Spinner<Integer> supplyQuantitySpinner;

    @FXML
    private TableView<SupplyItemRow> supplyItemsTable;

    @FXML
    private Label supplyResultLabel;

    @FXML
    private DatePicker outputFromDatePicker;

    @FXML
    private DatePicker outputToDatePicker;

    @FXML
    private TableView<Map<String, Object>> outputTable;

    @FXML
    private void initialize() {
        saleDatePicker.setValue(LocalDate.now());
        supplyRequestDatePicker.setValue(LocalDate.now());
        outputFromDatePicker.setValue(LocalDate.now().minusMonths(1));
        outputToDatePicker.setValue(LocalDate.now());

        saleQuantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1, 1));
        supplyQuantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100000, 1));

        supplyStatusComboBox.setItems(FXCollections.observableArrayList("NEW"));
        supplyStatusComboBox.setValue("NEW");
        supplyStatusComboBox.setEditable(true);

        supplyUnitsComboBox.setItems(FXCollections.observableArrayList(
                "kilograms",
                "grams",
                "liters",
                "milliliters"
        ));
        supplyUnitsComboBox.setValue("kilograms");

        configureCatalogTable();
        configureSaleItemsTable();
        configureSalesHistoryTable();
        configureSupplyItemsTable();
        clearMapTable(outputTable, "Выработка за период");

        catalogTable.setItems(catalogRows);
        saleItemsTable.setItems(saleRows);
        supplyItemsTable.setItems(supplyRows);

        catalogTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, row) -> {
            int max = row == null || row.quantity() == null || row.quantity() < 1 ? 1 : row.quantity();
            saleQuantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, max, 1));
        });

        tradePointComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, row) -> {
            if (row != null) {
                loadCatalog();
                clearMapTable(outputTable, "Выработка за период");
            }
        });
    }

    @Override
    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
        loadAssignedTradePoints();
    }

    @FXML
    private void onRefreshCatalog() {
        loadCatalog();
    }

    @FXML
    private void onCreateCustomer() {
        runSellerAction(() -> {
            CustomerResponse customer = createCustomerFromFields();
            selectCustomer(customer);
        });
    }

    @FXML
    private void onClearCustomer() {
        currentCustomerId = null;
        customerFullNameField.clear();
        customerPhoneField.clear();
        customerStateLabel.setText("Покупатель не выбран");
    }

    @FXML
    private void onAddSelectedProductToSale() {
        CatalogRow row = selectedCatalogRow();
        if (row == null) {
            showWarning("Товар не выбран", "Выберите товар в таблице остатков.");
            return;
        }
        if (row.price() == null) {
            showWarning("Цена отсутствует", "Для выбранного товара нет цены в этой торговой точке.");
            return;
        }

        int quantity = saleQuantitySpinner.getValue();
        int alreadyAdded = saleRows.stream()
                .filter(item -> Objects.equals(item.productId(), row.productId()))
                .mapToInt(SaleItemRow::quantity)
                .sum();

        if (row.quantity() != null && alreadyAdded + quantity > row.quantity()) {
            showWarning("Недостаточно остатка", "Нельзя добавить больше текущего остатка товара.");
            return;
        }

        Optional<SaleItemRow> existing = saleRows.stream()
                .filter(item -> Objects.equals(item.productId(), row.productId()))
                .findFirst();

        if (existing.isPresent()) {
            int index = saleRows.indexOf(existing.get());
            saleRows.set(index, existing.get().withQuantity(existing.get().quantity() + quantity));
        } else {
            saleRows.add(new SaleItemRow(row.productId(), row.productName(), quantity, row.price()));
        }
    }

    @FXML
    private void onRemoveSaleItem() {
        SaleItemRow selected = saleItemsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            saleRows.remove(selected);
        }
    }

    @FXML
    private void onClearSaleItems() {
        saleRows.clear();
    }

    @FXML
    private void onCreateSale() {
        runSellerAction(() -> {
            TradePointResponse tradePoint = requireSelectedTradePoint();
            if (tradePoint == null) {
                return;
            }
            if (saleDatePicker.getValue() == null) {
                showWarning("Дата не выбрана", "Выберите дату продажи.");
                return;
            }
            if (saleRows.isEmpty()) {
                showWarning("Нет позиций", "Добавьте хотя бы одну позицию продажи.");
                return;
            }

            if (currentCustomerId == null && !customerFullNameField.getText().isBlank()) {
                selectCustomer(createCustomerFromFields());
            }

            List<SaleLineRequest> items = saleRows.stream()
                    .map(row -> new SaleLineRequest(row.productId(), row.quantity()))
                    .toList();

            SaleWithItemsResponse response = appContext.getSellerClient().createSale(
                    token(),
                    new SellerSaleRequest(
                            saleDatePicker.getValue().toString(),
                            tradePoint.id(),
                            currentCustomerId,
                            items
                    )
            );

            saleResultLabel.setText("Продажа #" + response.sale().id() + " создана, позиций: " + response.items().size());
            saleRows.clear();
            loadCatalog();
            loadSalesHistory();
        });
    }

    @FXML
    private void onLoadSalesHistory() {
        loadSalesHistory();
    }

    @FXML
    private void onAddSelectedProductToSupply() {
        CatalogRow row = selectedCatalogRow();
        if (row == null) {
            showWarning("Товар не выбран", "Выберите товар в таблице остатков.");
            return;
        }

        supplyRows.add(new SupplyItemRow(
                row.productId(),
                row.productName(),
                null,
                supplyQuantitySpinner.getValue()
        ));
    }

    @FXML
    private void onAddNewProductToSupply() {
        String productName = supplyProductNameField.getText().trim();
        String units = supplyUnitsComboBox.getValue();
        if (productName.isBlank()) {
            showWarning("Название не указано", "Введите название нового товара.");
            return;
        }
        if (units == null || units.isBlank()) {
            showWarning("Единицы не указаны", "Выберите единицы измерения.");
            return;
        }

        supplyRows.add(new SupplyItemRow(
                null,
                productName,
                units,
                supplyQuantitySpinner.getValue()
        ));
        supplyProductNameField.clear();
    }

    @FXML
    private void onRemoveSupplyItem() {
        SupplyItemRow selected = supplyItemsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            supplyRows.remove(selected);
        }
    }

    @FXML
    private void onClearSupplyItems() {
        supplyRows.clear();
    }

    @FXML
    private void onCreateSupplyRequest() {
        runSellerAction(() -> {
            TradePointResponse tradePoint = requireSelectedTradePoint();
            if (tradePoint == null) {
                return;
            }
            if (supplyRequestDatePicker.getValue() == null) {
                showWarning("Дата не выбрана", "Выберите дату заявки.");
                return;
            }
            if (supplyRows.isEmpty()) {
                showWarning("Нет позиций", "Добавьте хотя бы одну позицию заявки.");
                return;
            }

            List<SupplyRequestLineRequest> items = supplyRows.stream()
                    .map(row -> new SupplyRequestLineRequest(
                            row.productId(),
                            row.productId() == null ? row.productName() : null,
                            row.productId() == null ? row.units() : null,
                            row.requestedQuantity()
                    ))
                    .toList();

            SupplyRequestWithItemsResponse response = appContext.getSellerClient().createSupplyRequest(
                    token(),
                    new CreateSupplyRequestWithItemsRequest(
                            supplyRequestDatePicker.getValue().toString(),
                            tradePoint.id(),
                            supplyStatusComboBox.getValue(),
                            items
                    )
            );

            supplyResultLabel.setText(
                    "Заявка #" + response.supplyRequest().id() + " создана, позиций: " + response.items().size()
            );
            supplyRows.clear();
        });
    }

    @FXML
    private void onLoadOutput() {
        runSellerAction(() -> {
            TradePointResponse tradePoint = requireSelectedTradePoint();
            if (tradePoint == null) {
                return;
            }
            if (outputFromDatePicker.getValue() == null || outputToDatePicker.getValue() == null) {
                showWarning("Период не указан", "Выберите даты начала и конца периода.");
                return;
            }
            if (outputFromDatePicker.getValue().isAfter(outputToDatePicker.getValue())) {
                showWarning("Период некорректен", "Дата начала не должна быть позже даты конца.");
                return;
            }

            List<Map<String, Object>> rows = appContext.getSellerClient().findOwnOutput(
                    token(),
                    tradePoint.id(),
                    outputFromDatePicker.getValue().toString(),
                    outputToDatePicker.getValue().toString()
            );
            renderMapRows(outputTable, rows, "За выбранный период продаж не найдено");
        });
    }

    private void loadAssignedTradePoints() {
        runSellerAction(() -> {
            List<TradePointResponse> tradePoints = appContext.getSellerClient().findAssignedTradePoints(token());
            tradePointComboBox.setItems(FXCollections.observableArrayList(tradePoints));
            if (!tradePoints.isEmpty()) {
                tradePointComboBox.getSelectionModel().selectFirst();
                sellerStatusLabel.setText("Назначенных торговых точек: " + tradePoints.size());
            } else {
                sellerStatusLabel.setText("Нет назначенных торговых точек");
            }
        });
    }

    private void loadCatalog() {
        runSellerAction(() -> {
            TradePointResponse tradePoint = requireSelectedTradePoint();
            if (tradePoint == null) {
                return;
            }

            List<Map<String, Object>> stocks = appContext.getSellerClient().findStocks(token(), tradePoint.id());
            List<TradePointProductPriceResponse> prices = appContext.getSellerClient().findPrices(token(), tradePoint.id());
            Map<Integer, BigDecimal> pricesByProductId = new HashMap<>();
            for (TradePointProductPriceResponse price : prices) {
                pricesByProductId.put(price.productId(), price.price());
            }

            catalogRows.setAll(stocks.stream()
                    .map(row -> {
                        Integer productId = toInteger(row.get("product_id"));
                        return new CatalogRow(
                                productId,
                                valueToString(row.get("product_name")),
                                toInteger(row.get("quantity")),
                                pricesByProductId.get(productId)
                        );
                    })
                    .toList());
        });
    }

    private void loadSalesHistory() {
        runSellerAction(() -> {
            List<SaleResponse> sales = appContext.getSellerClient().findCurrentSellerSales(token());
            salesHistoryTable.setItems(FXCollections.observableArrayList(sales));
        });
    }

    private CustomerResponse createCustomerFromFields() {
        String fullName = customerFullNameField.getText().trim();
        if (fullName.isBlank()) {
            throw new IllegalArgumentException("Введите ФИО покупателя.");
        }

        return appContext.getSellerClient().createCustomer(
                token(),
                fullName,
                customerPhoneField.getText().trim().isBlank() ? null : customerPhoneField.getText().trim()
        );
    }

    private void selectCustomer(CustomerResponse customer) {
        currentCustomerId = customer.id();
        if (customer.id() != null) {
            knownCustomers.put(customer.id(), customer);
        }
        customerStateLabel.setText("Покупатель: " + formatCustomer(customer.id()));
    }

    private TradePointResponse requireSelectedTradePoint() {
        TradePointResponse tradePoint = tradePointComboBox.getSelectionModel().getSelectedItem();
        if (tradePoint == null) {
            showWarning("Торговая точка не выбрана", "Выберите назначенную торговую точку.");
            return null;
        }
        return tradePoint;
    }

    private CatalogRow selectedCatalogRow() {
        return catalogTable.getSelectionModel().getSelectedItem();
    }

    private void configureCatalogTable() {
        addColumn(catalogTable, "Товар", 260, CatalogRow::productName);
        addColumn(catalogTable, "Остаток", 110, row -> String.valueOf(row.quantity()));
        addColumn(catalogTable, "Цена", 120, row -> formatDecimal(row.price()));
        catalogTable.setPlaceholder(new Label("Остатки и цены не загружены"));
    }

    private void configureSaleItemsTable() {
        addColumn(saleItemsTable, "Товар", 260, SaleItemRow::productName);
        addColumn(saleItemsTable, "Количество", 120, row -> String.valueOf(row.quantity()));
        addColumn(saleItemsTable, "Цена", 120, row -> formatDecimal(row.price()));
        addColumn(saleItemsTable, "Сумма", 120, row -> formatDecimal(row.total()));
        saleItemsTable.setPlaceholder(new Label("Позиции продажи не добавлены"));
    }

    private void configureSalesHistoryTable() {
        addColumn(salesHistoryTable, "Продажа", 90, row -> "№" + row.id());
        addColumn(salesHistoryTable, "Дата", 120, SaleResponse::saleDate);
        addColumn(salesHistoryTable, "Торговая точка", 240, row -> formatTradePoint(row.tradePointId()));
        addColumn(salesHistoryTable, "Покупатель", 200, row -> formatCustomer(row.customerId()));
        salesHistoryTable.setPlaceholder(new Label("Продажи не загружены"));
    }

    private void configureSupplyItemsTable() {
        addColumn(supplyItemsTable, "Товар", 260, SupplyItemRow::productName);
        addColumn(supplyItemsTable, "Единицы", 120, row -> formatUnits(row.units()));
        addColumn(supplyItemsTable, "Количество", 120, row -> String.valueOf(row.requestedQuantity()));
        supplyItemsTable.setPlaceholder(new Label("Позиции заявки не добавлены"));
    }

    private <T> void addColumn(TableView<T> table, String title, double width, Function<T, String> valueExtractor) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(valueExtractor.apply(cellData.getValue())));
        column.setPrefWidth(width);
        table.getColumns().add(column);
    }

    private void renderMapRows(TableView<Map<String, Object>> table, List<Map<String, Object>> rows, String emptyText) {
        table.getColumns().clear();
        table.getItems().clear();

        if (rows == null || rows.isEmpty()) {
            table.setPlaceholder(new Label(emptyText));
            return;
        }

        List<Map<String, Object>> displayRows = rows.stream()
                .map(this::presentMapRow)
                .toList();
        List<String> columnNames = displayRows.stream()
                .flatMap(row -> row.keySet().stream())
                .distinct()
                .toList();

        columnNames.forEach(columnName -> {
            TableColumn<Map<String, Object>, String> column = new TableColumn<>(columnName);
            column.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(
                    valueToString(cellData.getValue().get(columnName))
            ));
            column.setPrefWidth(160);
            table.getColumns().add(column);
        });

        table.setItems(FXCollections.observableArrayList(displayRows));
    }

    private Map<String, Object> presentMapRow(Map<String, Object> row) {
        Map<String, Object> displayRow = new LinkedHashMap<>();
        row.forEach((key, value) -> {
            if (shouldHideTechnicalId(key, row)) {
                return;
            }
            putUnique(displayRow, humanizeMapColumn(key), presentMapValue(key, value));
        });
        return displayRow;
    }

    private boolean shouldHideTechnicalId(String key, Map<String, Object> row) {
        String normalizedKey = normalizeKey(key);
        return switch (normalizedKey) {
            case "productid" -> hasKey(row, "product_name") || hasKey(row, "productName");
            case "tradepointid" -> hasKey(row, "trade_point_name") || hasKey(row, "tradePointName");
            case "sellerid" -> hasKey(row, "seller_name") || hasKey(row, "sellerName");
            case "customerid" -> hasKey(row, "customer_name") || hasKey(row, "customerName") || hasKey(row, "full_name");
            default -> false;
        };
    }

    private Object presentMapValue(String key, Object value) {
        return switch (normalizeKey(key)) {
            case "productid" -> formatProduct(toIntegerOrNull(value));
            case "tradepointid" -> formatTradePoint(toIntegerOrNull(value));
            case "customerid" -> formatCustomer(toIntegerOrNull(value));
            case "units" -> formatUnits(valueToString(value));
            default -> value;
        };
    }

    private String humanizeMapColumn(String key) {
        return switch (normalizeKey(key)) {
            case "id" -> "Номер";
            case "productid", "productname" -> "Товар";
            case "tradepointid", "tradepointname" -> "Торговая точка";
            case "tradepointaddress" -> "Адрес торговой точки";
            case "sellerid", "sellername" -> "Продавец";
            case "customerid", "customername", "fullname" -> "Покупатель";
            case "phonenumber" -> "Телефон";
            case "quantity" -> "Количество";
            case "totalquantity" -> "Общее количество";
            case "price", "unitprice" -> "Цена";
            case "amount", "totalamount" -> "Сумма";
            case "output" -> "Выработка";
            case "saledate" -> "Дата продажи";
            case "requestdate" -> "Дата заявки";
            case "units" -> "Единицы измерения";
            default -> prettifyUnknownColumn(key);
        };
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

    private void clearMapTable(TableView<Map<String, Object>> table, String placeholder) {
        table.getColumns().clear();
        table.getItems().clear();
        table.setPlaceholder(new Label(placeholder));
    }

    private void runSellerAction(Runnable action) {
        try {
            action.run();
        } catch (ApiException exception) {
            showError(exception.getErrorResponse().getCode(), exception.getErrorResponse().getMessage());
        } catch (Exception exception) {
            showError("UNKNOWN_ERROR", exception.getMessage());
        }
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.valueOf(String.valueOf(value));
    }

    private Integer toIntegerOrNull(Object value) {
        try {
            return toInteger(value);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private String formatProduct(Integer productId) {
        if (productId == null) {
            return "";
        }
        return catalogRows.stream()
                .filter(row -> Objects.equals(row.productId(), productId))
                .findFirst()
                .map(CatalogRow::productName)
                .orElse("товар №" + productId);
    }

    private String formatTradePoint(Integer tradePointId) {
        if (tradePointId == null) {
            return "";
        }
        return tradePointComboBox.getItems().stream()
                .filter(tradePoint -> Objects.equals(tradePoint.id(), tradePointId))
                .findFirst()
                .map(TradePointResponse::toString)
                .orElse("торговая точка №" + tradePointId);
    }

    private String formatCustomer(Integer customerId) {
        if (customerId == null) {
            return "";
        }
        CustomerResponse customer = knownCustomers.get(customerId);
        if (customer == null) {
            return "покупатель №" + customerId;
        }
        String phone = customer.phoneNumber() == null || customer.phoneNumber().isBlank()
                ? ""
                : ", " + customer.phoneNumber();
        return customer.fullName() + phone;
    }

    private String formatUnits(String units) {
        return switch (valueToString(units)) {
            case "kilograms" -> "кг";
            case "grams" -> "г";
            case "liters" -> "л";
            case "milliliters" -> "мл";
            default -> valueToString(units);
        };
    }

    private boolean hasKey(Map<String, Object> row, String key) {
        return row.keySet().stream().anyMatch(existingKey -> Objects.equals(normalizeKey(existingKey), normalizeKey(key)));
    }

    private String normalizeKey(String key) {
        return key == null
                ? ""
                : key.replace("_", "")
                        .replace("-", "")
                        .toLowerCase(Locale.ROOT);
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

    private String valueToString(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Boolean bool) {
            return bool ? "да" : "нет";
        }
        return String.valueOf(value);
    }

    private String formatDecimal(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private String token() {
        return appContext.getSessionState().getToken();
    }

    private void showWarning(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(text);
        alert.showAndWait();
    }

    private void showError(String code, String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR, "Код: " + code + "\nТекст: " + text, ButtonType.OK);
        alert.setTitle("Ошибка продавца");
        alert.setHeaderText("Операция не выполнена");
        alert.showAndWait();
    }

    private record CatalogRow(
            Integer productId,
            String productName,
            Integer quantity,
            BigDecimal price
    ) {
    }

    private record SaleItemRow(
            Integer productId,
            String productName,
            Integer quantity,
            BigDecimal price
    ) {
        private SaleItemRow withQuantity(Integer quantity) {
            return new SaleItemRow(productId, productName, quantity, price);
        }

        private BigDecimal total() {
            return price == null ? null : price.multiply(BigDecimal.valueOf(quantity));
        }
    }

    private record SupplyItemRow(
            Integer productId,
            String productName,
            String units,
            Integer requestedQuantity
    ) {
    }
}
