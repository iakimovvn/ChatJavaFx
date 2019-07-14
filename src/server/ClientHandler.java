package server;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;


public class ClientHandler {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Server server;

    private String nick;



    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String str = in.readUTF();
                            if (str.equals("/end")) {
                                out.writeUTF("/serverclosed");
                                break;
                            }
                            if(str.startsWith("/registration")){
                                out.writeUTF(AuthService.registration(str));
                            }
                            if(str.startsWith("/recovery")){
                                out.writeUTF(AuthService.recoveryPass(str));
                            }
                            if(str.startsWith("/auth")) {
                                String[] tokens = str.split(" ");
                                String newNick = AuthService.getNickByLoginAndPass(tokens[1], tokens[2]);
                                if (newNick != null) {
                                    if(!server.isNickBusy(newNick)){
                                        sendMsg("/authok " +newNick);
                                        nick = newNick;
                                        server.subscribe(ClientHandler.this);
                                        break;
                                    } else {
                                        sendMsg("Пользователь в сети.");
                                    }
                                }else {
                                    sendMsg("Неверный логин/пароль");
                                }
                            }
                        }

                        while (true){
                            String str = in.readUTF();
                            if(str.startsWith("/")) {
                                if (str.equals("/end")) {
                                    out.writeUTF("/serverclosed");
                                    break;
                                }
                                if(str.startsWith("/w")){
                                    String[] tokens = str.split(" ",3);
                                    server.sendPersonalMsg(ClientHandler.this,tokens[1],tokens[2]);
                                }
                                if(str.startsWith("/blacklist")){
                                    String[] tokens = str.split(" ");
                                    if(tokens.length == 1){
                                        sendMsg("/systemmsg command not found");
                                    }
                                    else if(nick.equals(tokens[1])){
                                        sendMsg("/systemmsg Вы не можете добавить в черный список самого себя");
                                    }
                                    else if(AuthService.isUserWithNick(tokens[1])) {
                                        if (AuthService.isInBlackList(nick, tokens[1])) {
                                            sendMsg("/systemmsg пользователь уже в черном списке");
                                        } else {
                                            AuthService.addToBlackList(nick, tokens[1]);
//                                            sendMsg("/systemmsg Вы добавили пользователя " + tokens[1] + " в черный список");
                                            server.broadcastSystemMsg(nick+" добавил "+tokens[1]+ " в черный список");
                                        }
                                    }else{
                                        sendMsg("/systemmsg Пользователь с ником "+tokens[1]+" не зарегистрирован");
                                    }
                                }
                                if(str.startsWith("/delblacklist")){
                                    String[] tokens = str.split(" ");
                                    if(tokens.length ==1){
                                        sendMsg("/systemmsg вы не ввели ник пользователя");

                                    }
                                    else if (AuthService.isInBlackList(nick, tokens[1])) {
                                        AuthService.deleteFromBlackList(nick, tokens[1]);
//                                        sendMsg("Вы удалили пользователя " + tokens[1] + " из черного списока");
                                        server.broadcastSystemMsg(tokens[1]+ " теперь модет общаться с "+nick);

                                    } else {
                                        sendMsg("/systemmsg Пользователя "+ tokens[1]+" нет в черном списке");

                                    }
                                }
                                if(str.startsWith("/clearblacklist")){
                                    AuthService.clearBlackList(nick);
                                    sendMsg("/systemmsg Черный список очищен.");
                                }
                            }else {
                                server.broadcastMsg(ClientHandler.this,nick+" "+str);
                                AuthService.writeMessageToSQLite(nick, str);

                            }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        server.unsubscribe(ClientHandler.this);
                        System.out.println("Client Disconnect");

                    }

                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendMsg(String msg){
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }
}
