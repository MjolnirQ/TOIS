package ru.nsu.tois.client.controller.manager;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import ru.nsu.tois.client.api.ApiException;
import ru.nsu.tois.client.api.ManagerClient;
import ru.nsu.tois.client.controller.AppContextAware;
import ru.nsu.tois.client.controller.manager.ManagerAction.FieldDef;
import ru.nsu.tois.client.controller.manager.ManagerAction.ItemSpec;
import ru.nsu.tois.client.controller.manager.ManagerForm.RequestData;
import ru.nsu.tois.client.session.AppContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ru.nsu.tois.client.controller.manager.ManagerValues.toIntegerOrNull;
import static ru.nsu.tois.client.controller.manager.ManagerValues.valueToString;

public class TradeManagerController implements AppContextAware {
    private final ObservableList<ManagerAction> currentActions = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> itemRows = FXCollections.observableArrayList();
    private final List<ManagerAction> actions = ManagerActionCatalog.actions();

    private AppContext appContext;
    private ManagerLookupCatalog lookups;
    private ManagerForm actionForm;
    private ManagerForm itemForm;
    private ManagerResultTable results;
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
    private Button executeButton;

    @FXML
    private Button refreshLookupsButton;

    @FXML
    private void initialize() {
        actionForm = new ManagerForm(formGrid);
        itemForm = new ManagerForm(itemFormGrid);
        results = new ManagerResultTable(resultTable, statusLabel);

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
        results.clear("Результат операции");

        if (!groupComboBox.getItems().isEmpty()) {
            groupComboBox.getSelectionModel().selectFirst();
        }
    }

    @Override
    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
        reloadLookups();
    }

    @FXML
    private void onRefreshLookups() {
        reloadLookups();
    }

    @FXML
    private void onExecuteAction() {
        if (groupComboBox.isDisabled()) {
            return;
        }
        if (selectedAction == null) {
            showWarning("Операция не выбрана", "Выберите операцию слева.");
            return;
        }

        ManagerAction action = selectedAction;
        RequestData request;
        try {
            request = actionForm.buildRequest(action, itemRows);
        } catch (IllegalArgumentException exception) {
            showWarning("Проверьте данные", exception.getMessage());
            return;
        }

        ManagerClient client = appContext.getManagerClient();
        String accessToken = token();
        Task<Object> task = new Task<>() {
            @Override
            protected Object call() {
                return client.execute(
                        accessToken,
                        action.method(),
                        request.path(),
                        request.queryParams(),
                        request.body()
                );
            }
        };
        task.setOnSucceeded(event -> {
            Object response = task.getValue();
            results.render(response, lookups);
            if (!(response instanceof Map<?, ?> map && map.containsKey("totalCount"))) {
                statusLabel.setText("Выполнено: " + action.title());
            }
        });
        runTask(task, "Выполняется: " + action.title());
    }

    @FXML
    private void onAddItem() {
        if (selectedAction == null || selectedAction.itemSpec() == null) {
            return;
        }

        try {
            itemRows.add(itemForm.readFields());
            renderItemTable();
            itemForm.clearInputs();
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
        itemRows.clear();
        actionTitleLabel.setText(action == null ? "Операция не выбрана" : action.title());
        statusLabel.setText("");
        results.clear("Результат операции");

        actionForm.render(action == null ? List.of() : action.fields(), lookups);
        renderItemEditor(action == null ? null : action.itemSpec());
        if (action != null) {
            actionForm.installUpdatePrefill(action);
        }
    }

    private void renderItemEditor(ItemSpec itemSpec) {
        boolean hasItems = itemSpec != null;
        itemEditorBox.setVisible(hasItems);
        itemEditorBox.setManaged(hasItems);
        for (Button button : List.of(addItemButton, removeItemButton, clearItemsButton)) {
            button.setVisible(hasItems);
            button.setManaged(hasItems);
        }

        itemEditorLabel.setText(hasItems ? itemSpec.title() : "");
        itemForm.render(hasItems ? itemSpec.fields() : List.of(), lookups);
        renderItemTable();
    }

    private void reloadLookups() {
        if (appContext == null || groupComboBox.isDisabled()) {
            return;
        }

        ManagerLookupCatalog catalog = new ManagerLookupCatalog(appContext.getManagerClient(), token());
        Task<ManagerLookupCatalog> task = new Task<>() {
            @Override
            protected ManagerLookupCatalog call() {
                catalog.reload();
                return catalog;
            }
        };
        task.setOnSucceeded(event -> {
            lookups = task.getValue();
            renderActionForm(selectedAction);
            statusLabel.setText("Справочники обновлены");
        });
        runTask(task, "Загрузка справочников…");
    }

    private void runTask(Task<?> task, String progressText) {
        setBusy(true);
        statusLabel.setText(progressText);
        task.runningProperty().addListener((observable, wasRunning, running) -> {
            if (!running) {
                setBusy(false);
            }
        });
        task.setOnFailed(event -> {
            statusLabel.setText("Операция не выполнена");
            showFailure(task.getException());
        });
        Thread worker = new Thread(task, "tois-manager-request");
        worker.setDaemon(true);
        worker.start();
    }

    private void setBusy(boolean busy) {
        groupComboBox.setDisable(busy);
        actionListView.setDisable(busy);
        formGrid.setDisable(busy);
        itemEditorBox.setDisable(busy);
        executeButton.setDisable(busy);
        refreshLookupsButton.setDisable(busy);
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

        String label = lookups == null ? null : lookups.findLabel(field.lookupSource(), toIntegerOrNull(value));
        return Objects.requireNonNullElse(label, valueToString(value));
    }

    private void showFailure(Throwable exception) {
        if (exception instanceof ApiException apiException) {
            showError(apiException.getErrorResponse().getCode(), apiException.getErrorResponse().getMessage());
        } else {
            showError("UNKNOWN_ERROR", exception.getMessage());
        }
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
}
