package se.kth.duo.dblab1javafx.Controller;

import javafx.application.Platform;
import se.kth.duo.dblab1javafx.Model.*;
import java.util.function.Consumer;

public class UserController {

    private final QL_Interface queryLogic;
    private User loggedInUser;

    public UserController(QL_Interface queryLogic) {
        this.queryLogic = queryLogic;
    }

    public boolean isLoggedIn() {
        return loggedInUser != null;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void logout() {
        loggedInUser = null;
    }

    public User login(String username, String password) throws DatabaseException {
        if (username == null || password == null) return null;

        User user = new User();
        user.setUsername(username.trim());

        User theUser = queryLogic.login(user, password);
        loggedInUser = theUser;
        return theUser;
    }

    public void loginAsync(String username,
                           String password,
                           Consumer<User> onSuccess,
                           Consumer<Exception> onError) {

        new Thread(() -> {
            try {
                User user = login(username, password);
                Platform.runLater(() -> onSuccess.accept(user));
            } catch (Exception ex) {
                Platform.runLater(() -> onError.accept(ex));
            }
        }, "loginT").start();

    }

}