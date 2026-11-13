package org.example.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.example.exception.ApiException;
import org.example.model.AppContext;
import org.example.model.AppContextAware;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class TradeManagerController implements AppContextAware {
    private static final List<String> UNITS = List.of("kilograms", "grams", "liters", "milliliters");
    private static final List<String> TRADE_POINT_TYPES = List.of("DEPARTMENT_STORE", "SHOP", "STALL", "KIOSK");
    private static final List<String> EMPLOYEE_POSITIONS = List.of("SELLER", "SECTION_MANAGER", "TRADE_MANAGER");
    private static final List<String> SUPPLY_STATUSES = List.of("NEW", "APPROVED", "REJECTED", "ORDERED", "DELIVERED");
    private static final String TODAY = "__today__";
    private static final String MONTH_AGO = "__month_ago__";

    private final ObservableList<ManagerAction> currentActions = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> itemRows = FXCollections.observableArrayList();
    private final Map<FieldDef, Control> formControls = new LinkedHashMap<>();
    private final Map<FieldDef, Control> itemControls = new LinkedHashMap<>();
    private final Map<LookupSource, ObservableList<LookupOption>> lookupOptions = new LinkedHashMap<>();
    private final List<ManagerAction> actions = buildActions();

    private AppContext appContext;
    private ManagerAction selectedAction;

    @FXML
    private ComboBox<String> groupComboBox;

    @FXML
    private ListView<ManagerAction> actionListView;

    @FXML
    private Label actionTitleLabel;

    @FXML
    private GridPane formGrid;

    @FXML
    private VBox itemEditorBox;

    @FXML
    private Label itemEditorLabel;

    @FXML
    private GridPane itemFormGrid;

    @FXML
    private TableView<Map<String, Object>> itemTable;

    @FXML
    private Button addItemButton;

    @FXML
    private Button removeItemButton;

    @FXML
    private Button clearItemsButton;

    @FXML
    private TableView<Map<String, Object>> resultTable;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        groupComboBox.setItems(FXCollections.observableArrayList(
                actions.stream().map(ManagerAction::group).distinct().toList()
        ));

        actionListView.setItems(currentActions);
        actionListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(ManagerAction action, boolean empty) {
                super.updateItem(action, empty);
                setText(empty || action == null ? null : action.title());
            }
        });

        groupComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, group) -> {
            currentActions.setAll(actions.stream()
                    .filter(action -> Objects.equals(action.group(), group))
                    .toList());
            if (!currentActions.isEmpty()) {
                actionListView.getSelectionModel().selectFirst();
            }
        });

        actionListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, action) -> {
            selectedAction = action;
            renderActionForm(action);
        });

        itemTable.setItems(itemRows);
        itemTable.setPlaceholder(new Label("Позиции не добавлены"));
        clearResultTable("Результат операции");

        if (!groupComboBox.getItems().isEmpty()) {
            groupComboBox.getSelectionModel().selectFirst();
        }
    }

    @Override
    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
        loadLookups();
        renderActionForm(selectedAction);
    }

    @FXML
    private void onRefreshLookups() {
        loadLookups();
        renderActionForm(selectedAction);
    }

    @FXML
    private void onExecuteAction() {
        if (selectedAction == null) {
            showWarning("Операция не выбрана", "Выберите операцию слева.");
            return;
        }

        runManagerAction(() -> {
            RequestData request = buildRequest(selectedAction);
            Object response = appContext.getManagerClient().execute(
                    token(),
                    selectedAction.method(),
                    request.path(),
                    request.queryParams(),
                    request.body()
            );
            renderResponse(response);
            if (!(response instanceof Map<?, ?> map && map.containsKey("totalCount"))) {
                statusLabel.setText("Выполнено: " + selectedAction.title());
            }
        });
    }

    @FXML
    private void onAddItem() {
        if (selectedAction == null || selectedAction.itemSpec() == null) {
            return;
        }

        try {
            Map<String, Object> item = new LinkedHashMap<>();
            for (FieldDef field : selectedAction.itemSpec().fields()) {
                Object value = readValue(field, itemControls.get(field));
                if (value != null || field.required()) {
                    item.put(field.key(), value);
                }
            }
            itemRows.add(item);
            renderItemTable();
            clearItemInputs();
        } catch (Exception exception) {
            showWarning("Позиция не добавлена", exception.getMessage());
        }
    }

    @FXML
    private void onRemoveItem() {
        Map<String, Object> selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            itemRows.remove(selected);
            renderItemTable();
        }
    }

    @FXML
    private void onClearItems() {
        itemRows.clear();
        renderItemTable();
    }

    private void renderActionForm(ManagerAction action) {
        formControls.clear();
        itemControls.clear();
        itemRows.clear();
        formGrid.getChildren().clear();
        itemFormGrid.getChildren().clear();
        actionTitleLabel.setText(action == null ? "Операция не выбрана" : action.title());
        statusLabel.setText("");
        clearResultTable("Результат операции");

        if (action == null) {
            itemEditorBox.setVisible(false);
            itemEditorBox.setManaged(false);
            return;
        }

        int row = 0;
        for (FieldDef field : action.fields()) {
            Label label = new Label(field.label());
            label.getStyleClass().add("field-label");
            Control control = createControl(field);
            formControls.put(field, control);
            formGrid.add(label, 0, row);
            formGrid.add(control, 1, row);
            GridPane.setMargin(label, new Insets(3, 8, 3, 0));
            GridPane.setMargin(control, new Insets(3, 0, 3, 0));
            row++;
        }

        renderItemEditor(action.itemSpec());
        installUpdatePrefill(action);
    }

    private void renderItemEditor(ItemSpec itemSpec) {
        boolean hasItems = itemSpec != null;
        itemEditorBox.setVisible(hasItems);
        itemEditorBox.setManaged(hasItems);
        addItemButton.setVisible(hasItems);
        addItemButton.setManaged(hasItems);
        removeItemButton.setVisible(hasItems);
        removeItemButton.setManaged(hasItems);
        clearItemsButton.setVisible(hasItems);
        clearItemsButton.setManaged(hasItems);

        if (!hasItems) {
            return;
        }

        itemEditorLabel.setText(itemSpec.title());
        int row = 0;
        for (FieldDef field : itemSpec.fields()) {
            Label label = new Label(field.label());
            label.getStyleClass().add("field-label");
            Control control = createControl(field);
            itemControls.put(field, control);
            itemFormGrid.add(label, 0, row);
            itemFormGrid.add(control, 1, row);
            GridPane.setMargin(label, new Insets(3, 8, 3, 0));
            GridPane.setMargin(control, new Insets(3, 0, 3, 0));
            row++;
        }
        renderItemTable();
    }

    private Control createControl(FieldDef field) {
        if (field.lookupSource() != null) {
            ObservableList<LookupOption> loadedOptions = lookupOptions.getOrDefault(
                    field.lookupSource(),
                    FXCollections.observableArrayList()
            );
            ObservableList<LookupOption> options = FXCollections.observableArrayList();
            if (!field.required()) {
                options.add(new LookupOption(null, "Не выбрано", Map.of()));
            }
            options.addAll(loadedOptions);

            ComboBox<LookupOption> comboBox = new ComboBox<>(options);
            comboBox.setMaxWidth(Double.MAX_VALUE);
            if (!options.isEmpty()) {
                comboBox.getSelectionModel().selectFirst();
            }
            return comboBox;
        }

        return switch (field.type()) {
            case BOOLEAN -> {
                CheckBox checkBox = new CheckBox();
                checkBox.setSelected(Boolean.parseBoolean(resolveDefault(field.defaultValue())));
                yield checkBox;
            }
            case DATE -> {
                DatePicker datePicker = new DatePicker();
                String defaultValue = resolveDefault(field.defaultValue());
                if (defaultValue != null && !defaultValue.isBlank()) {
                    datePicker.setValue(LocalDate.parse(defaultValue));
                }
                yield datePicker;
            }
            case ENUM -> {
                ComboBox<String> comboBox = new ComboBox<>(FXCollections.observableArrayList(field.options()));
                comboBox.setEditable(!field.required());
                String defaultValue = resolveDefault(field.defaultValue());
                if (defaultValue != null && !defaultValue.isBlank()) {
                    comboBox.setValue(defaultValue);
                }
                comboBox.setMaxWidth(Double.MAX_VALUE);
                yield comboBox;
            }
            default -> {
                TextField textField = new TextField(resolveDefault(field.defaultValue()));
                textField.setPromptText(field.required() ? "обязательно" : "");
                textField.setMaxWidth(Double.MAX_VALUE);
                yield textField;
            }
        };
    }

    private void installUpdatePrefill(ManagerAction action) {
        if (!List.of("PUT", "PATCH").contains(action.method())) {
            return;
        }
        if (action.fields().stream().noneMatch(field -> field.scopes().contains(FieldScope.BODY))) {
            return;
        }

        formControls.forEach((field, control) -> {
            if (field.scopes().contains(FieldScope.PATH) && control instanceof ComboBox<?> comboBox) {
                comboBox.valueProperty().addListener((observable, oldValue, newValue) -> prefillBodyFields(action));
            }
        });
        prefillBodyFields(action);
    }

    private void prefillBodyFields(ManagerAction action) {
        Map<String, Object> sourceRow = findPrefillSourceRow(action);
        if (sourceRow.isEmpty()) {
            return;
        }

        formControls.forEach((field, control) -> {
            if (!field.scopes().contains(FieldScope.BODY)) {
                return;
            }
            if (sourceRow.containsKey(field.key())) {
                setControlValue(field, control, sourceRow.get(field.key()));
            }
        });
    }

    private Map<String, Object> findPrefillSourceRow(ManagerAction action) {
        Map<String, Object> directRow = findDirectSelectedRow(action);
        if (!directRow.isEmpty()) {
            return directRow;
        }

        Map<String, Object> pathValues = currentPathValues(action);
        if (pathValues.isEmpty()) {
            return Map.of();
        }

        List<String> bodyKeys = action.fields().stream()
                .filter(field -> field.scopes().contains(FieldScope.BODY))
                .map(FieldDef::key)
                .toList();

        return lookupOptions.values().stream()
                .flatMap(options -> options.stream())
                .map(LookupOption::row)
                .filter(row -> matchesPathValues(row, pathValues))
                .filter(row -> bodyKeys.stream().anyMatch(row::containsKey))
                .findFirst()
                .orElse(Map.of());
    }

    private Map<String, Object> findDirectSelectedRow(ManagerAction action) {
        List<String> bodyKeys = action.fields().stream()
                .filter(field -> field.scopes().contains(FieldScope.BODY))
                .map(FieldDef::key)
                .toList();

        return formControls.entrySet().stream()
                .filter(entry -> entry.getKey().scopes().contains(FieldScope.PATH))
                .map(entry -> selectedLookupOption(entry.getValue()))
                .filter(Objects::nonNull)
                .map(LookupOption::row)
                .filter(row -> bodyKeys.stream().anyMatch(row::containsKey))
                .findFirst()
                .orElse(Map.of());
    }

    private Map<String, Object> currentPathValues(ManagerAction action) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (Map.Entry<FieldDef, Control> entry : formControls.entrySet()) {
            FieldDef field = entry.getKey();
            if (!field.scopes().contains(FieldScope.PATH)) {
                continue;
            }
            Object value = readOptionalValue(field, entry.getValue());
            if (value == null) {
                return Map.of();
            }
            values.put(field.key(), value);
        }
        return values;
    }

    private Object readOptionalValue(FieldDef field, Control control) {
        Object rawValue = switch (control) {
            case TextField textField -> textField.getText();
            case DatePicker datePicker -> datePicker.getValue();
            case ComboBox<?> comboBox -> comboValue(comboBox);
            case CheckBox checkBox -> checkBox.isSelected();
            default -> null;
        };

        if (rawValue instanceof LookupOption option) {
            return option.value();
        }
        if (rawValue == null || String.valueOf(rawValue).isBlank()) {
            return null;
        }

        return switch (field.type()) {
            case TEXT, ENUM -> String.valueOf(rawValue).trim();
            case INTEGER -> toIntegerOrNull(rawValue);
            case DECIMAL -> new BigDecimal(String.valueOf(rawValue).trim());
            case BOOLEAN -> rawValue instanceof Boolean value ? value : Boolean.valueOf(String.valueOf(rawValue));
            case DATE -> rawValue instanceof LocalDate date ? date.toString() : String.valueOf(rawValue);
        };
    }

    private LookupOption selectedLookupOption(Control control) {
        if (control instanceof ComboBox<?> comboBox && comboBox.getValue() instanceof LookupOption option) {
            return option;
        }
        return null;
    }

    private boolean matchesPathValues(Map<String, Object> row, Map<String, Object> pathValues) {
        return pathValues.entrySet().stream()
                .allMatch(entry -> Objects.equals(toComparableValue(entry.getValue()), toComparableValue(row.get(entry.getKey()))));
    }

    private Object toComparableValue(Object value) {
        Integer integerValue = toIntegerOrNull(value);
        return integerValue == null ? valueToString(value) : integerValue;
    }

    private void setControlValue(FieldDef field, Control control, Object value) {
        if (control instanceof TextField textField) {
            textField.setText(valueToString(value));
        } else if (control instanceof DatePicker datePicker) {
            datePicker.setValue(value == null || valueToString(value).isBlank() ? null : LocalDate.parse(valueToString(value)));
        } else if (control instanceof CheckBox checkBox) {
            checkBox.setSelected(Boolean.parseBoolean(valueToString(value)));
        } else if (control instanceof ComboBox<?> comboBox) {
            setComboBoxValue(field, comboBox, value);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setComboBoxValue(FieldDef field, ComboBox<?> comboBox, Object value) {
        ComboBox rawComboBox = comboBox;
        if (field.lookupSource() != null) {
            Integer id = toIntegerOrNull(value);
            for (Object item : comboBox.getItems()) {
                if (item instanceof LookupOption option && Objects.equals(option.value(), id)) {
                    rawComboBox.getSelectionModel().select(item);
                    return;
                }
            }
            rawComboBox.getSelectionModel().clearSelection();
            return;
        }

        String text = valueToString(value);
        if (comboBox.isEditable()) {
            comboBox.getEditor().setText(text);
        }
        rawComboBox.setValue(text.isBlank() ? null : text);
    }

    private RequestData buildRequest(ManagerAction action) {
        String path = action.pathTemplate();
        Map<String, Object> queryParams = new LinkedHashMap<>();
        Map<String, Object> body = new LinkedHashMap<>();

        for (Map.Entry<FieldDef, Control> entry : formControls.entrySet()) {
            FieldDef field = entry.getKey();
            Object value = readValue(field, entry.getValue());

            if (field.scopes().contains(FieldScope.PATH)) {
                if (value == null) {
                    throw new IllegalArgumentException("Не заполнено поле: " + field.label());
                }
                path = path.replace("{" + field.key() + "}", String.valueOf(value));
            }
            if (field.scopes().contains(FieldScope.QUERY) && value != null) {
                queryParams.put(field.key(), value);
            }
            if (field.scopes().contains(FieldScope.BODY) && (value != null || field.required())) {
                body.put(field.key(), value);
            }
        }

        if (action.itemSpec() != null) {
            if (itemRows.isEmpty()) {
                throw new IllegalArgumentException("Добавьте хотя бы одну позицию.");
            }
            body.put(action.itemSpec().bodyKey(), new ArrayList<>(itemRows));
        }

        return new RequestData(path, queryParams, body);
    }

    private Object readValue(FieldDef field, Control control) {
        Object rawValue = switch (control) {
            case TextField textField -> textField.getText();
            case DatePicker datePicker -> datePicker.getValue();
            case ComboBox<?> comboBox -> comboValue(comboBox);
            case CheckBox checkBox -> checkBox.isSelected();
            default -> null;
        };

        if (rawValue instanceof LookupOption option) {
            if (option.value() == null) {
                if (field.required()) {
                    throw new IllegalArgumentException("Не заполнено поле: " + field.label());
                }
                return null;
            }
            return option.value();
        }

        if (rawValue == null || String.valueOf(rawValue).isBlank()) {
            if (field.required()) {
                throw new IllegalArgumentException("Не заполнено поле: " + field.label());
            }
            return null;
        }

        return switch (field.type()) {
            case TEXT, ENUM -> String.valueOf(rawValue).trim();
            case INTEGER -> Integer.valueOf(String.valueOf(rawValue).trim());
            case DECIMAL -> new BigDecimal(String.valueOf(rawValue).trim());
            case BOOLEAN -> rawValue instanceof Boolean value ? value : Boolean.valueOf(String.valueOf(rawValue));
            case DATE -> rawValue instanceof LocalDate date ? date.toString() : String.valueOf(rawValue);
        };
    }

    private Object comboValue(ComboBox<?> comboBox) {
        if (comboBox.getValue() instanceof LookupOption) {
            return comboBox.getValue();
        }
        if (comboBox.isEditable()) {
            String text = comboBox.getEditor().getText();
            if (text != null && !text.isBlank()) {
                return text;
            }
        }
        return comboBox.getValue();
    }

    private void renderResponse(Object response) {
        if (response instanceof Map<?, ?> map && map.containsKey("totalCount") && map.containsKey("rows")) {
            statusLabel.setText("Всего: " + map.get("totalCount"));
            renderRows(toMapList(map.get("rows")), "Строки не найдены");
            return;
        }

        if (response instanceof List<?> list) {
            renderRows(toMapList(list), "Строки не найдены");
            return;
        }

        if (response instanceof Map<?, ?> map) {
            renderRows(List.of(toStringKeyMap(map)), "Операция выполнена");
            return;
        }

        renderRows(List.of(Map.of("result", valueToString(response))), "Операция выполнена");
    }

    private void renderRows(List<Map<String, Object>> rows, String emptyText) {
        resultTable.getColumns().clear();
        resultTable.getItems().clear();

        if (rows == null || rows.isEmpty()) {
            resultTable.setPlaceholder(new Label(emptyText));
            return;
        }

        List<Map<String, Object>> displayRows = presentRows(rows);
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
            resultTable.getColumns().add(column);
        });
        resultTable.setItems(FXCollections.observableArrayList(displayRows));
    }

    private List<Map<String, Object>> presentRows(List<Map<String, Object>> rows) {
        return rows.stream()
                .map(this::presentRow)
                .toList();
    }

    private Map<String, Object> presentRow(Map<String, Object> row) {
        Map<String, Object> displayRow = new LinkedHashMap<>();
        row.forEach((key, value) -> {
            PresentedCell cell = presentCell(key, value);
            putUnique(displayRow, cell.columnName(), cell.value());
        });
        return displayRow;
    }

    private PresentedCell presentCell(String key, Object value) {
        String normalizedKey = normalizeKey(key);
        return switch (normalizedKey) {
            case "productid" -> lookupCell("Товар", LookupSource.PRODUCT, value);
            case "tradepointid" -> lookupCell("Торговая точка", LookupSource.TRADE_POINT, value);
            case "customerid" -> lookupCell("Покупатель", LookupSource.CUSTOMER, value);
            case "sendertradepointid" -> lookupCell("Точка отправления", LookupSource.TRADE_POINT, value);
            case "receivertradepointid" -> lookupCell("Точка получения", LookupSource.TRADE_POINT, value);
            case "departmentstoreid" -> lookupCell("Универмаг", LookupSource.DEPARTMENT_STORE, value);
            case "supplierid" -> lookupCell("Поставщик", LookupSource.SUPPLIER, value);
            case "sellerid" -> lookupCell("Продавец", LookupSource.SELLER, value);
            case "managerid", "sectionmanagerid" -> new PresentedCell(
                    "Менеджер",
                    lookupLabelOrRaw(value, LookupSource.SECTION_MANAGER, LookupSource.EMPLOYEE)
            );
            case "employeeid" -> lookupCell("Сотрудник", LookupSource.EMPLOYEE, value);
            case "hallid" -> lookupCell("Торговый зал", LookupSource.HALL, value);
            case "sectionid" -> lookupCell("Секция", LookupSource.SECTION, value);
            case "saleid" -> lookupCell("Продажа", LookupSource.SALE, value);
            case "saleitemid" -> lookupCell("Позиция продажи", LookupSource.SALE_ITEM, value);
            case "supplyrequestid" -> lookupCell("Заявка на поставку", LookupSource.SUPPLY_REQUEST, value);
            case "supplyrequestitemid" -> lookupCell("Позиция заявки", LookupSource.SUPPLY_REQUEST_ITEM, value);
            case "supplierorderid" -> lookupCell("Заказ поставщику", LookupSource.SUPPLIER_ORDER, value);
            case "supplierorderitemid" -> lookupCell("Позиция заказа", LookupSource.SUPPLIER_ORDER_ITEM, value);
            case "deliveryid" -> lookupCell("Поставка", LookupSource.DELIVERY, value);
            case "deliveryitemid" -> lookupCell("Позиция поставки", LookupSource.DELIVERY_ITEM, value);
            case "producttransferid", "transferid" -> lookupCell("Перемещение", LookupSource.TRANSFER, value);
            case "producttransferitemid", "transferitemid" -> lookupCell("Позиция перемещения", LookupSource.TRANSFER_ITEM, value);
            case "sellerassignmentid" -> lookupCell("Назначение продавца", LookupSource.SELLER_ASSIGNMENT, value);
            case "sectionmanagerassignmentid" -> lookupCell(
                    "Назначение менеджера",
                    LookupSource.SECTION_MANAGER_ASSIGNMENT,
                    value
            );
            case "type", "tradepointtype" -> new PresentedCell("Тип торговой точки", translateTradePointType(value));
            case "position" -> new PresentedCell("Должность", translatePosition(value));
            case "status" -> new PresentedCell("Статус", translateSupplyStatus(value));
            case "units" -> new PresentedCell("Единицы измерения", translateUnits(value));
            default -> new PresentedCell(humanizeColumn(key), value);
        };
    }

    private PresentedCell lookupCell(String columnName, LookupSource source, Object value) {
        return new PresentedCell(columnName, lookupLabelOrRaw(value, source));
    }

    private String lookupLabelOrRaw(Object id, LookupSource... sources) {
        Integer value = toIntegerOrNull(id);
        if (value == null) {
            return valueToString(id);
        }

        for (LookupSource source : sources) {
            String label = findLookupLabel(source, value);
            if (label != null) {
                return label;
            }
        }
        return valueToString(id);
    }

    private String findLookupLabel(LookupSource source, Integer value) {
        return lookupOptions.getOrDefault(source, FXCollections.observableArrayList()).stream()
                .filter(option -> Objects.equals(option.value(), value))
                .findFirst()
                .map(LookupOption::label)
                .orElse(null);
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

    private void renderItemTable() {
        itemTable.getColumns().clear();
        if (itemRows.isEmpty()) {
            itemTable.setPlaceholder(new Label("Позиции не добавлены"));
            return;
        }

        List<FieldDef> fields = selectedAction == null || selectedAction.itemSpec() == null
                ? List.of()
                : selectedAction.itemSpec().fields();

        fields.forEach(field -> {
            TableColumn<Map<String, Object>, String> column = new TableColumn<>(field.label());
            column.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(
                    formatFieldValue(field, cellData.getValue().get(field.key()))
            ));
            column.setPrefWidth(130);
            itemTable.getColumns().add(column);
        });
    }

    private String formatFieldValue(FieldDef field, Object value) {
        if (field.lookupSource() == null || value == null) {
            return valueToString(value);
        }

        return lookupOptions.getOrDefault(field.lookupSource(), FXCollections.observableArrayList()).stream()
                .filter(option -> Objects.equals(option.value(), toInteger(value)))
                .findFirst()
                .map(LookupOption::label)
                .orElse(valueToString(value));
    }

    private List<Map<String, Object>> toMapList(Object value) {
        if (value instanceof Map<?, ?> map) {
            if (map.containsKey("rows")) {
                return toMapList(map.get("rows"));
            }
            if (map.containsKey("content")) {
                return toMapList(map.get("content"));
            }
        }
        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        return list.stream()
                .map(item -> item instanceof Map<?, ?> map ? toStringKeyMap(map) : Map.<String, Object>of("value", item))
                .toList();
    }

    private Map<String, Object> toStringKeyMap(Map<?, ?> map) {
        Map<String, Object> converted = new LinkedHashMap<>();
        map.forEach((key, value) -> converted.put(String.valueOf(key), value));
        return converted;
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

    private void loadLookups() {
        if (appContext == null) {
            return;
        }

        runManagerAction(() -> {
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

            statusLabel.setText("Справочники обновлены");
        });
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
        try {
            Object response = appContext.getManagerClient().execute(token(), "GET", path, Map.of("page", 0, "size", 500), Map.of());
            ObservableList<LookupOption> options = FXCollections.observableArrayList(toMapList(response).stream()
                    .filter(filter)
                    .map(row -> new LookupOption(valueBuilder.apply(row), labelBuilder.apply(row), row))
                    .filter(option -> !requireValue || option.value() != null)
                    .toList());
            lookupOptions.put(source, options);
        } catch (Exception exception) {
            lookupOptions.put(source, FXCollections.observableArrayList());
        }
    }

    private String lookupLabel(LookupSource source, Object id) {
        Integer value = toInteger(id);
        if (value == null) {
            return "";
        }

        return lookupOptions.getOrDefault(source, FXCollections.observableArrayList()).stream()
                .filter(option -> Objects.equals(option.value(), value))
                .findFirst()
                .map(LookupOption::label)
                .orElse("не найдено");
    }

    private void clearResultTable(String placeholder) {
        resultTable.getColumns().clear();
        resultTable.getItems().clear();
        resultTable.setPlaceholder(new Label(placeholder));
    }

    private void clearItemInputs() {
        itemControls.forEach((field, control) -> {
            if (control instanceof TextField textField) {
                textField.clear();
            } else if (control instanceof DatePicker datePicker) {
                datePicker.setValue(null);
            }
        });
    }

    private void runManagerAction(Runnable action) {
        try {
            action.run();
        } catch (ApiException exception) {
            showError(exception.getErrorResponse().getCode(), exception.getErrorResponse().getMessage());
        } catch (Exception exception) {
            showError("UNKNOWN_ERROR", exception.getMessage());
        }
    }

    private String resolveDefault(String value) {
        if (TODAY.equals(value)) {
            return LocalDate.now().toString();
        }
        if (MONTH_AGO.equals(value)) {
            return LocalDate.now().minusMonths(1).toString();
        }
        return value;
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
        alert.setTitle("Ошибка менеджера");
        alert.setHeaderText("Операция не выполнена");
        alert.showAndWait();
    }

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

    private List<ManagerAction> buildActions() {
        List<ManagerAction> list = new ArrayList<>();
        addCatalogActions(list);
        addSalesActions(list);
        addSupplierActions(list);
        addSupplyActions(list);
        addTransferActions(list);
        addTradePointActions(list);
        addEmployeeActions(list);
        addReportActions(list);
        return list;
    }

    private void addCatalogActions(List<ManagerAction> list) {
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

    private void addSalesActions(List<ManagerAction> list) {
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

    private void addSupplierActions(List<ManagerAction> list) {
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

    private void addSupplyActions(List<ManagerAction> list) {
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

    private void addTransferActions(List<ManagerAction> list) {
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

    private void addTradePointActions(List<ManagerAction> list) {
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

    private void addEmployeeActions(List<ManagerAction> list) {
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

    private void addReportActions(List<ManagerAction> list) {
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

    private List<FieldDef> tradePointFields() {
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

    private ItemSpec supplyItems() {
        return new ItemSpec("items", "Позиции заявки", List.of(
                bodyLookup("productId", "Существующий товар", false, LookupSource.PRODUCT),
                field("productName", "Название нового товара", FieldType.TEXT, false, null, List.of(), FieldScope.BODY),
                field("units", "Единицы", FieldType.ENUM, false, null, UNITS, FieldScope.BODY),
                field("requestedQuantity", "Количество", FieldType.INTEGER, true, null, List.of(), FieldScope.BODY)
        ));
    }

    private ItemSpec supplierOrderItems() {
        return new ItemSpec("items", "Позиции заказа", List.of(
                bodyLookup("productId", "Существующий товар", false, LookupSource.PRODUCT),
                field("productName", "Название нового товара", FieldType.TEXT, false, null, List.of(), FieldScope.BODY),
                field("units", "Единицы", FieldType.ENUM, false, null, UNITS, FieldScope.BODY),
                field("quantity", "Количество", FieldType.INTEGER, true, null, List.of(), FieldScope.BODY),
                field("purchasePrice", "Закупочная цена", FieldType.DECIMAL, true, null, List.of(), FieldScope.BODY)
        ));
    }

    private ItemSpec deliveryItems() {
        return new ItemSpec("items", "Позиции поставки", List.of(
                bodyLookup("productId", "Существующий товар", false, LookupSource.PRODUCT),
                field("productName", "Название нового товара", FieldType.TEXT, false, null, List.of(), FieldScope.BODY),
                field("units", "Единицы", FieldType.ENUM, false, null, UNITS, FieldScope.BODY),
                field("quantity", "Количество", FieldType.INTEGER, true, null, List.of(), FieldScope.BODY),
                field("deliveryPrice", "Цена поставки", FieldType.DECIMAL, true, null, List.of(), FieldScope.BODY)
        ));
    }

    private ItemSpec transferItems() {
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

    private enum FieldType {
        TEXT,
        INTEGER,
        DECIMAL,
        BOOLEAN,
        DATE,
        ENUM
    }

    private enum FieldScope {
        PATH,
        QUERY,
        BODY
    }

    private enum LookupSource {
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

    private record FieldDef(
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

    private record LookupOption(
            Integer value,
            String label,
            Map<String, Object> row
    ) {
        @Override
        public String toString() {
            return label;
        }
    }

    private record PresentedCell(
            String columnName,
            Object value
    ) {
    }

    private record ItemSpec(
            String bodyKey,
            String title,
            List<FieldDef> fields
    ) {
    }

    private record ManagerAction(
            String group,
            String title,
            String method,
            String pathTemplate,
            List<FieldDef> fields,
            ItemSpec itemSpec
    ) {
    }

    private record RequestData(
            String path,
            Map<String, Object> queryParams,
            Map<String, Object> body
    ) {
    }
}
