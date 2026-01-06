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

    /* Sökningar */
    public List<Book> searchBookByTitle(String title) throws DatabaseException {
        String query = "SELECT * FROM T_Book WHERE title LIKE ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Book> resultBooks = new ArrayList<>();

        try {
            ps = con.prepareStatement(query);
            ps.setString(1, "%" + title + "%");
            rs = ps.executeQuery();

            while (rs.next()) {
                Book book = new Book();
                book.setTitle(rs.getString("title"));
                book.setPages(rs.getInt("pages"));
                book.setISBN(rs.getString("ISBN"));

                book.setAuthors(selectAuthorsForBook(book.getISBN()));
                book.setGenres(selectGenresForBook(book.getISBN()));

                resultBooks.add(book);
            }

        } catch (SQLException e) {
            throw new DatabaseException("SQL error when searching a book by title", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return resultBooks;
    }

    @Override
    public List<Book> searchBookByISBN(String ISBN) throws DatabaseException {
        String query = "SELECT * FROM T_Book WHERE ISBN = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Book> resultBooks = new ArrayList<>();

        try {
            ps = con.prepareStatement(query);
            ps.setString(1, ISBN);
            rs = ps.executeQuery();

            while (rs.next()) {
                Book book = new Book();
                book.setTitle(rs.getString("title"));
                book.setPages(rs.getInt("pages"));
                book.setISBN(rs.getString("ISBN"));

                book.setAuthors(selectAuthorsForBook(book.getISBN()));
                book.setGenres(selectGenresForBook(book.getISBN()));

                resultBooks.add(book);
            }

        } catch (SQLException e) {
            throw new DatabaseException("SQL eror when searching a book by ISBN", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return resultBooks;
    }

    @Override
    public List<Book> searchBookByAuthor(String firstName, String lastName) throws DatabaseException {
        String query = "SELECT b.* FROM T_Book b " +
                "JOIN T_Book_Authors ba ON b.ISBN = ba.book_ISBN " +
                "JOIN T_Author a ON ba.author_aID = a.aID " +
                "WHERE a.firstName LIKE ? AND a.lastName LIKE ?";

        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Book> resultBooks = new ArrayList<>();

        try {
            ps = con.prepareStatement(query);
            ps.setString(1, "%" + (firstName == null ? "" : firstName.trim()) + "%");
            ps.setString(2, "%" + (lastName == null ? "" : lastName.trim()) + "%");

            rs = ps.executeQuery();

            while (rs.next()) {
                Book book = new Book();
                book.setTitle(rs.getString("title"));
                book.setPages(rs.getInt("pages"));
                book.setISBN(rs.getString("ISBN"));

                book.setAuthors(selectAuthorsForBook(book.getISBN()));
                book.setGenres(selectGenresForBook(book.getISBN()));

                resultBooks.add(book);
            }

        } catch (SQLException e) {
            throw new DatabaseException("SQL error when searching a book by author", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return resultBooks;
    }

    @Override
    public List<Book> searchBookByRating(int rating) throws DatabaseException {
        String query =
                "SELECT b.ISBN, b.title, b.pages FROM T_Book b " +
                        "JOIN ( " +
                        "   SELECT book_ISBN, AVG(rating) AS avg_rating " +
                        "   FROM (SELECT book_ISBN, rating FROM T_Rating " +
                        "   UNION ALL " +
                        "   SELECT book_ISBN, rating FROM T_User_Rating) all_ratings " +
                        "   GROUP BY book_ISBN HAVING AVG(rating) >= ? " +
                        ") r ON b.ISBN = r.book_ISBN";

        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Book> resultBooks = new ArrayList<>();

        try {
            ps = con.prepareStatement(query);
            ps.setInt(1, rating);
            rs = ps.executeQuery();

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
            throw new DatabaseException("SQL err. when searching books by minimum rating", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Book> resultBooks = new ArrayList<>();

        try {
            ps = con.prepareStatement(query);
            ps.setString(1, "%" + genre + "%");
            rs = ps.executeQuery();

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
            throw new DatabaseException("SQL error when searching books by genre", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return resultBooks;
    }


    /* select-frågor */
    @Override
    public List<Author> selectAuthorsForBook(String ISBN) throws DatabaseException {
        String query =
                "SELECT a.aID, a.firstName, a.lastName, a.birthDate, a.deathDate " +
                        "FROM T_Author a " +
                        "JOIN T_Book_Authors ba ON a.aID = ba.author_aID " +
                        "WHERE ba.book_ISBN = ?";

        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Author> authorsForBook = new ArrayList<>();

        try {
            ps = con.prepareStatement(query);
            ps.setString(1, ISBN);
            rs = ps.executeQuery();

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

        } catch (SQLException e) {
            throw new DatabaseException("SQL error when getting authors for books", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return authorsForBook;
    }

    @Override
    public List<Genre> selectGenresForBook(String ISBN) throws DatabaseException {
        String query = "SELECT g.gID, g.genreName " +
                "FROM T_Genre g " +
                "JOIN T_Book_Genre bg ON g.gID = bg.genre_gID " +
                "WHERE bg.book_ISBN = ?";

        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Genre> genresForBook = new ArrayList<>();

        try {
            ps = con.prepareStatement(query);
            ps.setString(1, ISBN);
            rs = ps.executeQuery();

            while (rs.next()) {
                Genre genre = new Genre();
                genre.setgID(rs.getInt("gID"));
                genre.setGenre(rs.getString("genreName"));
                genresForBook.add(genre);
            }

        } catch (SQLException e) {
            throw new DatabaseException("SQL error when try getting genres for the book", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return genresForBook;
    }

    /* inserts */
    @Override
    public void insertToBooks(Book book) throws DatabaseException {
        String insertBook = "INSERT INTO T_Book (ISBN, title, pages) VALUES (?, ?, ?)";
        String linkAuthor = "INSERT INTO T_Book_Authors (book_ISBN, author_aID) VALUES (?, ?)";
        String linkGenre  = "INSERT INTO T_Book_Genre (book_ISBN, genre_gID) VALUES (?, ?)";

        PreparedStatement ps = null;

        try {
            con.setAutoCommit(false);

            // insert bok
            ps = con.prepareStatement(insertBook);
            ps.setString(1, book.getISBN());
            ps.setString(2, book.getTitle());
            ps.setInt(3, book.getPages());
            ps.executeUpdate();
            ps.close();
            ps = null;

            for (Author a : book.getAuthors()) { // länkar författare
                int aId = findAuthorIdByName(a.getFirstName(), a.getLastName());
                if (aId == -1) {
                    throw new DatabaseException("Unknown author: " + a.getFirstName() + " " + a.getLastName());
                }

                ps = con.prepareStatement(linkAuthor);
                ps.setString(1, book.getISBN());
                ps.setInt(2, aId);
                ps.executeUpdate();
                ps.close();
                ps = null;
            }

            for (Genre g : book.getGenres()) { // länka genrer
                int gId = findGenreIdByName(g.getGenre());
                if (gId == -1) {
                    throw new DatabaseException("Unknown genre: " + g.getGenre());
                }

                ps = con.prepareStatement(linkGenre);
                ps.setString(1, book.getISBN());
                ps.setInt(2, gId);
                ps.executeUpdate();
                ps.close();
                ps = null;
            }

            con.commit(); // genomför transaktionen till de tre tabellerna
        } catch (Exception e) {
            try {
                con.rollback();
            } catch (SQLException ignored) {
                System.err.println("rollback failed");
            }

            if (e instanceof DatabaseException) throw (DatabaseException) e;
            throw new DatabaseException("SQL error when add book with its authors and genres", e);
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                con.setAutoCommit(true);
            } catch (SQLException ignored) {
                System.err.println("turn on auto commit failed");
            }
        }
    }

    @Override
    public void insertToRatings(String ISBN, int rating) throws DatabaseException {
        String sql = "INSERT INTO T_Rating (book_ISBN, rating) VALUES (?, ?)";

        PreparedStatement ps = null;

        try {
            con.setAutoCommit(false);

            ps = con.prepareStatement(sql);
            ps.setString(1, ISBN);
            ps.setInt(2, rating);
            ps.executeUpdate();

            con.commit();

        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException ignored) {
                System.err.println("rollback failed");
            }
            throw new DatabaseException("transaction failed", e);

        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void insertToUserRatings(String ISBN, String username, int userRating) throws DatabaseException {
        String sql = "INSERT INTO T_User_Rating (book_ISBN, username, rating) VALUES (?, ?, ?)";

        PreparedStatement ps = null;

        try {
            con.setAutoCommit(false);

            ps = con.prepareStatement(sql);
            ps.setString(1, ISBN);
            ps.setString(2, username);
            ps.setInt(3, userRating);
            ps.executeUpdate();

            con.commit();

        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException ignored) {
                System.err.println("rollback failed");
            }

            if (e instanceof SQLIntegrityConstraintViolationException) {
                throw new DatabaseException("User have already rated this book", e);
            }
            throw new DatabaseException("transaction failed" + e.getMessage(), e);

        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void insertToReviews(Review review) throws DatabaseException {
        String sql = "INSERT INTO T_Review (book_ISBN, username, reviewText) VALUES (?, ?, ?)";

        PreparedStatement ps = null;

        try {
            con.setAutoCommit(false);

            ps = con.prepareStatement(sql);
            ps.setString(1, review.getBook().getISBN());
            ps.setString(2, review.getUser().getUsername());
            ps.setString(3, review.getReviewText());
            ps.executeUpdate();

            con.commit();

        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException ignored) {
                System.err.println("rollback failed");
            }

            throw new DatabaseException("transaction failed" + e.getMessage(), e);

        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /* Deletes */
    @Override
    public void deleteBookByISBN(String ISBN) throws DatabaseException {
        String sql = "DELETE FROM T_Book WHERE ISBN = ?";

        PreparedStatement ps = null;

        try {
            con.setAutoCommit(false);

            ps = con.prepareStatement(sql);
            ps.setString(1, ISBN);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DatabaseException("No book found with ISBN: " + ISBN);
            }

            con.commit();

        } catch (Exception e) {
            try {
                con.rollback();
            } catch (SQLException ignored) {
                System.err.println("rollback failed");
            }

            if (e instanceof DatabaseException) throw (DatabaseException) e;
            throw new DatabaseException("Transaction failed: " + e.getMessage(), e);

        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /* inloggning mot databas användare */
    @Override
    public User login(User user, String password) throws DatabaseException {
        String sql = "SELECT username, password, accountCreationDate FROM T_User WHERE username = ?";

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = con.prepareStatement(sql);
            ps.setString(1, user.getUsername().trim());
            rs = ps.executeQuery();

            if (!rs.next()) return null;

            String dbPass = rs.getString("password");
            if (password == null || !dbPass.equals(password)) return null;

            User loggedIn = new User();
            loggedIn.setUsername(rs.getString("username"));

            java.sql.Date created = rs.getDate("accountCreationDate");
            if (created != null) loggedIn.setAccountCreationDate(created.toLocalDate());

            return loggedIn;

        } catch (SQLException e) {
            throw new DatabaseException("SQL Error at login", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }


    /* hjälp-metoder */
    private int findAuthorIdByName(String firstName, String lastName) throws SQLException {
        String sql = "SELECT aID FROM T_Author WHERE firstName = ? AND lastName = ?";

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = con.prepareStatement(sql);
            ps.setString(1, firstName);
            ps.setString(2, lastName);

            rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("aID");

            return -1;

        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private int findGenreIdByName(String genreName) throws SQLException {
        String sql = "SELECT gID FROM T_Genre WHERE genreName = ?";

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = con.prepareStatement(sql);
            ps.setString(1, genreName);

            rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("gID");

            return -1;

        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}