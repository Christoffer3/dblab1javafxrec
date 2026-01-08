package se.kth.duo.dblab1javafx.Model;

public class SearchException extends DatabaseException {
    private final String searchType;
    private final String searchValue;

    public SearchException(String message, String searchType, String searchValue, Throwable cause) {
        super(String.format("Connection error searching %s [%s]: %s", searchType, searchValue, message), cause);
        this.searchType = searchType;
        this.searchValue = searchValue;
    }
}