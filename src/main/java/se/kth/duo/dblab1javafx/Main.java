package se.kth.duo.dblab1javafx;

import javafx.application.Platform;
import se.kth.duo.dblab1javafx.Controller.*;
import javafx.application.Application;
import javafx.stage.Stage;
import java.sql.Connection;


public class Main extends Application {

    private Connection con;
    private Controller controller;

    @Override
    public void start(Stage primaryStage) {
        try {
            JDBC jdbc = new JDBC("dblab1", "dblab1client", "dblab1"); // hårdkodad uppkoppling, denna rättighets-begränsade MySQL-inloggningen är i skapad i MySQL Workbench.
            con = jdbc.connectToDB();

            controller = new Controller(con);
            controller.startUI(primaryStage); // startar View

            primaryStage.setOnCloseRequest(e -> controller.shutdown()); // stänger connection vid avlslut av app.

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Connection to database denied! Please check if login-input is correct.");
            if (con != null) {
                try { con.close(); } catch (Exception ce) { // TODO: förbättra hantering?

                }
            }

            System.out.println("Exiting after exception thrown!.");
            Platform.exit(); // alternativt?
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}