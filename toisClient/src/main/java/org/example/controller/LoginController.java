package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.exception.ApiException;
import org.example.http_client.DTOresponse.LoginResponse;
import org.example.model.AppContext;
import org.example.model.AppContextAware;

public class LoginController implements AppContextAware {
    private AppContext appContext;

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private void initialize() {
        loginButton.disableProperty().bind(
                loginField.textProperty().isEmpty()
                        .or(passwordField.textProperty().isEmpty())
        );
    }

    @FXML
    private void onLogin() {
        String login = loginField.getText();
        String password = passwordField.getText();

        try {
            LoginResponse response = appContext.getAuthClient().login(login, password);

            appContext.getSessionState().setToken(response.token());

            Stage currentStage = (Stage) loginField.getScene().getWindow();
            appContext.getWindowManager().openWindowForRoleAndClose(response.role(), currentStage);
        } catch (ApiException exception) {
            showError(
                    exception.getErrorResponse().getCode(),
                    exception.getErrorResponse().getMessage()
            );
        } catch (Exception exception) {
            showError("UNKNOWN_ERROR", exception.getMessage());
        }
    }

    private void showError(String code, String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка авторизации");
        alert.setHeaderText("Авторизация не выполнена");
        alert.setContentText("Код: " + code + "\nТекст: " + text);
        alert.showAndWait();
    }

    @Override
    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
    }
}
