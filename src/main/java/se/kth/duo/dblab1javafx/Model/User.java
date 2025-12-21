package se.kth.duo.dblab1javafx.Model;

import java.time.LocalDate;

public class User {
    private String username;
    private LocalDate accountCreationDate;

    public User(String username, LocalDate accountCreationDate) {
        this.username = username;
        this.accountCreationDate = accountCreationDate;
    }

    public User() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDate getAccountCreationDate() {
        return accountCreationDate;
    }

    public void setAccountCreationDate(LocalDate accountCreationDate) {
        this.accountCreationDate = accountCreationDate;
    }

}
