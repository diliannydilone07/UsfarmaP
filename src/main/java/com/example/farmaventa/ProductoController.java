package com.example.farmaventa;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ProductoController {


    @FXML private TextField txtBusqueda;
    @FXML private TableView<?> tablaProductos;
    @FXML private TableColumn<?, ?> colId;
    @FXML private TableColumn<?, ?> colNombre;
    @FXML private TableColumn<?, ?> colStock;
    @FXML private TableColumn<?, ?> colPrecio;
    @FXML private TableColumn<?, ?> colUbicacion;
    @FXML private Spinner<Integer> spinCantidad;


    @FXML private TextField txtNuevoNombre;
    @FXML private TextField txtNuevoDescuento;
    @FXML private TextField txtStockMinimo;
    @FXML private TextField txtStockActual;
    @FXML private TextField txtNuevaUbicacion;
    @FXML private ComboBox<String> comboCategoria;

    @FXML
    public void initialize() {

        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        spinCantidad.setValueFactory(valueFactory);


        if (comboCategoria != null) {
            comboCategoria.getItems().addAll("Analgesicos", "Antibióticos", "Cuidado Personal");
        }
    }


    @FXML
    public void onGuardarProductoClick(ActionEvent event) {
        String nombre = txtNuevoNombre.getText();
        String stock = txtStockActual.getText();

        System.out.println("Guardando producto: " + nombre + " con stock: " + stock);


        limpiarCampos();
    }

    private void limpiarCampos() {
        txtNuevoNombre.clear();
        txtNuevoDescuento.clear();
        txtStockMinimo.clear();
        txtStockActual.clear();
        txtNuevaUbicacion.clear();
    }

    @FXML
    public void onAñadirClick(ActionEvent event) {
        System.out.println("Producto añadido a la compra");
    }

    @FXML
    public void onCancelarClick(ActionEvent event) {
        System.out.println("Acción cancelada");
    }
}