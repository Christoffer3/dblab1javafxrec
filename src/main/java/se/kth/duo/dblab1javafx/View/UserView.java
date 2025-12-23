package se.kth.duo.dblab1javafx.View;

import javafx.event.ActionEvent;
import se.kth.duo.dblab1javafx.Controller.*;
import se.kth.duo.dblab1javafx.Model.*;
import se.kth.duo.dblab1javafx.Controller.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class UserView {

    // TODO: bör ej ta emot controllers?
    private final BookController bookController;
    private final AuthorController authorController;
    private final ReviewController reviewController;
    private final UserController userController;
    private final Runnable logoutAction; // todo: justera från runnable

    // TODO: bör hanteras i controller?
    private User loggedInUser = null;

    private Button searchButton;
    private Button rateBookButton;
    private Button inputBookButton;
    private Button removeBookButton;
    private Button loginButton;
    private Button logOutButton;
    private Button userRateBookButton;
    private Button inputReviewButton;

    public UserView(BookController bookController,
                    AuthorController authorController,
                    ReviewController reviewController,
                    UserController userController,
                    Runnable logoutAction) {
        this.bookController = bookController;
        this.authorController = authorController;
        this.reviewController = reviewController;
        this.userController = userController;
        this.logoutAction = logoutAction;
    }


    public void showUserProfile(Stage stage) {
        stage.setTitle("User Menu");

        searchButton = new Button("Search book");
        rateBookButton = new Button("Rate book");
        loginButton = new Button("Login");

        inputBookButton = new Button("Insert book");
        removeBookButton = new Button("Remove book");
        userRateBookButton = new Button("User rate book");

        inputReviewButton = new Button("Write a Review");
        logOutButton = new Button("Log out");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(searchButton, 0, 0);
        grid.add(rateBookButton, 1, 0);
        grid.add(loginButton, 2, 0);
        grid.add(inputBookButton, 0, 1);
        grid.add(removeBookButton, 1, 1);
        grid.add(userRateBookButton, 0, 2);
        grid.add(inputReviewButton, 1, 2);
        grid.add(logOutButton, 2, 2);

        setLoggedIn(false); // todo flytta till controller

        /* Wiring */ // todo: ska flyttas till controller så den anropar metod i view
        searchButton.addEventHandler(ActionEvent.ACTION, e -> openSearchDialog(stage));

        rateBookButton.addEventHandler(ActionEvent.ACTION,e -> {
            try {
                openAnonymousRatingDialog(stage);
            } catch (Exception ex) {
                showError("Rating failed", ex);
            }
        });

        loginButton.addEventHandler(ActionEvent.ACTION, e -> openLoginDialog(stage));

        logOutButton.addEventHandler(ActionEvent.ACTION,e -> {
            loggedInUser = null;
            setLoggedIn(false);
            new Alert(Alert.AlertType.INFORMATION, "Logged out").showAndWait();
            if (logoutAction != null) logoutAction.run();
        });

        inputBookButton.addEventHandler(ActionEvent.ACTION,e -> {
            try {
                openInsertBookDialog(stage);
            } catch (Exception ex) {
                showError("Insert book failed", ex);
            }
        });

        userRateBookButton.addEventHandler(ActionEvent.ACTION,e -> {
            try {
                openUserRatingDialog(stage);
            } catch (Exception ex) {
                showError("User rating failed", ex);
            }
        });

        inputReviewButton.addEventHandler(ActionEvent.ACTION,e -> {
            try {
                openWriteReviewDialog(stage);
            } catch (Exception ex) {
                showError("Write review failed", ex);
            }
        });

        removeBookButton.addEventHandler(ActionEvent.ACTION,e -> {
            try {
                openRemoveBookDialog(stage);
            } catch (Exception ex) {
                showError("Remove book failed", ex);
            }
        });

        stage.setScene(new Scene(grid, 600, 300));
        stage.show();
    }


    // Vid search, visar table, sedan kan klicka på bok-rad i UI för få info om boks författare i Alert-meddelande
    private void openSearchDialog(Stage owner) {
        Dialog<SearchRequest> dialog = new Dialog<>();
        dialog.setResizable(true);
        dialog.setTitle("Search");
        dialog.initOwner(owner);

        ButtonType searchBtn = new ButtonType("Search", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(searchBtn, ButtonType.CANCEL);

        ComboBox<String> type = new ComboBox<>();
        type.getItems().addAll("Title", "Author", "ISBN", "Genre", "Rating");
        type.getSelectionModel().selectFirst();

        TextField input = new TextField();
        TextField first = new TextField();
        TextField last = new TextField();

        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.getChildren().addAll(new Label("Search type:"), type, new Label("Search input:"), input);

        type.addEventHandler(ActionEvent.ACTION,e -> {
            box.getChildren().clear();
            box.getChildren().addAll(new Label("Search type:"), type);
            if ("Author".equals(type.getValue())) {
                box.getChildren().addAll(new Label("First name:"), first, new Label("Last name:"), last);
            } else {
                box.getChildren().addAll(new Label("Search input:"), input);
            }
        });

        // TODO: justera så när väljer author syns i hela fönster
        dialog.getDialogPane().setContent(box);

        dialog.setResultConverter(btn -> {
            if (btn != searchBtn) return null;
            if ("Author".equals(type.getValue())) {
                return new SearchRequest(type.getValue(), null, first.getText(), last.getText());
            }
            return new SearchRequest(type.getValue(), input.getText(), null, null);
        });

        Optional<SearchRequest> res = dialog.showAndWait();
        if (res.isEmpty()) return;

        SearchRequest req = res.get();
        Task<List<Book>> task = new Task<>() { // TODO: justera så trådning i controlelr o ej Task
            @Override
            protected List<Book> call() throws Exception {
                switch (req.type) {
                    case "Title": return bookController.searchBookByTitle(req.input.trim());
                    case "ISBN": return bookController.searchBookByISBN(req.input.trim());
                    case "Genre": return bookController.searchBookByGenre(req.input.trim());
                    case "Rating": return bookController.searchBookByRating(Integer.parseInt(req.input.trim()));
                    case "Author": return bookController.searchBookByAuthor(req.first.trim(), req.last.trim());
                    default: return List.of();
                }
            }
        };

        task.setOnSucceeded(e -> {
            List<Book> books = task.getValue();
            if (books == null || books.isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION, "No results found.").showAndWait();
            } else {
                showBooksTable(owner, books);
            }
        });

        task.setOnFailed(e -> showError("Search failed", task.getException()));
        new Thread(task, "search-task").start();
    }

    // delmoment av search-delen
    private void showBooksTable(Stage owner, List<Book> books) {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.setTitle("Search results");

        TableView<Book> table = new TableView<>();

        table.getItems().addAll(books);

        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitle()));

        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getISBN()));

        TableColumn<Book, Integer> pagesCol = new TableColumn<>("Pages");
        pagesCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getPages()).asObject());

        table.getColumns().addAll(titleCol, isbnCol, pagesCol);

        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Visar författare i Alert-meddelande // todo: flytta till controller
        table.getSelectionModel().selectedItemProperty().addListener((e, oldBook, newBook) -> {
            if (newBook != null) {
                showAuthorDetails(newBook);
            }
        });

        BorderPane root = new BorderPane(table);
        root.setPadding(new Insets(10));
        stage.setScene(new Scene(root, 760, 420));
        stage.show();
    }

    // delmoment för search-delen
    private void showAuthorDetails(Book book) {
        StringBuilder msg = new StringBuilder("Authors:\n\n");
        for (Author a : book.getAuthors()) {
            msg.append("- ").append(a.getFirstName()).append(" ").append(a.getLastName());
            if (a.getBirthDate() != null) msg.append(" (born ").append(a.getBirthDate()).append(")");
            if (a.getDeathDate() != null) msg.append(" (died ").append(a.getDeathDate()).append(")");
            msg.append("\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Author information");
        alert.setHeaderText("Authors for: " + book.getTitle());
        alert.setContentText(msg.toString());
        alert.showAndWait();
    }


    /* följande tre metoder kopplat till inlägg av bok */
    private void openInsertBookDialog(Stage owner) throws DatabaseException {
        if (loggedInUser == null) {
            new Alert(Alert.AlertType.WARNING, "You must be logged in to insert books.").showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Insert Book");
        dialog.initOwner(owner);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(10); g.setPadding(new Insets(10));

        TextField title = new TextField();
        TextField pages = new TextField();
        TextField isbn = new TextField();
        TextField authors = new TextField();
        TextField genres = new TextField();
        authors.setPromptText("Karl Svensson, Hans Blomgren");
        genres.setPromptText("Fantasy, Children");

        g.addRow(0, new Label("Title:"), title);
        g.addRow(1, new Label("Pages:"), pages);
        g.addRow(2, new Label("ISBN:"), isbn);
        g.addRow(3, new Label("Authors:"), authors);
        g.addRow(4, new Label("Genres:"), genres);
        g.add(new Label("(Only existing authors/genres allowed)"), 0, 5, 2, 1);

        dialog.getDialogPane().setContent(g);

        Optional<ButtonType> button = dialog.showAndWait();
        if (button.isEmpty() || button.get() != ButtonType.OK) return;

        List<Author> authorList = parseAuthors(authors.getText());
        List<Genre> genreList = parseGenres(genres.getText());

        bookController.createBook( // todo: flytta så sker i controller
                title.getText().trim(),
                authorList,
                genreList,
                Integer.parseInt(pages.getText().trim()),
                isbn.getText().trim()
        );

        new Alert(Alert.AlertType.INFORMATION, "Book inserted!").showAndWait();
    }

    // delmetod av insert to Books
    private List<Author> parseAuthors(String raw) throws DatabaseException {
        List<Author> out = new ArrayList<>(); // todo: justera följande
        if (raw == null || raw.trim().isEmpty()) return out;

        for (String token : raw.split(",")) {
            String full = token.trim();
            if (full.isEmpty()) continue;

            int idx = full.lastIndexOf(' ');
            if (idx <= 0 || idx == full.length() - 1) {
                throw new DatabaseException("Author must be 'First Last'. Invalid: " + full);
            }
            String first = full.substring(0, idx).trim();
            String last  = full.substring(idx + 1).trim();

            Author a = new Author();
            a.setFirstName(first);
            a.setLastName(last);
            out.add(a);
        }
        return out;
    }

    // delmetod av insert to Books
    private List<Genre> parseGenres(String raw) {
        List<Genre> out = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) return out;

        for (String token : raw.split(",")) {
            String name = token.trim();
            if (name.isEmpty()) continue;
            out.add(new Genre(0, name));
        }
        return out;
    }


    /* Anonym rating av bok */
    private void openAnonymousRatingDialog(Stage owner) throws DatabaseException {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Rate book");
        dialog.initOwner(owner);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(10); g.setPadding(new Insets(10));

        TextField isbn = new TextField();
        ComboBox<Integer> rating = new ComboBox<>();
        rating.getItems().addAll(1,2,3,4,5);
        rating.getSelectionModel().selectFirst();

        g.addRow(0, new Label("Book ISBN:"), isbn);
        g.addRow(1, new Label("Rating (1-5):"), rating);

        dialog.getDialogPane().setContent(g);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) return;

        bookController.rateBookAnonymous(isbn.getText().trim(), rating.getValue());
        new Alert(Alert.AlertType.INFORMATION, "Rating saved!").showAndWait();
    }


    /* user rating av bok */
    private void openUserRatingDialog(Stage owner) throws DatabaseException {
        if (loggedInUser == null) {
            new Alert(Alert.AlertType.WARNING, "You must be logged in to rate.").showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("User rate book");
        dialog.initOwner(owner);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(10); g.setPadding(new Insets(10));

        TextField isbn = new TextField();
        ComboBox<Integer> rating = new ComboBox<>();
        rating.getItems().addAll(1,2,3,4,5);
        rating.getSelectionModel().selectFirst();

        g.addRow(0, new Label("Book ISBN:"), isbn);
        g.addRow(1, new Label("Rating (1-5):"), rating);

        dialog.getDialogPane().setContent(g);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) return;

        bookController.userRateBook(isbn.getText().trim(), loggedInUser.getUsername(), rating.getValue());
        new Alert(Alert.AlertType.INFORMATION, "Rating saved!").showAndWait();
    }


    /* Skriva review */
    private void openWriteReviewDialog(Stage owner) throws DatabaseException {
        if (loggedInUser == null) {
            new Alert(Alert.AlertType.WARNING, "You must be logged in to write a review.").showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Write a review");
        dialog.initOwner(owner);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(10); g.setPadding(new Insets(10));

        TextField isbn = new TextField();
        TextField text = new TextField();

        g.addRow(0, new Label("Book ISBN:"), isbn);
        g.addRow(1, new Label("Review text:"), text);

        dialog.getDialogPane().setContent(g);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) return;

        Book book = new Book();
        book.setISBN(isbn.getText().trim());

        User user = new User(); // todo: flytta till controller
        user.setUsername(loggedInUser.getUsername());

        reviewController.createReview(book, user, text.getText().trim());
        new Alert(Alert.AlertType.INFORMATION, "Review saved!").showAndWait();
    }


    /* ta bort bok */
    private void openRemoveBookDialog(Stage owner) throws DatabaseException {
        if (loggedInUser == null) {
            new Alert(Alert.AlertType.WARNING, "You must be logged in to remove books.").showAndWait();
            return;
        }

        TextInputDialog d = new TextInputDialog();
        d.setTitle("Remove book");
        d.setHeaderText("Enter ISBN to remove:");
        d.initOwner(owner);

        Optional<String> res = d.showAndWait();
        if (res.isEmpty()) return;

        String isbn = res.get().trim();
        if (isbn.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "ISBN is required.").showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete book " + isbn + "?\nThis will delete ratings/reviews/links too.",
                ButtonType.YES, ButtonType.NO);
        confirm.initOwner(owner);

        Optional<ButtonType> c = confirm.showAndWait();
        if (c.isEmpty() || c.get() != ButtonType.YES) return;

        bookController.removeBook(isbn);
        new Alert(Alert.AlertType.INFORMATION, "Book deleted.").showAndWait();
    }


    /* Inloggningsfönstret */
    private void openLoginDialog(Stage owner) {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Login");
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);

        ButtonType login = new ButtonType("Login", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(login, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField username = new TextField();
        PasswordField password = new PasswordField();

        grid.addRow(0, new Label("Username:"), username);
        grid.addRow(1, new Label("Password:"), password);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(pressedButton -> {
            if (pressedButton != login) return null;
            return new String[]{username.getText(), password.getText()};
        });

        Optional<String[]> res = dialog.showAndWait();
        if (res.isEmpty()) return;

        String[] inputLogin = res.get();
        if (inputLogin[0].isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Username required").showAndWait();
            return;
        }

        // Inloggning i bakgrunden av UI // TODO: ändra till rekomenderad trådning och i controller
        Task<User> task = new Task<>() {
            @Override
            protected User call() throws Exception {
                return userController.login(inputLogin[0], inputLogin[1]);
            }
        };

        task.setOnSucceeded(e -> {
            User u = task.getValue();
            if (u == null) {
                new Alert(Alert.AlertType.ERROR, "Wrong username or password").showAndWait();
                return;
            }
            loggedInUser = u;
            setLoggedIn(true);
            new Alert(Alert.AlertType.INFORMATION, "Logged in as: " + loggedInUser.getUsername()).showAndWait();
        });

        task.setOnFailed(e -> showError("Login failed", task.getException()));

        new Thread(task, "login-task").start();
    }

    // alternativ av/på beroende på om inloggad eller ej.
    private void setLoggedIn(boolean loggedIn) {
        searchButton.setDisable(false); // alla kan alltid söka
        rateBookButton.setDisable(loggedIn); // endast anonyma (icke-inloggade kan)
        loginButton.setDisable(loggedIn);
        // endast inloggade kan gära följande
        userRateBookButton.setDisable(!loggedIn);
        inputBookButton.setDisable(!loggedIn);
        removeBookButton.setDisable(!loggedIn);
        inputReviewButton.setDisable(!loggedIn);
        logOutButton.setDisable(!loggedIn);
    }


    /* hjälp-metoder */
    private void showError(String title, Throwable ex) {
        ex.printStackTrace();
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle(title);
            a.setHeaderText(title);
            a.setContentText(ex.getMessage() == null ? ex.toString() : ex.getMessage());
            a.showAndWait();
        });
    }

    private static class SearchRequest {
        final String type;
        final String input;
        final String first;
        final String last;
        SearchRequest(String type, String input, String first, String last) {
            this.type = type; this.input = input == null ? "" : input;
            this.first = first == null ? "" : first;
            this.last = last == null ? "" : last;
        }
    }
}