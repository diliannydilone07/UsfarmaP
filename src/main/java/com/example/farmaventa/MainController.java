package com.example.farmaventa;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private StackPane contentArea;

    @FXML private Button btnInicio;
    @FXML private Button btnVentas;
    @FXML private Button btnCompras;        // ← NUEVO
    @FXML private Button btnPersonas;
    @FXML private Button btnInventario;
    @FXML private Button btnReclamaciones;
    @FXML private Button btnConvenios;
    @FXML private Button btnEnvios;

    @FXML private Label lblUsuario;

    private Button btnActivo;

    private static final String ESTILO_ACTIVO =
            "-fx-background-color: #E8F5E9; -fx-text-fill: #1B5E20; " +
                    "-fx-font-weight: bold; -fx-font-size: 13px; " +
                    "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 12 10 12;";

    private static final String ESTILO_INACTIVO =
            "-fx-background-color: transparent; -fx-text-fill: #424242; " +
                    "-fx-font-size: 13px; -fx-background-radius: 6; " +
                    "-fx-cursor: hand; -fx-padding: 10 12 10 12;";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        resetarTodosLosBotones();
        cargarVista("Dashboard.fxml", btnInicio);
    }

    @FXML private void onMenuInicio()        { cargarVista("Dashboard.fxml",        btnInicio); }
    @FXML private void onMenuVentas()        { cargarVista("Ventas.fxml",           btnVentas); }
    @FXML private void onMenuCompras()       { cargarVista("Compra.fxml",           btnCompras); } // ← NUEVO
    @FXML private void onMenuPersonas()      { cargarVista("Personas.fxml",         btnPersonas); }
    @FXML private void onMenuInventario()    { cargarVista("Inventario.fxml",       btnInventario); }
    @FXML private void onMenuReclamaciones() { cargarVista("Reclamacionventa.fxml", btnReclamaciones); }
    @FXML private void onMenuConvenios()     { cargarVista("Convenio.fxml",         btnConvenios); }
    @FXML private void onMenuEnvios()        { cargarVista("Envio.fxml",            btnEnvios); }

    private void cargarVista(String nombreFxml, Button boton) {
        try {
            URL ruta = getClass().getResource("/com/example/farmaventa/" + nombreFxml);
            if (ruta == null) {
                System.err.println(">>> No se encontro el FXML: " + nombreFxml);
                return;
            }
            FXMLLoader loader = new FXMLLoader(ruta);
            Node vista = loader.load();
            contentArea.getChildren().setAll(vista);
            marcarBotonActivo(boton);
        } catch (IOException e) {
            System.err.println(">>> Error cargando: " + nombreFxml);
            e.printStackTrace();
        }
    }

    private void resetarTodosLosBotones() {
        List.of(btnInicio, btnVentas, btnCompras, btnPersonas,
                        btnInventario, btnReclamaciones, btnConvenios, btnEnvios)
                .forEach(b -> b.setStyle(ESTILO_INACTIVO));
        btnActivo = null;
    }

    private void marcarBotonActivo(Button nuevoActivo) {
        if (btnActivo != null) btnActivo.setStyle(ESTILO_INACTIVO);
        nuevoActivo.setStyle(ESTILO_ACTIVO);
        btnActivo = nuevoActivo;
    }

    public void navegarA(String nombreFxml, Button boton) {
        cargarVista(nombreFxml, boton != null ? boton : btnInicio);
    }

    public Button getBtnInicio()          { return btnInicio; }
    public Button getBtnVentas()          { return btnVentas; }
    public Button getBtnCompras()         { return btnCompras; }       // ← NUEVO
    public Button getBtnPersonas()        { return btnPersonas; }
    public Button getBtnInventario()      { return btnInventario; }
    public Button getBtnReclamaciones()   { return btnReclamaciones; }
    public Button getBtnConvenios()       { return btnConvenios; }
    public Button getBtnEnvios()          { return btnEnvios; }
}