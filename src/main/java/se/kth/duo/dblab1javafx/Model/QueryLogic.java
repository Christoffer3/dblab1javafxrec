package se.kth.duo.dblab1javafx.Model;

import se.kth.duo.dblab1javafx.View.*;
import se.kth.duo.dblab1javafx.Controller.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class QueryLogic implements QL_Interface {

    Connection con;

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

    public List<Book> searchBookByISBN(String ISBN) throws DatabaseException {
        String query = "SELECT * FROM T_Book WHERE ISBN = ?";
        PreparedStatement ps = null;
        List<Book> resultBooks = new ArrayList<>();

        try {
            ps = con.prepareStatement(query);
            ps.setString(1, ISBN);
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
            throw new DatabaseException("SQL Error when searching a book by ISBN", e);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return resultBooks;
    }

    public List<Book> searchBookByAuthor(String firstName, String lastName) throws DatabaseException {
        String query = "SELECT b.* FROM T_Book b " +
                "JOIN T_Book_Authors ba ON b.ISBN = ba.book_ISBN " +
                "JOIN T_Author a ON ba.author_aID = a.aID " +
                "WHERE a.firstName LIKE ? AND a.lastName LIKE ?";

        PreparedStatement ps = null;
        List<Book> resultBooks = new ArrayList<>();

        try {
            ps = con.prepareStatement(query);
            ps.setString(1, "%" + firstName.trim() + "%");
            ps.setString(2, "%" + lastName.trim() + "%");
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
            throw new DatabaseException("SQL Error when searching a book by author", e);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return resultBooks;
    }

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

        PreparedStatement ps = null;
        List<Book> resultBooks = new ArrayList<>();

        try {
            ps = con.prepareStatement(query);
            ps.setInt(1, rating);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Book book = new Book();
                book.setISBN(rs.getString("ISBN"));
                book.setTitle(rs.getString("title"));
                book.setPages(rs.getInt("pages"));

                book.setAuthors(selectAuthorsForBook(book.getISBN()));
                book.setGenres(selectGenresForBook(book.getISBN()));

                resultBooks.add(book);
            }

        } catch (SQLException e) {
            throw new DatabaseException("SQL Error when searching books by minimum rating", e);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return resultBooks;
    }

    public List<Book> searchBookByGenre(String genre) throws DatabaseException {
        String query =
                "SELECT DISTINCT b.ISBN, b.title, b.pages " +
                        "FROM T_Book b " +
                        "JOIN T_Book_Genre bg ON b.ISBN = bg.book_ISBN " +
                        "JOIN T_Genre g ON bg.genre_gID = g.gID " +
                        "WHERE g.genreName LIKE ?";

        PreparedStatement ps = null;
        List<Book> resultBooks = new ArrayList<>();

        try {
            ps = con.prepareStatement(query);
            ps.setString(1, "%" + genre + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Book book = new Book();
                book.setISBN(rs.getString("ISBN"));
                book.setTitle(rs.getString("title"));
                book.setPages(rs.getInt("pages"));

                book.setAuthors(selectAuthorsForBook(book.getISBN()));
                book.setGenres(selectGenresForBook(book.getISBN()));

                resultBooks.add(book);
            }
        } catch (SQLException e) {
            throw new DatabaseException("SQL Error when searching books by genre", e);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return resultBooks;
    }


    /* select-frågor */
    public List<Author> selectAuthorsForBook(String ISBN) throws DatabaseException {
        List<Author> authorsForBook = new ArrayList<>();

        String query =
                "SELECT a.aID, a.firstName, a.lastName, a.birthDate, a.deathDate " +
                        "FROM T_Author a " +
                        "JOIN T_Book_Authors ba ON a.aID = ba.author_aID " +
                        "WHERE ba.book_ISBN = ?";
        PreparedStatement ps = null;

        try {
            ps = con.prepareStatement(query);
            ps.setString(1, ISBN);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Author author = new Author();
                author.setAuthorID(rs.getInt("aID"));
                author.setFirstName(rs.getString("firstName"));
                author.setLastName(rs.getString("lastName"));
                LocalDate birth = rs.getDate("birthDate").toLocalDate();
                if (birth != null) {
                    author.setBirthDate(birth);
                }
                authorsForBook.add(author);
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred: " + e.getMessage());
            e.printStackTrace();
            throw new DatabaseException("SQL Error when searching a book by rating", e);
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

        PreparedStatement ps = null;

        try {
            ps = con.prepareStatement(query);
            ps.setString(1, ISBN);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Genre genre = new Genre();
                genre.setgID(rs.getInt("gID"));
                genre.setGenre(rs.getString("genreName"));
                genresForBook.add(genre);
            }

        } catch (SQLException e) {
            throw new DatabaseException("SQL Error when fetching genres for book", e);
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return genresForBook;
    }


    /* Inserts */
    public void insertToBooks(Book book) throws DatabaseException { // när inloggad user lägger in en bok
        // TODO: ska följande delas upp i separata metoder? tror ej det
        String insertBook = "INSERT INTO T_Book (ISBN, title, pages) VALUES (?, ?, ?)";
        String linkAuthor = "INSERT INTO T_Book_Authors (book_ISBN, author_aID) VALUES (?, ?)"; // bokens tillhörande författare
        String linkGenre  = "INSERT INTO T_Book_Genre (book_ISBN, genre_gID) VALUES (?, ?)"; // bokens tillhörande genrer

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
                if (aId == -1) {
                    throw new DatabaseException("Unknown author: " + a.getFirstName() + " " + a.getLastName());
                }
                try (PreparedStatement ps = con.prepareStatement(linkAuthor)) {
                    ps.setString(1, book.getISBN());
                    ps.setInt(2, aId);
                    ps.executeUpdate();
                }
            }

            for (Genre g : book.getGenres()) {
                int gId = findGenreIdByName(g.getGenre());
                if (gId == -1) {
                    throw new DatabaseException("Unknown genre: " + g.getGenre());
                }
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

        } catch (SQLIntegrityConstraintViolationException e) { // kastas om user redan satt betyg på boken (då PK book_ISBN + username i databas)
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
            ps.setDate(4, java.sql.Date.valueOf(review.getReviewDate())); // LocalDate till sql.Date
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


    /* inloggning mot tabaas användare */
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

        return -1; // om ej funnen
    }

    private int findGenreIdByName(String genreName) throws SQLException {
        String sql = "SELECT gID FROM T_Genre WHERE genreName = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, genreName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("gID");
            }
        }

        return -1; // om ej hitta
    }




    // TODO: kan tas bort?
    public void insertToAuthors(Author author) throws DatabaseException {
        String query = "INSERT INTO T_Author (firstName, lastName, birthDate, deathDate) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, author.getFirstName());
            ps.setString(2, author.getLastName());
            ps.setDate(3, java.sql.Date.valueOf(author.getBirthDate()));

            if (author.getDeathDate() != null) {
                ps.setDate(4, java.sql.Date.valueOf(author.getDeathDate()));
            } else {
                ps.setNull(4, Types.DATE);
            }

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        author.setAuthorID(generatedKeys.getInt(1));                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred: " + e.getMessage());
            e.printStackTrace();
            throw new DatabaseException("SQL Error when searching a book by rating", e);
        }
    }

    // TODO: kan tas bort?
    public void bookAuthors(String ISBN, int a_id) throws DatabaseException {
        String query = "INSERT INTO T_Book_Authors (book_ISBN, author_aID) VALUES (?, ?)";
        PreparedStatement ps = null;

        try {
            con.setAutoCommit(false);
            ps = con.prepareStatement(query);
            ps.setString(1, ISBN);
            ps.setInt(2, a_id);
            int res = ps.executeUpdate();
            System.out.println(res + " records inserted");
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException e1) {
                    System.err.println("SQL Exception occurred: " + e1.getMessage());
                }
            }
            System.err.println("SQL Exception occurred: " + e.getMessage());
            e.printStackTrace();
            throw new DatabaseException("SQL Error when searching a book by rating", e);
        }
        finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                con.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("SQL Exception occurred: " + e.getMessage());
            }
        }

    }

    // TODO: kan tas bort?
    public void updateAuthor(Author oldAuthor, Author newAuthor) throws DatabaseException {
        String query;
        if (newAuthor.getDeathDate() != null) {
            query = "UPDATE T_Authors SET firstName = ?, lastName = ?, birthDate = ?, deathDate = ? WHERE firstName = ? AND lastName = ? AND birthDate = ?";

        } else {
            query = "UPDATE T_Authors SET firstName = ?, lastName = ?, birthDate = ? WHERE firstName = ? AND lastName = ? AND birthDate = ?";

        }
        PreparedStatement ps = null;
        try {
            con.setAutoCommit(false);
            ps = con.prepareStatement(query);
            if (newAuthor.getDeathDate() != null) {
                ps.setString(1, newAuthor.getFirstName());
                ps.setString(2, newAuthor.getLastName());
                ps.setDate(3, java.sql.Date.valueOf(newAuthor.getBirthDate()));
                ps.setDate(4, java.sql.Date.valueOf(newAuthor.getDeathDate()));

                ps.setString(5, oldAuthor.getFirstName());
                ps.setString(6, oldAuthor.getLastName());
                ps.setDate(7, java.sql.Date.valueOf(oldAuthor.getBirthDate()));

                if (oldAuthor.getDeathDate() != null) {
                    ps.setDate(8, java.sql.Date.valueOf(oldAuthor.getDeathDate()));                }
            }
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException e1) {
                    System.err.println("SQL Exception occurred: " + e1.getMessage());
                }
            }
            System.err.println("SQL Exception occurred: " + e.getMessage());
            e.printStackTrace();
            throw new DatabaseException("SQL Error when searching a book by rating", e);
        }
        finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                con.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("SQL Exception occurred: " + e.getMessage());
            }
        }
    }

}