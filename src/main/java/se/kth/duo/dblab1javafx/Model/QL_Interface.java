package se.kth.duo.dblab1javafx.Model;

import java.util.List;

public interface QL_Interface {

    List<Book> searchBookByTitle(String title) throws DatabaseException;
    List<Book> searchBookByISBN(String ISBN) throws DatabaseException;
    List<Book> searchBookByAuthor(String firstName, String lastName) throws DatabaseException;
    List<Book> searchBookByRating(int rating) throws DatabaseException;
    List<Book> searchBookByGenre(String genre) throws DatabaseException;
    void insertToBooks(Book book) throws DatabaseException;
    void insertToRatings(String ISBN, int rating) throws DatabaseException;
    void insertToUserRatings(String ISBN, String username, int userRating) throws DatabaseException;
    void insertToReviews(Review review) throws DatabaseException;

    void deleteBookByISBN(String ISBN) throws DatabaseException;

    User login(User user, String password) throws DatabaseException;

}