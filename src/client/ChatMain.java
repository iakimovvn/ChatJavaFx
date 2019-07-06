package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;

public class ChatMain  extends Application {

    Controller controller;
    public static Stage primaryStage;
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getResourceAsStream("resourcesMain/loginPanel.fxml"));
        this.controller = loader.getController();
        primaryStage.setTitle("Yakimovs Chat");
        primaryStage.setScene(new Scene(root,600,575));
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.setOnCloseRequest(windowEVENT ->{
            controller.dispose();
            Platform.exit();
            System.exit(0);
        });

    }
}

