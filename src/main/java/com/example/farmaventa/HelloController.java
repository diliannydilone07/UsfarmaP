package com.example.farmaventa;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class HelloController {

    @FXML
    private TableView<?> tablaVentaProducto;

    @FXML
    private Label lblMontoTotal;

    @FXML
    private Label lblMontoPendiente;

    @FXML
    public void initialize() {
        lblMontoTotal.setText("0.00");
        lblMontoPendiente.setText("0.00");
    }
}
