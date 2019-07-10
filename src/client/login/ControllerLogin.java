package client.login;

import client.ChatMain;
import client.chat.ControllerChat;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ControllerLogin {


    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private ControllerChat controllerChat;

    @FXML
    private Label labelNotIdentification;

    @FXML
    private TextField loginField;

    @FXML
    private TextField passwordField;



    public void setAuthorized(boolean isAuthorized){
        if(isAuthorized){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    ChatMain.primaryStage.setScene(ChatMain.sceneChat);
                }
            });
        }else{
            ChatMain.primaryStage.setScene(ChatMain.sceneLogin);
        }
    }

    public void tryToAuth() {

        controllerChat = ChatMain.controllerChat;
        socket = controllerChat.socket;

        if(socket == null || socket.isClosed()) {
            controllerChat.connect();
        }
        String login = loginField.getText();
        String password = passwordField.getText();
        controllerChat.writeLoginPassword(login, password);
        controllerChat.sendMsgFromString("/auth " + login + " " + password);
        loginField.clear();
        passwordField.clear();
    }

    public void writeToLabelNotIdentification(String str){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                labelNotIdentification.setText(str);                                    }
        });
    }


    public void registration(){
        ChatMain.primaryStage.setScene(ChatMain.sceneRegistration);
    }

    public void recoveryPass(){
        ChatMain.primaryStage.setScene(ChatMain.sceneRecovery);
    }


}
