package org.example.model;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WindowManager {
    private static final String SUPERADMIN_ROLE = "SUPERADMIN_ROLE";
    private static final String TRADE_MANAGER_ROLE = "TRADE_MANAGER_ROLE";
    private static final String SELLER_ROLE = "SELLER_ROLE";

    private static final int ROLE_WINDOW_WIDTH = 1100;
    private static final int ROLE_WINDOW_HEIGHT = 740;
    private static final int ROLE_WINDOW_MIN_WIDTH = 960;
    private static final int ROLE_WINDOW_MIN_HEIGHT = 640;

    private final AppContext appContext;

    public WindowManager(AppContext appContext) {
        this.appContext = appContext;
    }

    public void openLoginWindow(Stage stage) {
        openInStage(
                stage,
                "/login.fxml",
                "Авторизация",
                460,
                340,
                true,
                420,
                320
        );
    }

    public void openWindowForRoleAndClose(String role, Stage currentStage) {
        switch (role) {
            case SUPERADMIN_ROLE -> openSuperadminWindow();
            case TRADE_MANAGER_ROLE -> openTradeManagerWindow();
            case SELLER_ROLE -> openSellerWindow();
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        }

        currentStage.close();
    }

    private void openSuperadminWindow() {
        openInStage(
                new Stage(),
                "/admin-control-panel.fxml",
                "Администрирование",
                ROLE_WINDOW_WIDTH,
                ROLE_WINDOW_HEIGHT,
                true,
                ROLE_WINDOW_MIN_WIDTH,
                ROLE_WINDOW_MIN_HEIGHT
        );
    }

    private void openTradeManagerWindow() {
        openInStage(
                new Stage(),
                "/trade-manager.fxml",
                "Менеджер торговли",
                ROLE_WINDOW_WIDTH,
                ROLE_WINDOW_HEIGHT,
                true,
                ROLE_WINDOW_MIN_WIDTH,
                ROLE_WINDOW_MIN_HEIGHT
        );
    }

    private void openSellerWindow() {
        openInStage(
                new Stage(),
                "/seller.fxml",
                "Продавец",
                ROLE_WINDOW_WIDTH,
                ROLE_WINDOW_HEIGHT,
                true,
                ROLE_WINDOW_MIN_WIDTH,
                ROLE_WINDOW_MIN_HEIGHT
        );
    }

    private void openInStage(
            Stage stage,
            String fxmlPath,
            String title,
            int width,
            int height,
            boolean resizable,
            int minWidth,
            int minHeight
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxmlPath)
            );

            Parent root = loader.load();

            Object controller = loader.getController();

            if (controller instanceof AppContextAware aware) {
                aware.setAppContext(appContext);
            }

            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(
                    getClass().getResource("/styles/app.css").toExternalForm()
            );

            stage.setTitle(title);
            stage.setScene(scene);
            stage.setResizable(resizable);
            stage.setMinWidth(minWidth);
            stage.setMinHeight(minHeight);
            stage.show();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
