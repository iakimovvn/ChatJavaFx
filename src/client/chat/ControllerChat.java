package client.chat;

import client.ChatMain;
import client.login.ControllerLogin;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

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
    private ArrayList<PrivateStage> deletedPrivateStageArrayList;

    private ControllerLogin controllerLogin;

    private boolean isLogin = false;
    private String login;
    private String password;
    private String nick;

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
            deletedPrivateStageArrayList = new ArrayList<>();

            clientList.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                @Override
                public void handle(ContextMenuEvent event) {
                    MyContextMenu myContextMenu = new MyContextMenu(clientList.getSelectionModel().getSelectedItem());
                    myContextMenu.show(clientList,event.getScreenX(),event.getScreenY());
                }
            });

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
                                    nick = nickArr[1];
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {

                                            nickName.setText(nick);
                                        }
                                    });
                                }
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
                    } finally {
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
            if(!msgArr[1].startsWith("null")) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        vBoxMessage.getChildren().add(new SystemMessageHBox(msgArr[1]));
                    }
                });
            }
        } else if(msgArr[0].equals(nick)){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    vBoxMessage.getChildren().add(new MyMessageHBox(makeMessageForLabel(msgArr[1])));

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
                Alert alert = new Alert(Alert.AlertType.WARNING,"Вы не можете открыть приватный чат с самим собой!");
                alert.show();
            }else{
                if(isTherePrivateWithNickTo(deletedPrivateStageArrayList, nickTo)){
                   resurrectPrivateChat(nickTo);
                }
                else if(!isTherePrivateWithNickTo(privateStageArrayList,nickTo)){
                    createPrivateChat(nickTo);
                }else{
                    putTopPrivateMsg(nickTo);
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
        if(isTherePrivateWithNickTo(deletedPrivateStageArrayList,privateMsgArr[1])){
            resurrectPrivateChat(privateMsgArr[1]);
        }

        if(!privateMsgArr[1].equals(nickName.getText()) && !isTherePrivateWithNickTo(privateStageArrayList,privateMsgArr[1])){
            createPrivateChat(privateMsgArr[1]);
        }else{
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    putTopPrivateMsg(privateMsgArr[1]);
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
                    ps.controllerPrivateChat.addToVBoxMessage(new MyMessageHBox(makeMessageForLabel(privateMsgArr[3])));
                    break;
                }else{
                    ps.controllerPrivateChat.addToVBoxMessage(new OtherMessageHBox(privateMsgArr[2],makeMessageForLabel(privateMsgArr[3])));
                    break;
                }
            }
        }
    }

    private void putTopPrivateMsg(String nickTo){
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
        final int NUMBER_IF_SINGS = 30;
        if(msg.length() > NUMBER_IF_SINGS){
            String[] msgArr = msg.split(" ");
            StringBuilder stringBuilder = new StringBuilder();
            int singsNow = 0;
            for (String word : msgArr) {
                if(singsNow+word.length() < NUMBER_IF_SINGS-1){
                    stringBuilder.append(word);
                    stringBuilder.append(" ");
                    singsNow+=word.length()+1;
                }else if(singsNow+word.length()==NUMBER_IF_SINGS || singsNow+word.length()==NUMBER_IF_SINGS-1){
                    stringBuilder.append(word);
                    stringBuilder.append("\n");

                    singsNow = 0;
                }else{
                    int countRemainingSymbols = NUMBER_IF_SINGS - singsNow-1;
                    String[] twoWordsArr = makeTwoWordFromOne(word, countRemainingSymbols);
                    stringBuilder.append(twoWordsArr[0]);
                    stringBuilder.append(twoWordsArr[1]);

                    singsNow = word.length() - countRemainingSymbols;

                }

            }
            msg = stringBuilder.toString();

        }
        return msg;

    }

    private String[] makeTwoWordFromOne(String word, int lengthFirstWord){
        String[] resArr = new String[2];
        char[] charWordArr = word.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i <lengthFirstWord ; i++) {
            stringBuilder.append(charWordArr[i]);
        }
        stringBuilder.append("-\n");
        resArr[0] = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        for (int i = lengthFirstWord; i < charWordArr.length ; i++) {
            stringBuilder.append(charWordArr[i]);
        }
        stringBuilder.append(" ");
        resArr[1] = stringBuilder.toString();
        return resArr;

    }

    public void deleteFromPrivateStageArrayList(PrivateStage ps){
        privateStageArrayList.remove(ps);
        deletedPrivateStageArrayList.add(ps);
    }


    private void setTimeOut(ControllerChat controllerChat, int delay){


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


    public void makeGreenYellowTheme(){
       changeCssFromEverything("client/chat/resourcesChat/css/StyleClassYellow.css");
    }

    public void makeBlueRedTheme(){
        changeCssFromEverything("client/chat/resourcesChat/css/StyleClassBlue.css");
    }

    private void changeCssFromEverything(String cssUrl){
        ChatMain.sceneChat.getRoot().getStylesheets().clear();
        ChatMain.sceneChat.getRoot().getStylesheets().add(cssUrl);
        changeCssFromActivePrivate(cssUrl);
        changeCssFromPrivateDeleted(cssUrl);

    }

    private void changeCssFromActivePrivate(String cssUrl){
        Iterator <PrivateStage> iterator = privateStageArrayList.iterator();
        while (iterator.hasNext()){
            iterator.next().changeCss(cssUrl);
        }
    }

    private void changeCssFromPrivateDeleted(String cssUrl){
        Iterator <PrivateStage> iterator = deletedPrivateStageArrayList.iterator();
        while (iterator.hasNext()){
            iterator.next().changeCss(cssUrl);
        }
    }

    public void logout(){
        controllerLogin.setAuthorized(false);
        dispose();
    }


    private void addToPrivateStageArrayListFromDeletedPrivateStageArrayList(PrivateStage ps){
        privateStageArrayList.add(ps);
        deletedPrivateStageArrayList.remove(ps);
    }

    private void resurrectPrivateChat (String nickTo){
        Iterator<PrivateStage> iterator = deletedPrivateStageArrayList.iterator();
        while (iterator.hasNext()){
            PrivateStage ps = iterator.next();
            if(ps.privateNickTo.equals(nickTo)){
                addToPrivateStageArrayListFromDeletedPrivateStageArrayList(ps);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        ps.show();
                    }
                });
                break;
            }
        }
    }


    public void clearBlacklist(){
        sendMsgFromString("/clearblacklist");
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }

    public void clearChat(){
        vBoxMessage.getChildren().clear();
    }



    public void dispose(){
        try {
            if( out != null && !socket.isClosed()) {
                System.out.println("Close");
                out.writeUTF("/end");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class MyContextMenu extends ContextMenu {

        private MyContextMenu(String nick) {
            MenuItem addBlackList = new MenuItem("add to BlackList");
            MenuItem removeFromBlackList = new MenuItem(" remove from BlackList");

            addBlackList.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    sendMsgFromString("/blacklist "+nick);

                }
            });

            removeFromBlackList.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    sendMsgFromString("/delblacklist "+nick);

                }
            });

            this.getItems().addAll(addBlackList,removeFromBlackList);
        }
    }


    private class MyMessageHBox extends HBox {


        private MyMessageHBox(String message) {
            setMaxWidth(350);
            setAlignment(Pos.CENTER_LEFT);

            Pane pane = new Pane();
            pane.setPrefWidth(10);
            this.getChildren().add(pane);

            Label nickLabel = new Label("ME: ");
            nickLabel.setFont(Font.font("Arial", FontWeight.BOLD,16));
            nickLabel.setAlignment(Pos.CENTER);
            nickLabel.setTextFill(Color.RED);
            nickLabel.setMaxWidth(140);
            this.getChildren().add(nickLabel);


            this.getChildren().add(new PaneMessage(message, Color.YELLOW));

        }


    }

    private class PaneMessage extends Pane {
        private PaneMessage (String message, Color color){
            Text msg = new Text(message);
            msg.setFont(Font.font("Arial", FontPosture.ITALIC,14));
            Bounds messageBounds = msg.getBoundsInParent();

            double msgWight = messageBounds.getWidth();
            double msgHeight = messageBounds.getHeight();
            double paneWight = msgWight + 20;
            double paneHeight = msgHeight + 20;
            double msgX = (paneWight - msgWight)/2;
            double msgY = (paneHeight-msgHeight)/2;

            msg.relocate(msgX,msgY);

            Rectangle rectangle = new Rectangle(0 ,0,paneWight,paneHeight);
            rectangle.setArcWidth(20);
            rectangle.setArcHeight(20);
            rectangle.setFill(color);

           this.getChildren().addAll(rectangle, msg);
           setPrefSize(paneWight,paneHeight);

        }
    }





    private class SystemMessageHBox extends HBox {

        private SystemMessageHBox(String msg) {

            setPrefWidth(350);
            setAlignment(Pos.CENTER);

            Label messageLbl = new Label(msg);
            messageLbl.setFont(Font.font("Arial",12));
            messageLbl.setAlignment(Pos.CENTER);
            messageLbl.setTextFill(Color.GRAY);
            messageLbl.setMaxWidth(350);
            this.getChildren().add(messageLbl);
        }
    }



    private class OtherMessageHBox extends HBox {

        private OtherMessageHBox(String nickname, String message) {
            setMaxWidth(350);
            setAlignment(Pos.CENTER_RIGHT);
            this.getChildren().add(new PaneMessage(message, Color.PINK));

            Label nickLabel = new Label(" :"+nickname);
            nickLabel.setFont(Font.font("Arial", FontWeight.BOLD,16));
            nickLabel.setAlignment(Pos.CENTER);
            nickLabel.setTextFill(Color.GREEN);
            nickLabel.setMaxWidth(140);
            this.getChildren().add(nickLabel);

            Pane pane = new Pane();
            pane.setPrefWidth(10);
            this.getChildren().add(pane);

        }
    }

}
