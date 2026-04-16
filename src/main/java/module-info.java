module com.example.farmaventa {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.desktop;
    requires java.sql;

    opens com.example.farmaventa to javafx.fxml;
    exports com.example.farmaventa;
    exports com.example.farmaventa.modelo;
    opens com.example.farmaventa.modelo to javafx.fxml;
    exports Usuarios;
    opens Usuarios to javafx.fxml;
}