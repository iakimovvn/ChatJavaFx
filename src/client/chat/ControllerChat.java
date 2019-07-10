package client.chat;

import client.ChatMain;
import client.login.ControllerLogin;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class ControllerChat {

    public Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;

    private ArrayList<PrivateStage> privateStageArrayList;
//    private ArrayList<PrivateStage> deletedPrivateStageArrayList;

    private ControllerLogin controllerLogin;

    private boolean isLogin = false;
    private String login;
    private String password;

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

    @FXML
    private ScrollPane scrollPaneMsg;

    @FXML
    private Button btmSend;






    public void connect() {
        try {

            socket =new Socket(IP_ADDRESS,PORT);

//            this.isAuthorized = false;

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            controllerLogin = ChatMain.controllerLogin;

            privateStageArrayList = new ArrayList<>();
//            deletedPrivateStageArrayList = new ArrayList<>();

            reLoginAfterCrashServer();
            setDisableBtmAndField(false);


            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while(true) {
                            String str = in.readUTF();
                            if(str.startsWith("/serverclosed")) break;
                            if(str.startsWith("/authok")) {
                                if(!isLogin) {
                                    controllerLogin.setAuthorized(true);
                                    String[] nickArr = str.split(" ");
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            nickName.setText(nickArr[1]);
//                                            circleIsInNet.setStyle("-fx-fill: green");
                                        }
                                    });
                                }
                                isLogin = true;
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
                            } else {
                                inputToVBoxMessage(str );
                            }
                        }
                    }catch (EOFException e){
                        setDisableBtmAndField(true);
                        setTimeOut(ControllerChat.this,2000);
                        getSystemMessage("Сервер упал. Ожидание подключения");
                        System.out.println("Ошибка чтения");
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                    finally {
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
            setDisableBtmAndField(true);
            setTimeOut(ControllerChat.this,2000);
            System.out.println("Попытка подключения");
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
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                vBoxMessage.getChildren().add(new SystemMessageHBox(msg));
            }
        });
    }

    private void inputToVBoxMessage(String msg){
        String[] msgArr = msg.split(" ",2);
        if(msgArr[0].equals("/systemmsg")){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    vBoxMessage.getChildren().add(new SystemMessageHBox(msgArr[1]));
                }
            });
        } else if(msgArr[0].equals(nickName.getText())) {
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
        scrollPaneMsg.vvalueProperty().bind(vBoxMessage.heightProperty());

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
                if(!isTherePrivateWithNickTo(privateStageArrayList,nickTo)){
                    createPrivateChat(nickTo);
                }else{
                    makeTopPrivateMsg(nickTo);
                }
            }
        }
    }

    private boolean isTherePrivateWithNickTo(ArrayList<PrivateStage> arrayList, String nickTo){
        boolean isThere = false;
        Iterator <PrivateStage>iterator = arrayList.iterator();

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
//        if(isTherePrivateWithNickTo(deletedPrivateStageArrayList,privateMsgArr[1])){
//            resurrectPrivateChat(privateMsgArr[1]);
//        }

        if(!privateMsgArr[1].equals(nickName.getText()) && !isTherePrivateWithNickTo(privateStageArrayList,privateMsgArr[1])){
            createPrivateChat(privateMsgArr[1]);
        }else{
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    makeTopPrivateMsg(privateMsgArr[1]);
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

    private void makeTopPrivateMsg(String nickTo){
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

//    private void resurrectPrivateChat (String nickTo){
//        Iterator<PrivateStage> iterator = deletedPrivateStageArrayList.iterator();
//        while (iterator.hasNext()){
//            PrivateStage ps = iterator.next();
//            if(ps.privateNickTo.equals(nickTo)){
//                addToPrivateStageArrayListFromDeletedPrivateStageArrayList(ps);
//            }
//        }
//    }





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
//        deletedPrivateStageArrayList.add(ps);
    }

//    public void addToPrivateStageArrayListFromDeletedPrivateStageArrayList(PrivateStage ps){
//        privateStageArrayList.add(ps);
//        deletedPrivateStageArrayList.remove(ps);
//    }


    public void setTimeOut(ControllerChat controllerChat, int delay){


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                controllerChat.connect();
            }
        }).start();
    }

    private void setDisableBtmAndField(boolean isDisable){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if(isDisable){
                    circleIsInNet.setStyle("-fx-fill: red");
                }else{
                    circleIsInNet.setStyle("-fx-fill: green");
                }
                messageTextField.setDisable(isDisable);
                btmSend.setDisable(isDisable);
                clientList.setDisable(isDisable);
                closeAllPrivateChar();


            }
        });
    }

    private void reLoginAfterCrashServer(){
        if(isLogin){
            try {
                out.writeUTF("/auth "+login+" "+ password);
                getSystemMessage("Успешное подключение");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeAllPrivateChar(){
        Iterator<PrivateStage> iterator = privateStageArrayList.iterator();
        while (iterator.hasNext()){
            PrivateStage ps = iterator.next();
            ps.close();
        }
    }

    public void writeLoginPassword(String login, String password){
        this.login = login;
        this.password = password;
    }

    public void dispose(){
        try {
            if(!socket.isClosed() && out != null) {
                System.out.println("Close");
                out.writeUTF("/end");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
