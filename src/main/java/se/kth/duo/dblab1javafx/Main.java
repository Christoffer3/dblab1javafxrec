package se.kth.duo.dblab1javafx;

import javafx.application.Platform;
import se.kth.duo.dblab1javafx.Controller.Controller;
import javafx.application.Application;
import javafx.stage.Stage;
import se.kth.duo.dblab1javafx.Model.JDBC;
import se.kth.duo.dblab1javafx.Model.QL_Interface;
import se.kth.duo.dblab1javafx.Model.QueryLogic;
import java.sql.Connection;
import java.sql.SQLException;


public class Main extends Application {

    private Connection con;
    private Controller controller;

    @Override
    public void start(Stage primaryStage) {
        try {
            JDBC jdbc = new JDBC("dblab1", "dblab1client", "dblab1"); // hårdkodad uppkoppling, denna rättighets-begränsade MySQL-inloggningen är i skapad i MySQL Workbench.
            con = jdbc.connectToDB();

            QL_Interface queryLogic = new QueryLogic(con);
            primaryStage.setOnCloseRequest(e -> this.closeConnection()); // stänger connection vid avlslut av app.
            controller = new Controller(primaryStage, queryLogic);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Connection to database denied, please check that login-input is correct");
            closeConnection();
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }


    public void closeConnection() {
        System.out.println("Try closing connection to database");

        try {
            if (con != null && !con.isClosed()) {
                con.close();
                System.out.println("Connection to database closed");
            }
        } catch (SQLException e) {
            System.out.println("Exception thrown whilst trying to close connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

}