package se.kth.duo.dblab1javafx.Controller;

import se.kth.duo.dblab1javafx.Model.*;
// import java.sql.SQLException;
import java.time.LocalDate;
// import java.util.ArrayList;
// import java.util.List;

public class ReviewController {

    private QL_Interface queryLogic;

    public ReviewController(QL_Interface queryLogic) { // tar emot huv. modellobjektat skapat i huv. Controllern
        this.queryLogic = queryLogic;
    }

    public Review createReview(Book book, User user, String text) throws DatabaseException {
        Review r = new Review(book, user, text, LocalDate.now());
        queryLogic.insertToReviews(r);
        return r;
    }

}
