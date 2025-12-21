package se.kth.duo.dblab1javafx.Model;

public class UserRating {
    private Book book;
    private User user;
    private int rating;

    public UserRating(Book book, User user, int rating) {
        this.book = book;
        this.user = user;
        this.rating = rating;
    }

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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}