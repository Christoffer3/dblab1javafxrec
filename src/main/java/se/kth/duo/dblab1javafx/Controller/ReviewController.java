package se.kth.duo.dblab1javafx.Controller;

import javafx.application.Platform;
import se.kth.duo.dblab1javafx.Model.*;
import java.time.LocalDate;
import java.util.function.Consumer;

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

    public void createReviewAsync(Book book, User user, String text, Consumer<Review> onSuccess, Consumer<Throwable> onError) {
        new Thread(() -> {
            try {
                Review review = createReview(book, user, text);

                Platform.runLater(() -> onSuccess.accept(review));
            } catch (Throwable ex) {
                Platform.runLater(() -> onError.accept(ex));
            }
        }, "review-thread").start();
    }

}