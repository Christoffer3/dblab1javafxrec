package se.kth.duo.dblab1javafx.Model;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class QueryLogic implements QL_Interface {

    private Connection con;

    public QueryLogic(Connection con) {
        this.con = con;
    }

    /* Searches */
    public List<Book> searchBookByTitle(String title) throws DatabaseException {
        String query = "SELECT * FROM T_Book WHERE title LIKE ?";
        PreparedStatement ps = null;
        List<Book> resultBooks = new ArrayList<>();

        try {
            ps = con.prepareStatement(query);
            ps.setString(1, "%" + title + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Book book = new Book();
                book.setTitle(rs.getString("title"));
                book.setPages(rs.getInt("pages"));
                book.setISBN(rs.getString("ISBN"));

                List<Author> authors = selectAuthorsForBook(rs.getString("ISBN"));
                book.setAuthors(authors);
                book.setGenres(selectGenresForBook(book.getISBN()));

                resultBooks.add(book);
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred: " + e.getMessage());
            e.printStackTrace();
            throw new DatabaseException("SQL Error when searching a book by title", e);
        } finally {
            try {
                if (ps != null) {
                    ps.close(); // ResultSet stängs samtidigt
                }
            } catch (SQLException e) {
                e.printStackTrace(); // TODO: hantera bättre?
            }
        }

        return resultBooks;
    }

    @Override
    public List<Book> searchBookByISBN(String ISBN) throws DatabaseException {
        String query = "SELECT * FROM T_Book WHERE ISBN = ?";
        List<Book> resultBooks = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, ISBN);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Book book = new Book();
                    book.setTitle(rs.getString("title"));
                    book.setPages(rs.getInt("pages"));
                    book.setISBN(rs.getString("ISBN"));
                    book.setAuthors(selectAuthorsForBook(book.getISBN()));
                    book.setGenres(selectGenresForBook(book.getISBN()));
                    resultBooks.add(book);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("SQL Error when searching a book by ISBN", e);
        }
        return resultBooks;
    }

    @Override
    public List<Book> searchBookByAuthor(String firstName, String lastName) throws DatabaseException {
        String query = "SELECT b.* FROM T_Book b " +
                "JOIN T_Book_Authors ba ON b.ISBN = ba.book_ISBN " +
                "JOIN T_Author a ON ba.author_aID = a.aID " +
                "WHERE a.firstName LIKE ? AND a.lastName LIKE ?";

        List<Book> resultBooks = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, "%" + (firstName == null ? "" : firstName.trim()) + "%");
            ps.setString(2, "%" + (lastName == null ? "" : lastName.trim()) + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Book book = new Book();
                    book.setTitle(rs.getString("title"));
                    book.setPages(rs.getInt("pages"));
                    book.setISBN(rs.getString("ISBN"));
                    book.setAuthors(selectAuthorsForBook(book.getISBN()));
                    book.setGenres(selectGenresForBook(book.getISBN()));
                    resultBooks.add(book);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("SQL Error when searching a book by author", e);
        }
        return resultBooks;
    }

    @Override
    public List<Book> searchBookByRating(int rating) throws DatabaseException {

        // rating kommer från T_Rating + T_User_Rating
        // rating = "minsta snittrating", därför AVG + HAVING
        String query =
                "SELECT b.ISBN, b.title, b.pages " +
                        "FROM T_Book b " +
                        "JOIN ( " +
                        "   SELECT book_ISBN, AVG(rating) AS avg_rating " +
                        "   FROM ( " +
                        "       SELECT book_ISBN, rating FROM T_Rating " +
                        "       UNION ALL " +
                        "       SELECT book_ISBN, rating FROM T_User_Rating " +
                        "   ) all_ratings " +
                        "   GROUP BY book_ISBN " +
                        "   HAVING AVG(rating) >= ? " +
                        ") r ON b.ISBN = r.book_ISBN";

        List<Book> resultBooks = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, rating);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Book book = new Book();
                    book.setISBN(rs.getString("ISBN"));
                    book.setTitle(rs.getString("title"));
                    book.setPages(rs.getInt("pages"));
                    book.setAuthors(selectAuthorsForBook(book.getISBN()));
                    book.setGenres(selectGenresForBook(book.getISBN()));
                    resultBooks.add(book);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("SQL Error when searching books by minimum rating", e);
        }
        return resultBooks;
    }

    @Override
    public List<Book> searchBookByGenre(String genre) throws DatabaseException {
        String query =
                "SELECT DISTINCT b.ISBN, b.title, b.pages " +
                        "FROM T_Book b " +
                        "JOIN T_Book_Genre bg ON b.ISBN = bg.book_ISBN " +
                        "JOIN T_Genre g ON bg.genre_gID = g.gID " +
                        "WHERE g.genreName LIKE ?";

        List<Book> resultBooks = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, "%" + genre + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Book book = new Book();
                    book.setISBN(rs.getString("ISBN"));
                    book.setTitle(rs.getString("title"));
                    book.setPages(rs.getInt("pages"));
                    book.setAuthors(selectAuthorsForBook(book.getISBN()));
                    book.setGenres(selectGenresForBook(book.getISBN()));
                    resultBooks.add(book);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("SQL Error when searching books by genre", e);
        }
        return resultBooks;
    }


    /* select-frågor */
    @Override
    public List<Author> selectAuthorsForBook(String ISBN) throws DatabaseException {
        List<Author> authorsForBook = new ArrayList<>();

        String query =
                "SELECT a.aID, a.firstName, a.lastName, a.birthDate, a.deathDate " +
                        "FROM T_Author a " +
                        "JOIN T_Book_Authors ba ON a.aID = ba.author_aID " +
                        "WHERE ba.book_ISBN = ?";

        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, ISBN);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Author author = new Author();
                    author.setAuthorID(rs.getInt("aID"));
                    author.setFirstName(rs.getString("firstName"));
                    author.setLastName(rs.getString("lastName"));

                    Date birthSql = rs.getDate("birthDate");
                    if (birthSql != null) author.setBirthDate(birthSql.toLocalDate());

                    Date deathSql = rs.getDate("deathDate");
                    if (deathSql != null) author.setDeathDate(deathSql.toLocalDate());

                    authorsForBook.add(author);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("SQL Error when fetching authors for book", e);
        }

        return authorsForBook;
    }

    @Override
    public List<Genre> selectGenresForBook(String ISBN) throws DatabaseException {
        List<Genre> genresForBook = new ArrayList<>();

        String query = "SELECT g.gID, g.genreName " +
                "FROM T_Genre g " +
                "JOIN T_Book_Genre bg ON g.gID = bg.genre_gID " +
                "WHERE bg.book_ISBN = ?";

        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, ISBN);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Genre genre = new Genre();
                    genre.setgID(rs.getInt("gID"));
                    genre.setGenre(rs.getString("genreName"));
                    genresForBook.add(genre);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("SQL Error when fetching genres for book", e);
        }

        return genresForBook;
    }


    /* Inserts */
    @Override
    public void insertToBooks(Book book) throws DatabaseException {
        // TODO: ska följande delas upp i separata metoder? tror ej det
        String insertBook = "INSERT INTO T_Book (ISBN, title, pages) VALUES (?, ?, ?)";
        String linkAuthor = "INSERT INTO T_Book_Authors (book_ISBN, author_aID) VALUES (?, ?)";
        String linkGenre  = "INSERT INTO T_Book_Genre (book_ISBN, genre_gID) VALUES (?, ?)";

        try {
            con.setAutoCommit(false); // följande är en transaktion... behöver därav stänga av autocommit först

            try (PreparedStatement ps = con.prepareStatement(insertBook)) {
                ps.setString(1, book.getISBN());
                ps.setString(2, book.getTitle());
                ps.setInt(3, book.getPages());
                ps.executeUpdate();
            }

            for (Author a : book.getAuthors()) {
                int aId = findAuthorIdByName(a.getFirstName(), a.getLastName());
                if (aId == -1) throw new DatabaseException("Unknown author: " + a.getFirstName() + " " + a.getLastName());

                try (PreparedStatement ps = con.prepareStatement(linkAuthor)) {
                    ps.setString(1, book.getISBN());
                    ps.setInt(2, aId);
                    ps.executeUpdate();
                }
            }

            for (Genre g : book.getGenres()) {
                int gId = findGenreIdByName(g.getGenre());
                if (gId == -1) throw new DatabaseException("Unknown genre: " + g.getGenre());

                try (PreparedStatement ps = con.prepareStatement(linkGenre)) {
                    ps.setString(1, book.getISBN());
                    ps.setInt(2, gId);
                    ps.executeUpdate();
                }
            }

            con.commit(); // genomför transaktionen till de tre tabellerna
        } catch (Exception e) {
            try {
                con.rollback();
            } catch (SQLException ignored) { // TODO: hantera följande slut av metod bättre?

            }
            if (e instanceof DatabaseException) throw (DatabaseException) e;
            throw new DatabaseException("SQL Error when inserting book with authors/genres", e);
        } finally {
            try {
                con.setAutoCommit(true); // viktigt ej glömma, sätter på autocommit igen.
            } catch (SQLException ignored) {

            }
        }
    }

    @Override
    public void insertToRatings(String ISBN, int rating) throws DatabaseException {
        String sql = "INSERT INTO T_Rating (book_ISBN, rating) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ISBN);
            ps.setInt(2, rating);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("SQL Error when inserting rating", e);
        }
    }

    @Override
    public void insertToUserRatings(String ISBN, String username, int userRating) throws DatabaseException {
        String sql = "INSERT INTO T_User_Rating (book_ISBN, username, rating) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ISBN);
            ps.setString(2, username);
            ps.setInt(3, userRating);
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DatabaseException("User have already rated this book.", e);
        } catch (SQLException e) {
            throw new DatabaseException("SQL Error when inserting user rating", e);
        }
    }

    @Override
    public void insertToReviews(Review review) throws DatabaseException {
        String sql = "INSERT INTO T_Review (book_ISBN, username, reviewText, reviewDate) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, review.getBook().getISBN());
            ps.setString(2, review.getUser().getUsername());
            ps.setString(3, review.getReviewText());
            ps.setDate(4, java.sql.Date.valueOf(review.getReviewDate()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("SQL Error when inserting review", e);
        }
    }


    /* Deletes */
    @Override
    public void deleteBookByISBN(String ISBN) throws DatabaseException {
        String sql = "DELETE FROM T_Book WHERE ISBN = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ISBN);
            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new DatabaseException("No book found with ISBN: " + ISBN);
            }
        } catch (SQLException e) {
            throw new DatabaseException("SQL Error when deleting book", e);
        }
    }


    /* inloggning mot databas användare */
    @Override
    public User login(User user, String password) throws DatabaseException {
        String sql = "SELECT username, password, accountCreationDate FROM T_User WHERE username = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getUsername().trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                String dbPass = rs.getString("password");
                if (password == null || !dbPass.equals(password)) return null;

                User loggedIn = new User();
                loggedIn.setUsername(rs.getString("username"));

                java.sql.Date created = rs.getDate("accountCreationDate");
                if (created != null) loggedIn.setAccountCreationDate(created.toLocalDate());

                return loggedIn;
            }
        } catch (SQLException e) {
            throw new DatabaseException("SQL Error at login", e);
        }
    }


    /* hjälp-metoder */ // TODO: lägg till i interface? och byt namn till t.ex. "check if author is in database"
    private int findAuthorIdByName(String firstName, String lastName) throws SQLException {
        String sql = "SELECT aID FROM T_Author WHERE firstName = ? AND lastName = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("aID");
            }
        }
        return -1;
    }

    private int findGenreIdByName(String genreName) throws SQLException {
        String sql = "SELECT gID FROM T_Genre WHERE genreName = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, genreName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("gID");
            }
        }
        return -1;
    }

}