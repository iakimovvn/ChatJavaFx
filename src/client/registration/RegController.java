package client.registration;

import client.ChatMain;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegController {

    @FXML
    Label registrationLbl;
    @FXML
    Label errMsg;

    @FXML
    TextField loginField;

    @FXML
    TextField passField;

    @FXML
    TextField rePassField;

    @FXML
    TextField nickField;

    @FXML
    TextField controlWord;

    @FXML
    Button regBtn;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;



    public void toRegistration(){
        if(!isTextFieldValid(loginField.getText()) || !isTextFieldValid(nickField.getText())
                || !isTextFieldValid(controlWord.getText())){
            errMsg.setText("Заполните все поля(4-20 символов)");
        } else if(!isPasswordValidSymbols(passField.getText())){
            errMsg.setText("Пароль неверного формата A-z,0-1,_,-");
        } else if(!isPasswordBigLetter(passField.getText())){
            errMsg.setText("Должны быть заглавные буквыт");
        } else if (!isPasswordNumbers(passField.getText())) {
            errMsg.setText("Должны быть цифры");
        } else if (!passField.getText().equals(rePassField.getText())) {
            errMsg.setText("Пароли не совпадают");
        } else {
            if(socket == null || socket.isClosed()){
                connectReg();
            }
            sendToServer("/registration "+loginField.getText()+" "+passField.getText()+" "
                        +nickField.getText()+" "+controlWord.getText());
        }

    }

    public void sendToServer(String str){
        try {
            out.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connectReg(){
        try {
            socket = new Socket(IP_ADDRESS,PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String str = in.readUTF();
                            if(str.startsWith("/serverclosed")) break;
                            if(str.startsWith("/regok")){
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        registrationLbl.setText(" Вы зарегистрированы");
                                        errMsg.setText("");
                                        loginField.clear();
                                        loginField.requestFocus();
                                        passField.clear();
                                        rePassField.clear();
                                        nickField.clear();
                                        controlWord.clear();
                                    }
                                });
                                sendToServer("/end");
                                break;
                            }else if (str.startsWith("/recovery")){
                                ChatMain.recoveryController.readResult(str);
                                break;
                            }
                            else{
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        errMsg.setText(str);
                                    }
                                });
                            }

                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    }finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isTextFieldValid(String str){

            Pattern patternSymbols = Pattern.compile("^[A-z,0-9,_,\\-]{4,20}$");
            Matcher matcher = patternSymbols.matcher(str);
           return matcher.matches();
    }

    private static boolean isPasswordValidSymbols(String password) {
        Pattern patternSymbols = Pattern.compile(
                "^[A-z,0-9,_,\\-]{5,20}$");
        Matcher matcher = patternSymbols.matcher(password);
        return matcher.matches();
    }

    private static boolean isPasswordBigLetter(String password){
        Pattern patternSymbols = Pattern.compile(
                ".*[A-Z]+.*");
        Matcher matcher = patternSymbols.matcher(password);
        return matcher.matches();
    }

    private static boolean isPasswordNumbers(String password){
        Pattern patternSymbols = Pattern.compile(
                ".*\\d+.*");
        Matcher matcher = patternSymbols.matcher(password);
        return matcher.matches();
    }

    public void backToLoginScene(){
        ChatMain.primaryStage.setScene(ChatMain.sceneLogin);

    }

    public void dispose(){
        try {
            if(out != null) {
                System.out.println("Close");
                out.writeUTF("/end");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
