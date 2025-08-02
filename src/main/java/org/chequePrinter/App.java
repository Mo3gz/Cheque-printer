package org.chequePrinter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import org.chequePrinter.service.DatabaseService;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        DatabaseService.initializeDatabase();
        Parent root = FXMLLoader.load(getClass().getResource("/org/chequePrinter/view/ChequeView.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Cheque Printer");
        stage.setWidth(1024);
        stage.setHeight(768);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
