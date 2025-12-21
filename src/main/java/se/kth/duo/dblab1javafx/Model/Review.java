package se.kth.duo.dblab1javafx.Model;

import java.time.LocalDate;

public class Review {
    private Book book;
    private User user;
    private String reviewText;
    private LocalDate reviewDate;

    public Review(Book book, User user, String reviewText, LocalDate reviewDate) {
        this.user = user;
        this.book = book;
        this.reviewText = reviewText;
        this.reviewDate = reviewDate;
    }

    public Review() {}

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public LocalDate getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDate reviewDate) {
        this.reviewDate = reviewDate;
    }
}