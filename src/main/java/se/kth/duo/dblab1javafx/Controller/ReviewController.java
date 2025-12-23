package se.kth.duo.dblab1javafx.Controller;

import se.kth.duo.dblab1javafx.Model.*;
import java.time.LocalDate;

public class ReviewController {

    private final QL_Interface queryLogic;

    public ReviewController(QL_Interface queryLogic) {
        this.queryLogic = queryLogic;
    }

    public Review createReview(Book book, User user, String text) throws DatabaseException {
        Review r = new Review(book, user, text, LocalDate.now());
        queryLogic.insertToReviews(r);
        return r;
    }

}