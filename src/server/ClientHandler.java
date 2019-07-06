package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Server server;

    private String nick;
    private boolean authOk = false;



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
                            if(str.startsWith("/auth")) {
                                String[] tokens = str.split(" ");
                                String newNick = AuthService.getNickByLoginAndPass(tokens[1], tokens[2]);
                                if (newNick != null) {
                                    if(!server.isNickBusy(newNick)){
                                        authOk = true;
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

                        while (authOk){
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
                                    if(nick.equals(tokens[1])){
                                        sendMsg("Вы не можете добавить в черный список самого себя");
                                    }
                                    else if(AuthService.isUserWithNick(tokens[1])) {
                                        if (AuthService.isInBlackList(nick, tokens[1])) {
                                            sendMsg("пользователь уже в черном списке");
                                        } else {
                                            AuthService.addToBlackList(nick, tokens[1]);
                                            sendMsg("Вы добавили пользователя " + tokens[1] + " в черный список");
                                        }
                                    }else{
                                        sendMsg("Пользователь с ником "+tokens[1]+" не зарегистрирован");
                                    }
                                }
                                if(str.startsWith("/delblacklist")){
                                    String[] tokens = str.split(" ");
                                    if (AuthService.isInBlackList(nick, tokens[1])) {
                                        AuthService.deleteFromBlackList(nick, tokens[1]);
                                        sendMsg("Вы удалили пользователя " + tokens[1] + " из черного списока");
                                    } else {
                                        sendMsg("Пользователя "+ tokens[1]+" нет в черном списке");

                                    }
                                }
                                if(str.startsWith("/clearblacklist")){
                                    AuthService.clearBlackList(nick);
                                    sendMsg("Черный список очищен.");
                                }
                            }else {
                                server.broadcastMsg(ClientHandler.this,nick+": "+str);
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
