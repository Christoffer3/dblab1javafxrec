package se.kth.duo.dblab1javafx.View;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import se.kth.duo.dblab1javafx.Controller.*;
import se.kth.duo.dblab1javafx.Model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserView {

    private final BookController bookController;
    private final AuthorController authorController;
    private final ReviewController reviewController;
    private final UserController userController;

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
                    UserController userController) {
        this.bookController = bookController;
        this.authorController = authorController;
        this.reviewController = reviewController;
        this.userController = userController;
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

        availableButtons(); // för vilke knappar kan välja i session

        /* Wiring */
        searchButton.addEventHandler(ActionEvent.ACTION, e -> openSearchDialog(stage));
        rateBookButton.addEventHandler(ActionEvent.ACTION, e -> openAnonymousRatingDialog(stage));
        loginButton.addEventHandler(ActionEvent.ACTION, e -> openLoginDialog(stage));
        inputBookButton.addEventHandler(ActionEvent.ACTION, e -> openInsertBookDialog(stage));
        userRateBookButton.addEventHandler(ActionEvent.ACTION, e -> openUserRatingDialog(stage));
        inputReviewButton.addEventHandler(ActionEvent.ACTION, e -> openWriteReviewDialog(stage));
        removeBookButton.addEventHandler(ActionEvent.ACTION, e -> openRemoveBookDialog(stage));

        logOutButton.addEventHandler(ActionEvent.ACTION, e -> {
            userController.logout();
            availableButtons(); // gör inloggade alternativ ej tillgängliga längre
            new Alert(Alert.AlertType.INFORMATION, "Logged out").showAndWait();
        });

        stage.setScene(new Scene(grid, 600, 300));
        stage.show();
    }

    // hjälp-metod för se vilka knappar tillgängliga
    private void availableButtons() {
        boolean loggedIn = userController.isLoggedIn();
        searchButton.setDisable(false); // alla kan söka
        rateBookButton.setDisable(loggedIn); // endast anonym får anonym rating
        loginButton.setDisable(loggedIn);
        // nedan endast inloggade tillåtna göra
        userRateBookButton.setDisable(!loggedIn);
        inputBookButton.setDisable(!loggedIn);
        removeBookButton.setDisable(!loggedIn);
        inputReviewButton.setDisable(!loggedIn);
        logOutButton.setDisable(!loggedIn);
    }

    // Vid search, visar table, sedan kan klicka på bok-rad i UI för få info om boks författare i Alert-meddelande
    private void openSearchDialog(Stage owner) {
        Dialog<Search> dialog = new Dialog<>();
        dialog.setResizable(true);
        dialog.setTitle("Search");
        dialog.initOwner(owner);

        ButtonType searchButton = new ButtonType("Search", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(searchButton, ButtonType.CANCEL);

        ComboBox<String> type = new ComboBox<>();
        type.getItems().addAll("Title", "Author", "ISBN", "Genre", "Rating");
        type.getSelectionModel().selectFirst();

        TextField input = new TextField();
        TextField first = new TextField();
        TextField last = new TextField();

        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.getChildren().addAll(new Label("Search via:"), type, new Label("input:"), input);

        type.addEventHandler(ActionEvent.ACTION, e -> {
            box.getChildren().clear();
            box.getChildren().addAll(new Label("Search via:"), type);
            if ("Author".equals(type.getValue())) { // TODO: justera så när väljer author syns i hela fönster
                box.getChildren().addAll(new Label("First name:"), first, new Label("Last name:"), last);
            } else {
                box.getChildren().addAll(new Label("Search via:"), input);
            }
        });

        dialog.getDialogPane().setContent(box);

        dialog.setResultConverter(button -> {
            if (button != searchButton) return null;
            if ("Author".equals(type.getValue())) {
                return new Search(type.getValue(), null, first.getText(), last.getText());
            }
            return new Search(type.getValue(), input.getText(), null, null);
        });

        Optional<Search> res = dialog.showAndWait();
        if (res.isEmpty()) return;

        Search req = res.get();

        /* gör sökning i databasen */
        bookController.searchAsync( // TODO: justera anropet
                req.type, req.input, req.first, req.last,
                books -> {
                    if (books == null || books.isEmpty()) {
                        new Alert(Alert.AlertType.INFORMATION, "No results found.").showAndWait();
                    } else {
                        showBooksTable(owner, books);
                    }
                },
                ex -> showError("Search failed", ex)
        );
    }

    // resulterande tabell av search
    private void showBooksTable(Stage owner, List<Book> books) {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.setTitle("Search results");

        TableView<Book> table = new TableView<>();
        table.getItems().setAll(books);

        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitle()));

        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getISBN()));

        TableColumn<Book, Integer> pagesCol = new TableColumn<>("Pages");
        pagesCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getPages()).asObject());

        table.getColumns().addAll(titleCol, isbnCol, pagesCol);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // ser efter val av rad i tabellen
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldBook, newBook) -> {
            if (newBook != null) {
                showAuthorDetails(newBook); // Visar författare för vald bok i Alert-meddelande
            }
        });

        BorderPane root = new BorderPane(table);
        root.setPadding(new Insets(10));
        stage.setScene(new Scene(root, 760, 420));
        stage.show();
    }

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
    private void openInsertBookDialog(Stage owner) {
        if (!userController.isLoggedIn()) {
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

        try {
            List<Author> authorList = parseAuthors(authors.getText());
            List<Genre> genreList = parseGenres(genres.getText());

            bookController.createBookAsync(title.getText().trim(), authorList, genreList, Integer.parseInt(pages.getText().trim()), isbn.getText().trim(),
                    book -> {
                        new Alert(Alert.AlertType.INFORMATION, "Book inserted!").showAndWait();
                    },
                    ex -> {
                        showError("Insert book failed", ex);
                    }
            );
        } catch (NumberFormatException nfe) {
            showError("Invalid input", new Exception("Pages must be a number"));
        } catch (Exception ex) {
            showError("Request failed", ex);
        }
    }

    // skapa författarna utifrån input TODO: justera, alt. fast comboBox lsita med befintliga
    private List<Author> parseAuthors(String raw) throws DatabaseException {
        List<Author> out = new ArrayList<>();
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

    // skapa genrerna utifrån input TODO: justera, -||-
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


    /* anonym-rating */
    private void openAnonymousRatingDialog(Stage owner) {
        if (userController.isLoggedIn()) { // inloggade kan ej göra anonyma
            new Alert(Alert.AlertType.WARNING, "Logged in users must use 'User rate book'.").showAndWait();
            return;
        }

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

        try {
            String isbnStr = isbn.getText().trim();
            int ratingVal = rating.getValue();

            bookController.rateBookAnonymousAsync(isbnStr, ratingVal,
                    () -> {
                        new Alert(Alert.AlertType.INFORMATION, "Rating saved!").showAndWait();
                    },
                    ex -> {
                        showError("Rating of book failed", ex);
                    }
            );
        } catch (Exception ex) {
            showError("Request failed", ex);
        }
    }


    /* user rating av bok */
    private void openUserRatingDialog(Stage owner) {
        if (!userController.isLoggedIn()) {
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

        try {
            String username = userController.getLoggedInUser().getUsername();
            String isbnStr = isbn.getText().trim();
            int ratingVal = rating.getValue();

            bookController.userRateBookAsync(isbnStr, username, ratingVal,
                    () -> {
                        new Alert(Alert.AlertType.INFORMATION, "Rating saved!").showAndWait();
                    },
                    ex -> {
                        showError("User rating failed", ex);
                    }
            );
        } catch (Exception ex) {
            showError("Request failed", ex);
        }
    }

    /* recension av bok */
    private void openWriteReviewDialog(Stage owner) {
        if (!userController.isLoggedIn()) {
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

        try {
            Book book = new Book();
            book.setISBN(isbn.getText().trim());
            User user = userController.getLoggedInUser();
            reviewController.createReviewAsync(book, user, text.getText().trim(),
                    review -> {
                        new Alert(Alert.AlertType.INFORMATION, "Review saved!").showAndWait();
                        text.clear();
                    },
                    ex -> {
                        showError("Write a review failed", ex);
                    }
            );
        } catch (Exception ex) {
            showError("Request failed", ex);
        }
    }

    /* borttagning av bok */
    private void openRemoveBookDialog(Stage owner) {
        if (!userController.isLoggedIn()) {
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

        try {
            String isbnStr = isbn.trim();

            bookController.removeBookAsync(isbnStr,
                    () -> {
                        new Alert(Alert.AlertType.INFORMATION, "Book deleted.").showAndWait();
                    },
                    ex -> {
                        showError("Remove book failed", ex);
                    }
            );
        } catch (Exception ex) {
            showError("Request failed", ex);
        }
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
        if (inputLogin[0] == null || inputLogin[0].trim().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Username required").showAndWait();
            return;
        }

        userController.loginAsync(inputLogin[0], inputLogin[1],
                user -> {
                    if (user == null) {
                        new Alert(Alert.AlertType.ERROR, "Wrong username or password").showAndWait();
                        return;
                    }
                    availableButtons();
                    new Alert(Alert.AlertType.INFORMATION, "Logged in as: " + user.getUsername()).showAndWait();
                },
                ex -> showError("Login failed", ex)
        );
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

    private static class Search {
        final String type;
        final String input;
        final String first;
        final String last;

        Search(String type, String input, String first, String last) {
            this.type = type;
            this.input = input == null ? "" : input;
            this.first = first == null ? "" : first;
            this.last = last == null ? "" : last;
        }
    }
}