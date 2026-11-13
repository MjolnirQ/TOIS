package ru.nsu.tois.client.controller.manager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import ru.nsu.tois.client.controller.manager.ManagerAction.FieldDef;
import ru.nsu.tois.client.controller.manager.ManagerAction.FieldScope;
import ru.nsu.tois.client.controller.manager.ManagerAction.LookupSource;
import ru.nsu.tois.client.controller.manager.ManagerLookupCatalog.LookupOption;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ru.nsu.tois.client.controller.manager.ManagerActionCatalog.MONTH_AGO;
import static ru.nsu.tois.client.controller.manager.ManagerActionCatalog.TODAY;
import static ru.nsu.tois.client.controller.manager.ManagerValues.toIntegerOrNull;
import static ru.nsu.tois.client.controller.manager.ManagerValues.valueToString;

final class ManagerForm {
    private final GridPane grid;
    private final Map<FieldDef, Control> formControls = new LinkedHashMap<>();
    private ManagerLookupCatalog lookups;

    ManagerForm(GridPane grid) {
        this.grid = grid;
    }

    void render(List<FieldDef> fields, ManagerLookupCatalog lookups) {
        this.lookups = lookups;
        formControls.clear();
        grid.getChildren().clear();

        for (int row = 0; row < fields.size(); row++) {
            FieldDef field = fields.get(row);
            Label label = new Label(field.label());
            label.getStyleClass().add("field-label");
            Control control = createControl(field);
            formControls.put(field, control);
            grid.add(label, 0, row);
            grid.add(control, 1, row);
            GridPane.setMargin(label, new Insets(3, 8, 3, 0));
            GridPane.setMargin(control, new Insets(3, 0, 3, 0));
        }
    }

    Map<String, Object> readFields() {
        Map<String, Object> values = new LinkedHashMap<>();
        formControls.forEach((field, control) -> {
            Object value = readValue(field, control);
            if (value != null || field.required()) {
                values.put(field.key(), value);
            }
        });
        return values;
    }

    private Control createControl(FieldDef field) {
        if (field.lookupSource() != null) {
            List<LookupOption> loadedOptions = lookups == null ? List.of() : lookups.options(field.lookupSource());
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

    void installUpdatePrefill(ManagerAction action) {
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
        Map<String, Object> pathValues = currentPathValues();
        if (pathValues.size() == 1) {
            return findDirectSelectedRow(action);
        }
        if (pathValues.isEmpty()) {
            return Map.of();
        }

        List<String> bodyKeys = action.fields().stream()
                .filter(field -> field.scopes().contains(FieldScope.BODY))
                .map(FieldDef::key)
                .toList();

        if (lookups == null) {
            return Map.of();
        }
        LookupSource source;
        if (action.pathTemplate().endsWith("/price")) {
            source = LookupSource.TRADE_POINT_PRICE;
        } else if (action.pathTemplate().endsWith("/stock")) {
            source = LookupSource.TRADE_POINT_STOCK;
        } else if (pathValues.containsKey("supplierId") && pathValues.containsKey("productId")) {
            source = LookupSource.SUPPLIER_PRODUCT;
        } else {
            return Map.of();
        }
        return lookups.options(source).stream()
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

    private Map<String, Object> currentPathValues() {
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
            checkBox.setSelected(Boolean.TRUE.equals(value) || Boolean.parseBoolean(String.valueOf(value)));
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

    RequestData buildRequest(ManagerAction action, List<Map<String, Object>> itemRows) {
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

    static Object readValue(FieldDef field, Control control) {
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

    private static Object comboValue(ComboBox<?> comboBox) {
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
    void clearInputs() {
        formControls.forEach((field, control) -> {
            if (control instanceof TextField textField) {
                textField.clear();
            } else if (control instanceof DatePicker datePicker) {
                datePicker.setValue(null);
            }
        });
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

    record RequestData(
            String path,
            Map<String, Object> queryParams,
            Map<String, Object> body
    ) {
    }}
