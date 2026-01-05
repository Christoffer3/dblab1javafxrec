package se.kth.duo.dblab1javafx.Controller;

import javafx.stage.Stage;
import se.kth.duo.dblab1javafx.Model.QL_Interface;
import se.kth.duo.dblab1javafx.View.UserView;
import java.sql.SQLException;

public class Controller {

    private final BookController bookController;
    private final AuthorController authorController;
    private final ReviewController reviewController;
    private final UserController userController;
    private final UserView userView;

    public Controller(Stage primaryStage, QL_Interface queryLogic) {
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

        startUI(primaryStage); // startar View
    }

    public void startUI(Stage stage) {
        userView.showUserProfile(stage);
    }

}