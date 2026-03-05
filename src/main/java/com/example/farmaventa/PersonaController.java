package com.example.farmaventa;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class PersonaController {

    @FXML private RadioButton rbEmpleado;
    @FXML private RadioButton rbCliente;
    @FXML private VBox vboxDatosEmpleado;

    // Campos de Persona
    @FXML private TextField txtId, txtNombre, txtApellido, txtTelefono, txtEmail;
    @FXML private ComboBox<String> cmbGenero;


    @FXML private ComboBox<String> cmbCargo;
    @FXML private DatePicker dpHorario;

    @FXML
    public void initialize() {

        cmbGenero.getItems().addAll("Masculino", "Femenino", "Otro");
        cmbCargo.getItems().addAll("Administrador", "Farmacéutico", "Vendedor", "Cajero");


        rbEmpleado.selectedProperty().addListener((obs, estabaSeleccionado, ahoraSeleccionado) -> {
            vboxDatosEmpleado.setVisible(ahoraSeleccionado);
            vboxDatosEmpleado.setManaged(ahoraSeleccionado);

            if (!ahoraSeleccionado) {
                cmbCargo.setValue(null);
                dpHorario.setValue(null);
            }
        });
    }
}