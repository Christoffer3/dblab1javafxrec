package se.kth.duo.dblab1javafx.Model;


public class Rating {
    private Book book;
    private int rating;

    public Rating(Book book, int rating) {
        this.book = book;
        this.rating = rating;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}