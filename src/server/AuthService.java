package server;

import java.sql.*;

public class AuthService {

    private static Connection connection;
    private static Statement stmt;

    public static void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:./src/server/MyUsers.db");
            stmt = connection.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String getNickByLoginAndPass(String login, String pass) {
        String sql = String.format("SELECT nickname FROM main where login = '%s' and password = '%s'", login, pass);

        try {
            ResultSet rs = stmt.executeQuery(sql);

            if(rs.next()) {
                return rs.getString(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void addToBlackList(String holderBlackList, String userToBlackList){
        String sql = String.format("SELECT blacklist FROM main where nickname = '%s'", holderBlackList);
        try {
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                String newBlackList = (rs.getString(1) == null)? userToBlackList
                        : rs.getString(1)+" "+userToBlackList;
                sql = String.format("UPDATE main SET blacklist = '%s' WHERE nickname = '%s'",newBlackList,holderBlackList);
                stmt.execute(sql);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isInBlackList (String holderBlackList, String userForCheck){
        boolean res = false;
        String sql = String.format("SELECT blacklist FROM main where nickname = '%s'", holderBlackList);
        try {
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next() && rs.getString(1)!= null){
                String[] blackListArr = rs.getString(1).split(" ");
                for (String user: blackListArr) {
                    if(user.equals(userForCheck)) {
                        res = true;
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
    public static boolean isUserWithLogin(String login){
        boolean isUser = false;
        String sql = String.format("SELECT id FROM main where login = '%s'", login );
        try {
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                isUser = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isUser;
    }

    public static boolean isUserWithNick(String nick){
        boolean isUser = false;
        String sql = String.format("SELECT id FROM main where nickname = '%s'", nick );
        try {
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                isUser = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isUser;

    }

    public static void deleteFromBlackList (String holderBlackList, String userDeleteFromBlackList){
        String sql = String.format("SELECT blacklist FROM main where nickname = '%s'", holderBlackList);
        try {
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next() && rs.getString(1)!= null){
                StringBuilder sb = new StringBuilder();
                String[] blackListArr = rs.getString(1).split(" ");
                for (String user: blackListArr) {
                    if(!user.equals(userDeleteFromBlackList)){
                        sb.append(user);
                        sb.append(" ");
                    }
                }
                sql = String.format("UPDATE main SET blacklist = '%s' WHERE nickname = '%s'",sb.toString(),holderBlackList);
                stmt.execute(sql);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public static String registration(String regData){
        String msg="";
        String[] regDataArr = regData.split(" ");
        try {
            if(isUserWithLogin(regDataArr[1])){
                msg = "Пользователь с логином "+regDataArr[1]+ " уже зарегистрирован";
            }else if(isUserWithNick(regDataArr[3])){
                msg = "Пользователь с ником "+regDataArr[3]+ " уже зарегистрирован";
            }else {
                String sql = String.format("INSERT INTO main (login, password, nickname, controlword)\n" +
                        "VALUES ('%s', '%s','%s','%s');", regDataArr[1], regDataArr[2], regDataArr[3], regDataArr[4]);
                stmt.execute(sql);
                msg = "/regok";
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return msg;
    }
    public static String recoveryPass(String recoveryData){
        String resMsg = "/recovery ";
        String[] recoveryDataArr = recoveryData.split(" ");
        String sql = String.format("SELECT password FROM main where login = '%s' and  controlword = '%s';"
                ,recoveryDataArr[1],recoveryDataArr[2]);
        try {
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                resMsg+=rs.getString(1);
            }
            else {
                resMsg+="Пользователь не найден";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resMsg;
    }



    public static void clearBlackList(String holderBlackList){
        String sql = String.format("UPDATE main SET blacklist = NULL WHERE nickname = '%s'",holderBlackList);
        try {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
