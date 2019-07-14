package client;

import client.chat.ControllerChat;
import client.login.ControllerLogin;
import client.registration.RecoveryController;
import client.registration.RegController;
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
    public static RegController regController;
    public static RecoveryController recoveryController;

    public static  Scene sceneLogin;
    public static Scene sceneRegistration;
    public static Scene sceneChat;
    public static Scene sceneRecovery;

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
            regController.dispose();
            Platform.exit();
            System.exit(0);
        });

    }

    private void sceneInitialization() throws IOException{

        FXMLLoader loaderLogin = new FXMLLoader();
        Parent rootLogin = loaderLogin.load(getClass()
                .getResourceAsStream("login/resourcesLogin/loginPanel.fxml"));
        sceneLogin = new Scene(rootLogin,WINDOW_WIDTH,WINDOW_HEIGHT);
        controllerLogin = loaderLogin.getController();

        FXMLLoader loaderRegistration = new FXMLLoader();
        Parent rootRegistration = loaderRegistration.load(getClass()
                .getResourceAsStream("registration/resourcesReg/registration.fxml"));
        sceneRegistration = new Scene(rootRegistration,WINDOW_WIDTH,WINDOW_HEIGHT);
        regController = loaderRegistration.getController();

        FXMLLoader loaderChat = new FXMLLoader();
        Parent rootChat = loaderChat.load(getClass()
                .getResourceAsStream("chat/resourcesChat/chatPanel.fxml"));
        sceneChat = new Scene(rootChat,WINDOW_WIDTH,WINDOW_HEIGHT);
        controllerChat = loaderChat.getController();

        FXMLLoader loaderRecovery = new FXMLLoader();
        Parent rootRecovery = loaderRecovery.load(getClass()
                .getResourceAsStream("registration/resourcesReg/passRecovery.fxml"));
        sceneRecovery = new Scene(rootRecovery,WINDOW_WIDTH,WINDOW_HEIGHT);
        recoveryController = loaderRecovery.getController();


    }


}

