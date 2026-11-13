package org.example.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.example.exception.ApiException;
import org.example.http_client.DTOresponse.PermissionResponse;
import org.example.http_client.DTOresponse.RoleResponse;
import org.example.http_client.DTOresponse.UserAccountResponse;
import org.example.model.AppContext;
import org.example.model.AppContextAware;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class AdminControlPanelController implements AppContextAware {
    private AppContext appContext;

    @FXML
    private ListView<RoleResponse> rolesListView;

    @FXML
    private ListView<PermissionResponse> permissionsListView;

    @FXML
    private ListView<UserAccountResponse> userAccountsListView;

    @FXML
    private void initialize() {
        rolesListView.setPlaceholder(new Label("Роли не загружены"));
        permissionsListView.setPlaceholder(new Label("Permissions не загружены"));
        userAccountsListView.setPlaceholder(new Label("Учетные записи не загружены"));

        rolesListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(RoleResponse role, boolean empty) {
                super.updateItem(role, empty);
                setText(empty || role == null ? null : role.id() + " | " + role.name());
            }
        });

        permissionsListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(PermissionResponse permission, boolean empty) {
                super.updateItem(permission, empty);
                setText(empty || permission == null ? null : permission.id() + " | " + permission.code());
            }
        });

        userAccountsListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(UserAccountResponse userAccount, boolean empty) {
                super.updateItem(userAccount, empty);
                if (empty || userAccount == null) {
                    setText(null);
                    return;
                }

                setText(userAccount.id() + " | " + userAccount.login()
                        + " | " + roleName(userAccount.roleId())
                        + " | employee " + userAccount.employeeId()
                        + " | " + (Boolean.TRUE.equals(userAccount.isActive()) ? "active" : "disabled"));
            }
        });
    }

    @Override
    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
        loadData();
    }

    @FXML
    private void onAddRole() {
        openRoleDialog(null).ifPresent(result -> runAdminAction(() -> {
            appContext.getAdminClient().createRole(token(), result.name(), result.permissionIds());
            loadData();
        }));
    }

    @FXML
    private void onEditRole() {
        RoleResponse selectedRole = rolesListView.getSelectionModel().getSelectedItem();
        if (selectedRole == null) {
            showWarning("Роль не выбрана", "Выберите роль для редактирования.");
            return;
        }

        openRoleDialog(selectedRole).ifPresent(result -> runAdminAction(() -> {
            appContext.getAdminClient().updateRole(token(), selectedRole.id(), result.name(), result.permissionIds());
            loadData();
        }));
    }

    @FXML
    private void onDeleteRole() {
        RoleResponse selectedRole = rolesListView.getSelectionModel().getSelectedItem();
        if (selectedRole == null) {
            showWarning("Роль не выбрана", "Выберите роль для удаления.");
            return;
        }

        if (!confirm("Удалить роль?", "Роль будет удалена: " + selectedRole.name())) {
            return;
        }

        runAdminAction(() -> {
            appContext.getAdminClient().deleteRole(token(), selectedRole.id());
            loadData();
        });
    }

    @FXML
    private void onAddPermission() {
        openPermissionDialog(null).ifPresent(code -> runAdminAction(() -> {
            appContext.getAdminClient().createPermission(token(), code);
            loadData();
        }));
    }

    @FXML
    private void onEditPermission() {
        PermissionResponse selectedPermission = permissionsListView.getSelectionModel().getSelectedItem();
        if (selectedPermission == null) {
            showWarning("Permission не выбран", "Выберите permission для редактирования.");
            return;
        }

        openPermissionDialog(selectedPermission).ifPresent(code -> runAdminAction(() -> {
            appContext.getAdminClient().updatePermission(token(), selectedPermission.id(), code);
            loadData();
        }));
    }

    @FXML
    private void onDeletePermission() {
        PermissionResponse selectedPermission = permissionsListView.getSelectionModel().getSelectedItem();
        if (selectedPermission == null) {
            showWarning("Permission не выбран", "Выберите permission для удаления.");
            return;
        }

        if (!confirm("Удалить permission?", "Permission будет удален: " + selectedPermission.code())) {
            return;
        }

        runAdminAction(() -> {
            appContext.getAdminClient().deletePermission(token(), selectedPermission.id());
            loadData();
        });
    }

    @FXML
    private void onAddUserAccount() {
        openUserAccountDialog(null).ifPresent(result -> runAdminAction(() -> {
            appContext.getAdminClient().createUserAccount(
                    token(),
                    result.login(),
                    result.password(),
                    result.roleId(),
                    result.employeeId(),
                    result.isActive()
            );
            loadData();
        }));
    }

    @FXML
    private void onEditUserAccount() {
        UserAccountResponse selectedUserAccount = userAccountsListView.getSelectionModel().getSelectedItem();
        if (selectedUserAccount == null) {
            showWarning("Учетная запись не выбрана", "Выберите учетную запись для редактирования.");
            return;
        }

        openUserAccountDialog(selectedUserAccount).ifPresent(result -> runAdminAction(() -> {
            appContext.getAdminClient().updateUserAccount(
                    token(),
                    selectedUserAccount.id(),
                    result.login(),
                    result.password(),
                    result.roleId(),
                    result.employeeId(),
                    result.isActive()
            );
            loadData();
        }));
    }

    @FXML
    private void onDeleteUserAccount() {
        UserAccountResponse selectedUserAccount = userAccountsListView.getSelectionModel().getSelectedItem();
        if (selectedUserAccount == null) {
            showWarning("Учетная запись не выбрана", "Выберите учетную запись для удаления.");
            return;
        }

        if (!confirm("Удалить учетную запись?", "Учетная запись будет удалена: " + selectedUserAccount.login())) {
            return;
        }

        runAdminAction(() -> {
            appContext.getAdminClient().deleteUserAccount(token(), selectedUserAccount.id());
            loadData();
        });
    }

    @FXML
    private void onToggleUserAccountActive() {
        UserAccountResponse selectedUserAccount = userAccountsListView.getSelectionModel().getSelectedItem();
        if (selectedUserAccount == null) {
            showWarning("Учетная запись не выбрана", "Выберите учетную запись.");
            return;
        }

        boolean newActiveValue = !Boolean.TRUE.equals(selectedUserAccount.isActive());

        if (!confirm(
                "Изменить статус учетной записи?",
                "Учетная запись " + selectedUserAccount.login()
                        + " будет "
                        + (newActiveValue ? "активирована." : "деактивирована.")
        )) {
            return;
        }

        runAdminAction(() -> {
            appContext.getAdminClient().updateUserAccountActive(
                    token(),
                    selectedUserAccount.id(),
                    newActiveValue
            );
            loadData();
        });
    }

    @FXML
    private void onEditUserAccountCredentials() {
        UserAccountResponse selectedUserAccount = userAccountsListView.getSelectionModel().getSelectedItem();
        if (selectedUserAccount == null) {
            showWarning("Учетная запись не выбрана", "Выберите учетную запись.");
            return;
        }

        Dialog<UserAccountCredentialsDialogResult> dialog = new Dialog<>();
        dialog.setTitle("Изменить логин/пароль");
        dialog.setHeaderText(selectedUserAccount.login());
        dialog.initOwner(userAccountsListView.getScene().getWindow());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField loginField = new TextField(selectedUserAccount.login());
        loginField.setPromptText("Логин");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Оставьте пустым, чтобы не менять пароль");

        VBox content = new VBox(10,
                new Label("Логин"),
                loginField,
                new Label("Новый пароль"),
                passwordField
        );
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.disableProperty().bind(loginField.textProperty().isEmpty());

        dialog.setResultConverter(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return null;
            }

            return new UserAccountCredentialsDialogResult(
                    loginField.getText().trim(),
                    passwordField.getText()
            );
        });

        dialog.showAndWait().ifPresent(result -> runAdminAction(() -> {
            appContext.getAdminClient().updateUserAccountCredentials(
                    token(),
                    selectedUserAccount.id(),
                    result.login(),
                    result.password()
            );
            loadData();
        }));
    }

    private void loadData() {
        runAdminAction(() -> {
            List<RoleResponse> roles = appContext.getAdminClient().findAllRoles(token());
            List<PermissionResponse> permissions = appContext.getAdminClient().findAllPermissions(token());
            rolesListView.setItems(FXCollections.observableArrayList(roles));
            permissionsListView.setItems(FXCollections.observableArrayList(permissions));
            loadUserAccounts();
        });
    }

    private void loadUserAccounts() {
        try {
            List<UserAccountResponse> userAccounts = appContext.getAdminClient().findAllUserAccounts(token());
            userAccountsListView.setItems(FXCollections.observableArrayList(userAccounts));
        } catch (ApiException exception) {
            userAccountsListView.setItems(FXCollections.emptyObservableList());
            userAccountsListView.setPlaceholder(new Label("Нет права admin.users.read"));
        }
    }

    private Optional<RoleDialogResult> openRoleDialog(RoleResponse role) {
        Dialog<RoleDialogResult> dialog = new Dialog<>();
        dialog.setTitle(role == null ? "Добавить роль" : "Редактировать роль");
        dialog.setHeaderText(role == null ? "Новая роль" : role.name());
        dialog.initOwner(rolesListView.getScene().getWindow());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField(role == null ? "" : role.name());
        nameField.setPromptText("Название роли");

        VBox permissionsBox = new VBox(6);
        Set<Integer> selectedPermissionIds = role == null || role.permissionIds() == null
                ? Set.of()
                : role.permissionIds();

        for (PermissionResponse permission : permissionsListView.getItems()) {
            CheckBox checkBox = new CheckBox(permission.code());
            checkBox.setUserData(permission.id());
            checkBox.setSelected(selectedPermissionIds.contains(permission.id()));
            permissionsBox.getChildren().add(checkBox);
        }

        ScrollPane scrollPane = new ScrollPane(permissionsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(320);

        VBox content = new VBox(10,
                new Label("Название"),
                nameField,
                new Label("Permissions"),
                scrollPane
        );
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.disableProperty().bind(nameField.textProperty().isEmpty());

        dialog.setResultConverter(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return null;
            }

            Set<Integer> permissionIds = new HashSet<>();
            for (Node node : permissionsBox.getChildren()) {
                if (node instanceof CheckBox checkBox && checkBox.isSelected()) {
                    permissionIds.add((Integer) checkBox.getUserData());
                }
            }

            return new RoleDialogResult(nameField.getText().trim(), permissionIds);
        });

        return dialog.showAndWait();
    }

    private Optional<UserAccountDialogResult> openUserAccountDialog(UserAccountResponse userAccount) {
        Dialog<UserAccountDialogResult> dialog = new Dialog<>();
        dialog.setTitle(userAccount == null ? "Добавить учетную запись" : "Редактировать учетную запись");
        dialog.setHeaderText(userAccount == null ? "Новая учетная запись" : userAccount.login());
        dialog.initOwner(userAccountsListView.getScene().getWindow());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField loginField = new TextField(userAccount == null ? "" : userAccount.login());
        loginField.setPromptText("Логин");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(userAccount == null ? "Пароль" : "Новый пароль");

        ComboBox<RoleResponse> roleComboBox = new ComboBox<>(rolesListView.getItems());
        roleComboBox.setCellFactory(listView -> roleCell());
        roleComboBox.setButtonCell(roleCell());
        roleComboBox.getSelectionModel().select(
                rolesListView.getItems().stream()
                        .filter(role -> userAccount != null && role.id().equals(userAccount.roleId()))
                        .findFirst()
                        .orElse(null)
        );
        if (roleComboBox.getSelectionModel().isEmpty() && !roleComboBox.getItems().isEmpty()) {
            roleComboBox.getSelectionModel().selectFirst();
        }

        TextField employeeIdField = new TextField(userAccount == null ? "" : String.valueOf(userAccount.employeeId()));
        employeeIdField.setPromptText("ID сотрудника");

        CheckBox activeCheckBox = new CheckBox("Активна");
        activeCheckBox.setSelected(userAccount == null || Boolean.TRUE.equals(userAccount.isActive()));

        VBox content = new VBox(10,
                new Label("Логин"),
                loginField,
                new Label("Пароль"),
                passwordField,
                new Label("Роль"),
                roleComboBox,
                new Label("Сотрудник"),
                employeeIdField,
                activeCheckBox
        );
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        if (userAccount == null) {
            okButton.disableProperty().bind(
                    loginField.textProperty().isEmpty()
                            .or(passwordField.textProperty().isEmpty())
                            .or(roleComboBox.valueProperty().isNull())
                            .or(employeeIdField.textProperty().isEmpty())
            );
        } else {
            okButton.disableProperty().bind(
                    loginField.textProperty().isEmpty()
                            .or(roleComboBox.valueProperty().isNull())
                            .or(employeeIdField.textProperty().isEmpty())
            );
        }

        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                parseInteger(employeeIdField.getText(), "ID сотрудника");
            } catch (IllegalArgumentException exception) {
                showWarning("Некорректные данные", exception.getMessage());
                event.consume();
            }
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return null;
            }

            return new UserAccountDialogResult(
                    loginField.getText().trim(),
                    passwordField.getText(),
                    roleComboBox.getValue().id(),
                    parseInteger(employeeIdField.getText(), "ID сотрудника"),
                    activeCheckBox.isSelected()
            );
        });

        return dialog.showAndWait();
    }

    private Optional<String> openPermissionDialog(PermissionResponse permission) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(permission == null ? "Добавить permission" : "Редактировать permission");
        dialog.setHeaderText(permission == null ? "Новый permission" : permission.code());
        dialog.initOwner(permissionsListView.getScene().getWindow());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField codeField = new TextField(permission == null ? "" : permission.code());
        codeField.setPromptText("permission code");

        VBox content = new VBox(10, new Label("Code"), codeField);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.disableProperty().bind(codeField.textProperty().isEmpty());

        dialog.setResultConverter(buttonType -> buttonType == ButtonType.OK ? codeField.getText().trim() : null);

        return dialog.showAndWait();
    }

    private void runAdminAction(Runnable action) {
        try {
            action.run();
        } catch (ApiException exception) {
            showError(exception.getErrorResponse().getCode(), exception.getErrorResponse().getMessage());
        } catch (Exception exception) {
            showError("UNKNOWN_ERROR", exception.getMessage());
        }
    }

    private boolean confirm(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(text);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void showWarning(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(text);
        alert.showAndWait();
    }

    private void showError(String code, String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка панели управления");
        alert.setHeaderText("Операция не выполнена");
        alert.setContentText("Код: " + code + "\nТекст: " + text);
        alert.showAndWait();
    }

    private ListCell<RoleResponse> roleCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(RoleResponse role, boolean empty) {
                super.updateItem(role, empty);
                setText(empty || role == null ? null : role.id() + " | " + role.name());
            }
        };
    }

    private String roleName(Integer roleId) {
        if (roleId == null || rolesListView.getItems() == null) {
            return "role " + roleId;
        }

        return rolesListView.getItems().stream()
                .filter(role -> roleId.equals(role.id()))
                .map(RoleResponse::name)
                .findFirst()
                .orElse("role " + roleId);
    }

    private Integer parseInteger(String value, String fieldName) {
        try {
            return Integer.valueOf(value.trim());
        } catch (Exception exception) {
            throw new IllegalArgumentException(fieldName + " должен быть целым числом.");
        }
    }

    private String token() {
        return appContext.getSessionState().getToken();
    }

    private record RoleDialogResult(
            String name,
            Set<Integer> permissionIds
    ) {
    }

    private record UserAccountDialogResult(
            String login,
            String password,
            Integer roleId,
            Integer employeeId,
            Boolean isActive
    ) {
    }

    private record UserAccountCredentialsDialogResult(
            String login,
            String password
    ) {
    }
}
