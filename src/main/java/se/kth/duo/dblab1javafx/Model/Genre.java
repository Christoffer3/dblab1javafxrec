package se.kth.duo.dblab1javafx.Model;

public class Genre {
    private int gID;
    private String genre;

    public Genre(int gID, String genre) {
        this.gID = gID;
        this.genre = genre;
    }

    public Genre() {}

    public int getgID() {
        return gID;
    }

    public void setgID(int gID) {
        this.gID = gID;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
