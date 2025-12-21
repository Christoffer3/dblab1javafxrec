package se.kth.duo.dblab1javafx.Controller;

import se.kth.duo.dblab1javafx.Model.*;

// import java.sql.SQLException;
// import java.util.ArrayList;
import java.util.List;
// import java.util.stream.Collectors;

public class BookController {

    private final QL_Interface queryLogic;

    public BookController(QL_Interface queryLogic) { // tar emot huv. modellobjektat skapat i huv. Controllern
        this.queryLogic = queryLogic;
    }

    public Book createBook(String title, List<Author> authors, List<Genre> genres, int pages, String ISBN) throws DatabaseException {
        Book book = new Book(title, pages, ISBN);
        book.setAuthors(authors);
        book.setGenres(genres);
        queryLogic.insertToBooks(book);
        return book;
    }

    public List<Book> searchBookByTitle(String title) throws DatabaseException {
        return queryLogic.searchBookByTitle(title);
    }
    public List<Book> searchBookByAuthor(String firstName, String lastName) throws DatabaseException {
        return queryLogic.searchBookByAuthor(firstName, lastName);
    }

    public List<Book> searchBookByISBN(String isbn) throws DatabaseException {
        return queryLogic.searchBookByISBN(isbn);
    }

    public List<Book> searchBookByGenre(String genre) throws DatabaseException {
        return queryLogic.searchBookByGenre(genre);
    }

    public List<Book> searchBookByRating(int rating) throws DatabaseException {
        return queryLogic.searchBookByRating(rating);
    }

    public void removeBook(String isbn) throws DatabaseException {
        queryLogic.deleteBookByISBN(isbn);
    }

    // TODO: inloggad användare kan fortfarande göra anonyma (inloggade users ska ej kunna) (!)
    public void rateBookAnonymous(String isbn, int rating) throws DatabaseException {
        queryLogic.insertToRatings(isbn, rating);
    }

    public void userRateBook(String isbn, String username, int rating) throws DatabaseException {
        queryLogic.insertToUserRatings(isbn, username, rating);
    }

    // Kan ta bort det nedan?
    public void assignAuthorToBook(String ISBN, int authorID) throws DatabaseException {
        queryLogic.bookAuthors(ISBN, authorID);
    }

    public String getTitle(Book book) {
        return book.getTitle();
    }

    public List<Genre> getGenre(Book book) {
        return book.getGenres();
    }

    public int getPages(Book book) {
        return book.getPages();
    }

    public String getISBN(Book book) {
        return book.getISBN();
    }

    public List<Author> getAuthors(Book book) {
        return book.getAuthors();
    }
}