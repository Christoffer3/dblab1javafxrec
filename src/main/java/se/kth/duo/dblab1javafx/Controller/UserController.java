package se.kth.duo.dblab1javafx.Controller;

import se.kth.duo.dblab1javafx.Model.*;

public class UserController {
    private final QL_Interface queryLogic;

    public UserController(QL_Interface queryLogic) { // tar emot huv. modellobjektat skapat i huv. Controllern
        this.queryLogic = queryLogic;
    }

    public User login(String username, String password) throws DatabaseException {
        if (username == null || password == null) return null;

        User user = new User();
        user.setUsername(username);
        return queryLogic.login(user, password);
    }

}