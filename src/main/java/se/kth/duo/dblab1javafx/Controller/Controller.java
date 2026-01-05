package se.kth.duo.dblab1javafx.Controller;

import javafx.stage.Stage;
import se.kth.duo.dblab1javafx.Model.*;
import se.kth.duo.dblab1javafx.View.UserView;
import java.sql.Connection;
import java.sql.SQLException;

public class Controller {

    private final Connection con;
    private final QL_Interface queryLogic;
    private final BookController bookController;
    private final AuthorController authorController;
    private final ReviewController reviewController;
    private final UserController userController;
    private final UserView userView;

    public Controller(Connection con, Stage primaryStage) {
        this.con = con;
        this.queryLogic = new QueryLogic(con);

        this.bookController = new BookController(queryLogic);
        this.authorController = new AuthorController(queryLogic);
        this.reviewController = new ReviewController(queryLogic);
        this.userController = new UserController(queryLogic);

        this.userView = new UserView(
                bookController,
                authorController,
                reviewController,
                userController
        );

        primaryStage.setOnCloseRequest(e -> this.shutdown()); // stänger connection vid avlslut av app.
        startUI(primaryStage); // startar View
    }

    public void startUI(Stage stage) {
        userView.showUserProfile(stage);
    }

    public void shutdown() { // TODO: hanteras det korrekt enligt uppg.beskrivning? saknar finally efter sista exceptionen?
        System.out.println("Shutting down.");
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                System.out.println("Connection successfully closed towards database.");
            }
        } catch (SQLException e) {
            System.out.println("Exception thrown whilst trying to close connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}