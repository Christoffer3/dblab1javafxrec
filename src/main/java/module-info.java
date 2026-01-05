module se.kth.duo.dblab1javafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;
    requires javafx.base;

    opens se.kth.duo.dblab1javafx to javafx.base;
    exports se.kth.duo.dblab1javafx;
    exports se.kth.duo.dblab1javafx.Model;
    opens se.kth.duo.dblab1javafx.Model to javafx.base;
}
