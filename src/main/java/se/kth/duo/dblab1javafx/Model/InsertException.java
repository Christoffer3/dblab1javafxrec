package se.kth.duo.dblab1javafx.Model;

public class InsertException extends DatabaseException {
    private String entityType;
    private String identifier;

    public InsertException(String message, String type, String id, String reason) {
        super(message + " [" + type + " ID: " + id + "]: " + reason);
    }

    public InsertException(String message, String type, String id, Throwable cause) {
        super(message + " [" + type + " ID: " + id + "]", cause);
    }
}