package client.chat;

import client.ChatMain;
import client.login.ControllerLogin;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import sun.rmi.runtime.RuntimeUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class ControllerChat {

    public Socket socket;
    private DataInputStream in;
    public DataOutputStream out;

    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;

    private ArrayList<PrivateStage> privateStageArrayList;

    private ControllerLogin controllerLogin;

    @FXML
    private TextField messageTextField;

    @FXML
    private Label nickName;

    @FXML
    private Circle circleIsInNet;

    @FXML
    ListView <String> clientList;

    @FXML
    private VBox vBoxMessage;






    public void connect() {
        try {
            socket =new Socket(IP_ADDRESS,PORT);
//            this.isAuthorized = false;

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            controllerLogin = ChatMain.controllerLogin;

            privateStageArrayList = new ArrayList<>();



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
                            } else if (str.startsWith("/w")) {
                                getPrivateMessage(str);
                            } else if(str.startsWith("/systemmsg")){
                                getSystemMessage(str);
                            }
                            else {
                                inputToVBoxMessage(str );
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

    private void getSystemMessage(String msg){
        String[] msgArr = msg.split(" ",2);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                vBoxMessage.getChildren().add(new SystemMessageHBox(msgArr[1]));
            }
        });
    }

    private void inputToVBoxMessage(String msg){
        String[] msgArr = msg.split(" ",2);
        if(msgArr[0].equals(nickName.getText())) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    vBoxMessage.getChildren().add(new MyMessageHBox(msgArr[0], makeMessageForLabel(msgArr[1])));
                }
            });
        }else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    vBoxMessage.getChildren().add(new OtherMessageHBox(msgArr[0], makeMessageForLabel(msgArr[1])));
                }
            });
        }

    }

    public void sendMsgFromString(String str){
        try {
            out.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createPrivateChatFromClick (MouseEvent mouseEvent){
        if(mouseEvent.getClickCount() == 2){
            String nickTo = clientList.getSelectionModel().getSelectedItem();
            if(nickName.getText().equals(nickTo)){
            }else{
                if(!isTherePrivateWithNickTo(nickTo)){
                    createPrivateChat(nickTo);
                }else{
                    makeTop(nickTo);
                }
            }
        }
    }

    private boolean isTherePrivateWithNickTo(String nickTo){
        boolean isThere = false;
        Iterator <PrivateStage>iterator = privateStageArrayList.iterator();

        while(iterator.hasNext()){
            PrivateStage ps = iterator.next();
            if(ps.privateNickTo.equals(nickTo)){
                isThere = true;
                break;
            }
        }
        return isThere;
    }

    private void getPrivateMessage(String str){
        String[] privateMsgArr = str.split(" ",4);
        if(!privateMsgArr[1].equals(nickName.getText()) && !isTherePrivateWithNickTo(privateMsgArr[1])){
            createPrivateChat(privateMsgArr[1]);
        }else{
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    makeTop(privateMsgArr[1]);
                }
            });
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Iterator <PrivateStage>iterator = privateStageArrayList.iterator();
        while(iterator.hasNext()){
            PrivateStage ps = iterator.next();
            if(ps.privateNickTo.equals(privateMsgArr[1])){
                if(str.startsWith("/wsystemmsg")){
                    ps.controllerPrivateChat.addToVBoxMessage(new SystemMessageHBox(
                            privateMsgArr[2]+" "+privateMsgArr[3]));
                }
                else if (privateMsgArr[2].equals(nickName.getText())) {
                    ps.controllerPrivateChat.addToVBoxMessage(new MyMessageHBox(privateMsgArr[2],privateMsgArr[3]));
                    break;
                }else{
                    ps.controllerPrivateChat.addToVBoxMessage(new OtherMessageHBox(privateMsgArr[2],privateMsgArr[3]));
                    break;
                }
            }
        }
    }

    private void makeTop(String nickTo){
        Iterator<PrivateStage> iterator = privateStageArrayList.iterator();
        while (iterator.hasNext()){
            PrivateStage ps =iterator.next();
            if(ps.privateNickTo.equals(nickTo)){
                ps.setAlwaysOnTop(true);
                ps.setAlwaysOnTop(false);
                break;
            }
        }
    }


    private void createPrivateChat(String nickTo){

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                PrivateStage ps = new PrivateStage(nickTo);
                privateStageArrayList.add(ps);
                ps.controllerPrivateChat.setLabelNickTo(nickTo);
                ps.show();

            }
        });
    }

    private String makeMessageForLabel(String msg){
        final int NUMBER_IF_SINGS = 37;
        if(msg.length() > NUMBER_IF_SINGS){
            String[] msgArr = msg.split(" ");
            StringBuilder stringBuilder = new StringBuilder();
            int singsNow = 0;
            for (String word : msgArr) {
                if(singsNow+word.length() < NUMBER_IF_SINGS){
                    stringBuilder.append(word);
                    stringBuilder.append(" ");
                    singsNow+=word.length()+1;
                }else{
                    stringBuilder.append("\n");
                    stringBuilder.append(word);
                    singsNow = 0;
                }

            }
            msg = stringBuilder.toString();

        }
        return msg;

    }

    public void deleteFromPrivateStageArrayList(PrivateStage ps){
        privateStageArrayList.remove(ps);
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
