package se.kth.duo.dblab1javafx.Model;

import java.time.LocalDate;

public class Author {
    private int authorID;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private LocalDate deathDate;

    public Author(int authorID, String firstName, String lastname, LocalDate birthDate, LocalDate deathDate) {
        this.authorID = authorID;
        this.firstName = firstName;
        this.lastName = lastname;
        this.birthDate = birthDate;
        this.deathDate = deathDate;
    }

    public Author() {}

    public int getAuthorID() {
        return authorID;
    }

    public void setAuthorID(int authorID) {
        this.authorID = authorID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public LocalDate getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(LocalDate deathDate) {
        this.deathDate = deathDate;
    }
}
