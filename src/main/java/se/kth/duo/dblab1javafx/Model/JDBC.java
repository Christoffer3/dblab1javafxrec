package se.kth.duo.dblab1javafx.Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class JDBC {

    private String user;
    private String pass;
    private String db;

    public JDBC(String database, String username, String pass) {
        this.db = database;
        this.user = username;
        this.pass = pass;
    }

    public Connection connectToDB() throws SQLException{
        String server = "jdbc:mysql://localhost:3306/" + db + "?UseClientEnc=UTF8";
        Connection con = null;

        System.out.println("Connecting to database...");
        con = DriverManager.getConnection(server, user, pass);

        System.out.println("Connected as " + user);

        return con;
    }
}