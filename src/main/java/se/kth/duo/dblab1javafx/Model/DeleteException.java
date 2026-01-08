package se.kth.duo.dblab1javafx.Model;

public class DeleteException extends DatabaseException {
    public DeleteException(String message, String id) {
        super(message + " [ID: " + id + "]");
    }

    public DeleteException(String message, String id, Throwable cause) {
        super(message + " [ID: " + id + "]", cause);
    }
}