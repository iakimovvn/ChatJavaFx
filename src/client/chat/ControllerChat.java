package client.chat;

import client.ChatMain;
import client.login.ControllerLogin;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import javax.activation.MailcapCommandMap;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ControllerChat {

    public Socket socket;
    private DataInputStream in;
    public DataOutputStream out;

    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;

    private ControllerLogin controllerLogin;

    @FXML
    private TextField messageTextField;

    @FXML
    private Label nickName;

    @FXML
    private Circle circleIsInNet;

    @FXML
    private ListView <String> clientList;

    @FXML
    private VBox vBoxMessage;






    public void connect() {
        try {
            socket =new Socket(IP_ADDRESS,PORT);
//            this.isAuthorized = false;

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            controllerLogin = ChatMain.controllerLogin;



            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while(true) {
                            String str = in.readUTF();
                            if(str.startsWith("/serverclosed")) break;
                            if(str.startsWith("/authok")) {
                                controllerLogin.setAuthorized(true);
                                String[] nickArr = str.split(" ");
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        nickName.setText(nickArr[1]);
                                        circleIsInNet.setStyle("-fx-fill: green");
                                    }
                                });
                                break;
                            }else{
                                controllerLogin.writeToLabelNotIdentification(str);
                            }
                        }
                        while(true) {
                            String str = in.readUTF();
                            if (str.startsWith("/serverclosed")) break;
                            if (str.startsWith("/clientlist")) {
                                String[] tokens = str.split(" ");
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        clientList.getItems().clear();
                                        for (int i = 1; i < tokens.length; i++) {
                                            clientList.getItems().add(tokens[i]);
                                        }
                                    }
                                });
                            } else {
                                inputToVBoxMessage(str + "\n");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //setAuthorized(false);
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendMsg(){

        try {
            out.writeUTF(messageTextField.getText());
            messageTextField.clear();
            messageTextField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void inputToVBoxMessage(String msg){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Label msgLbl = new Label();
                msgLbl.setText(msg);
                vBoxMessage.getChildren().add(msgLbl);
            }
        });

    }

    public void sendMsgFromString(String str){
        try {
            out.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
