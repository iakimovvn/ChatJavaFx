package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Vector;

public class Server {
    private Vector<ClientHandler> clients;

    public Server() throws SQLException {
        clients = new Vector<>();
        ServerSocket server = null;
        Socket socket = null;
        try {
            AuthService.connect();

            server = new ServerSocket(8189);
            System.out.println("Server is start");

            while (true){
                socket = server.accept();
                System.out.println("Client connect");
                new ClientHandler(socket,this);

            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AuthService.disconnect();
        }
    }
    public boolean isNickBusy(String nick) {
        for (ClientHandler o : clients) {
            if (o.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public void broadcastSystemMsg(String msg){
        for (ClientHandler o: clients) {
            o.sendMsg("/systemmsg "+msg);
        }
    }

    public void broadcastMsg(ClientHandler from, String msg) {
        AuthService.writeMessageToSQLite(from.getNick(),msg);
        for (ClientHandler o : clients) {
            if (!AuthService.isInBlackList(from.getNick(),o.getNick())) {
                o.sendMsg(msg);
            }
        }
    }

    public void sendPersonalMsg(ClientHandler from, String nickTo, String msg) {
        for (ClientHandler o : clients) {
            if (o.getNick().equals(nickTo)) {
                if(AuthService.isInBlackList(o.getNick(),from.getNick())){
                    from.sendMsg("/wsystemmsg "+nickTo+" Вы находитесь в черном списке.");
                    return;
                }else {
                    o.sendMsg("/w " + from.getNick() +" "+ from.getNick()+" " + msg);
                    from.sendMsg("/w " + nickTo + " "+from.getNick()+ " " + msg);
                    return;
                }
            }
        }
        from.sendMsg("/systemmsg Клиент с ником " + nickTo + " не в чате");
    }

    private String makeMessageFromArray(String[] arr, int from, int to){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = from; i <= to; i++) {
            stringBuilder.append(arr[i]);
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

    public void subscribe (ClientHandler clientHandler){
        clients.add(clientHandler);
        broadcastSystemMsg(clientHandler.getNick()+" полдключился");
        broadcastClientList();
        AuthService.sendAllMessage(clientHandler);


    }

    public void unsubscribe (ClientHandler clientHandler){
        clients.remove(clientHandler);
        broadcastSystemMsg(clientHandler.getNick()+" отключился");
        broadcastClientList();
    }


    public void broadcastClientList(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("/clientlist ");
        for (ClientHandler clientHandler : clients) {
           stringBuilder.append(clientHandler.getNick()+" ") ;
        }
        String out = stringBuilder.toString();
        for (ClientHandler clientHandler: clients) {
            clientHandler.sendMsg(out);
        }

    }
}
