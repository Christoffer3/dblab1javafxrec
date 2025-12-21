package se.kth.duo.dblab1javafx.Model;

import java.util.ArrayList;
import java.util.List;

public class Book {
    private String title;
    private String ISBN;
    private int pages;

    private List<Author> authors = new ArrayList<>();
    private List<Genre> genres = new ArrayList<>();

    public Book(String title, int pages, String ISBN) {
        this.title = title;
        this.pages = pages;
        this.ISBN = ISBN;
    }

    public Book() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }
}
