package ru.nsu.tois.client;

import javafx.application.Application;
import javafx.stage.Stage;
import ru.nsu.tois.client.session.AppContext;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        try {
            AppContext appContext = new AppContext();
            appContext.getWindowManager().openLoginWindow(stage);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
