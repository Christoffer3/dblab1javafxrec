package se.kth.duo.dblab1javafx.Controller;

import se.kth.duo.dblab1javafx.Model.*;

// import java.sql.SQLException;
import java.time.LocalDate;
// import java.util.List;

public class AuthorController {

    private final QL_Interface queryLogic;

    public AuthorController(QL_Interface queryLogic) { // tar emot huv. modellobjektat skapat i huv. Controllern
        this.queryLogic = queryLogic;
    }

    public Author createAuthor(String firstName, String lastname, LocalDate birthDate, LocalDate deathDate) throws DatabaseException {
        Author author = new Author(0, firstName, lastname, birthDate, deathDate);
        queryLogic.insertToAuthors(author);
        return author;
    }

    // Nedan kan tas bort?
    public String getFirstName(Author author) {
        return author.getFirstName();
    }

    public String getLastName(Author author) {
        return author.getLastName();
    }

    public LocalDate getBirthDate(Author author) {
        return author.getBirthDate();
    }

    public LocalDate getDeathDate(Author author) { // finns nödvändigtvis ej
        if (author.getDeathDate() == null) {
            System.out.println("Death date is null");
        }
        return author.getDeathDate();
    }
}