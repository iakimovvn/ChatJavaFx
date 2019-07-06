package client;

import client.chat.ControllerChat;
import client.login.ControllerLogin;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatMain  extends Application {

    public static ControllerChat controllerChat;
    public static ControllerLogin controllerLogin;

    public static  Scene sceneLogin;
    public static Scene sceneRegistration;
    public static Scene sceneChat;

    private final int WINDOW_WIDTH = 600;
    private final int WINDOW_HEIGHT = 575;

    public static Stage primaryStage;
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        sceneInitialization();
        primaryStage.setTitle("Yakimovs Chat");
        primaryStage.setScene(sceneLogin);
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.setOnCloseRequest(windowEVENT ->{
            controllerChat.dispose();
            Platform.exit();
            System.exit(0);
        });

    }

    private void sceneInitialization(){
        try {
            FXMLLoader loaderLogin = new FXMLLoader();
            Parent rootLogin = loaderLogin.load(getClass().getResourceAsStream("login/resourcesLogin/loginPanel.fxml"));
            sceneLogin = new Scene(rootLogin,WINDOW_WIDTH,WINDOW_HEIGHT);
            controllerLogin = loaderLogin.getController();

            FXMLLoader loaderRegistration = new FXMLLoader();
            Parent rootRegistration = loaderRegistration.load(getClass().getResourceAsStream("registration/resourcesReg/registration.fxml"));
            sceneRegistration = new Scene(rootRegistration,WINDOW_WIDTH,WINDOW_HEIGHT);

            FXMLLoader loaderChat = new FXMLLoader();
            Parent rootChat = loaderChat.load(getClass().getResourceAsStream("chat/resourcesChat/chatPanel.fxml"));
            sceneChat = new Scene(rootChat,WINDOW_WIDTH,WINDOW_HEIGHT);
            controllerChat = loaderChat.getController();



        } catch (IOException e) {
        e.printStackTrace();
    }
    }


}

