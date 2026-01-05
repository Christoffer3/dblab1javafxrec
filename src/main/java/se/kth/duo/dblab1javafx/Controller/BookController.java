package se.kth.duo.dblab1javafx.Controller;

import javafx.application.Platform;
import se.kth.duo.dblab1javafx.Model.*;
import java.util.List;
import java.util.function.Consumer;

public class BookController {

    private final QL_Interface queryLogic;

    public BookController(QL_Interface queryLogic) {
        this.queryLogic = queryLogic;
    }

    public Book createBook(String title, List<Author> authors, List<Genre> genres, int pages, String ISBN) throws DatabaseException {
        Book book = new Book(title, pages, ISBN);
        book.setAuthors(authors);
        book.setGenres(genres);
        queryLogic.insertToBooks(book);
        return book;
    }

    public void createBookAsync(String title, List<Author> authors, List<Genre> genres, int pages, String ISBN, Consumer<Book> onSuccess, Consumer<Throwable> onError) {
        new Thread(() -> {
            try {
                Book book = createBook(title, authors, genres, pages, ISBN);
                Platform.runLater(() -> onSuccess.accept(book));
            } catch (Throwable ex) {
                Platform.runLater(() -> onError.accept(ex));
            }
        }, "book-thread").start();
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

    public void rateBookAnonymous(String isbn, int rating) throws DatabaseException {
        queryLogic.insertToRatings(isbn, rating);
    }

    public void userRateBook(String isbn, String username, int rating) throws DatabaseException {
        queryLogic.insertToUserRatings(isbn, username, rating);
    }

    public void removeBookAsync(String isbn, Runnable onSuccess, Consumer<Throwable> onError) {
        new Thread(() -> {
            try {
                queryLogic.deleteBookByISBN(isbn);
                Platform.runLater(onSuccess);
            } catch (Throwable ex) {
                Platform.runLater(() -> onError.accept(ex));
            }
        }, "db-remove-thread").start();
    }

    public void rateBookAnonymousAsync(String isbn, int rating, Runnable onSuccess, Consumer<Throwable> onError) {
        new Thread(() -> {
            try {
                queryLogic.insertToRatings(isbn, rating);
                Platform.runLater(onSuccess);
            } catch (Throwable ex) {
                Platform.runLater(() -> onError.accept(ex));
            }
        }, "db-rate-anon-thread").start();
    }

    public void userRateBookAsync(String isbn, String username, int rating, Runnable onSuccess, Consumer<Throwable> onError) {
        new Thread(() -> {
            try {
                queryLogic.insertToUserRatings(isbn, username, rating);
                Platform.runLater(onSuccess);
            } catch (Throwable ex) {
                Platform.runLater(() -> onError.accept(ex));
            }
        }, "db-rate-user-thread").start();
    }

    public void searchAsync(String type,
                            String input,
                            String first,
                            String last,
                            Consumer<List<Book>> onSuccess,
                            Consumer<Throwable> onError) {

        new Thread(() -> {
            try {
                List<Book> books;
                switch (type) {
                    case "Title":  books = searchBookByTitle(input.trim()); break;
                    case "ISBN":   books = searchBookByISBN(input.trim()); break;
                    case "Genre":  books = searchBookByGenre(input.trim()); break;
                    case "Rating": books = searchBookByRating(Integer.parseInt(input.trim())); break;
                    case "Author": books = searchBookByAuthor(first.trim(), last.trim()); break;
                    default:       books = List.of();
                }
                Platform.runLater(() -> onSuccess.accept(books));
            } catch (Throwable ex) {
                Platform.runLater(() -> onError.accept(ex));
            }
        }, "search-thread").start();

    }

}