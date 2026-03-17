package com.example.farmaventa;

import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.Persona;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import javax.swing.JOptionPane;
import java.sql.*;

public class PersonaController {

    Conexion conexion = new Conexion();

    @FXML private TextField        txtId;
    @FXML private TextField        txtNombre;
    @FXML private TextField        txtApellido;
    @FXML private TextField        txtTelefono;
    @FXML private TextField        txtEmail;
    @FXML private ComboBox<String> cmbGenero;
    @FXML private ComboBox<String> cmbCargo;
    @FXML private DatePicker       dpHorario;
    @FXML private RadioButton      rbCliente;
    @FXML private RadioButton      rbEmpleado;
    @FXML private VBox             vboxDatosEmpleado;
    @FXML private TextField        txtBusqueda;

    @FXML private TableView<Persona>           tablaPersonas;
    @FXML private TableColumn<Persona, Number> colId;
    @FXML private TableColumn<Persona, String> colNombre;
    @FXML private TableColumn<Persona, String> colTipo;
    @FXML private TableColumn<Persona, String> colCargo;
    @FXML private TableColumn<Persona, String> colTelefono;
    @FXML private TableColumn<Persona, String> colEmail;

    @FXML
    public void initialize() {
        cmbGenero.getItems().addAll("Masculino", "Femenino", "Otro");
        cargarComboCargos();

        // mostrar/ocultar sección empleado
        rbEmpleado.selectedProperty().addListener((obs, old, seleccionado) -> {
            vboxDatosEmpleado.setVisible(seleccionado);
            vboxDatosEmpleado.setManaged(seleccionado);
            if (!seleccionado) { cmbCargo.setValue(null); dpHorario.setValue(null); }
        });

        // al hacer click en la tabla, llenar el formulario
        tablaPersonas.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) cargarEnFormulario(sel);
        });

        colId.setCellValueFactory(c -> c.getValue().idPersonaProperty());
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colTipo.setCellValueFactory(c -> c.getValue().tipoProperty());
        colCargo.setCellValueFactory(c -> c.getValue().cargoProperty());
        colTelefono.setCellValueFactory(c -> c.getValue().telefonoProperty());
        colEmail.setCellValueFactory(c -> c.getValue().emailProperty());

        actualizarTabla();
    }

    private void cargarComboCargos() {
        String sql = "SELECT nombre FROM TBL_CARGO";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cmbCargo.getItems().add(rs.getString("nombre"));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar cargos: " + e.getMessage());
        }
    }

    // Botón "💾 Guardar" → onAction="#onGuardar"
    @FXML
    public void onGuardar(ActionEvent event) {
        if (txtNombre.getText().isBlank() || txtApellido.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Nombre y Apellido son obligatorios.");
            return;
        }

        String sqlPersona = "INSERT INTO TBL_PERSONA (nombre, apellido, genero, numero_telefono, correo_electronico) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sqlPersona, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, txtNombre.getText().trim());
            ps.setString(2, txtApellido.getText().trim());
            ps.setString(3, cmbGenero.getValue() != null ? cmbGenero.getValue() : "");
            ps.setString(4, txtTelefono.getText().trim());
            ps.setString(5, txtEmail.getText().trim());
            ps.executeUpdate();

            // obtener el id generado
            int idPersona = -1;
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) idPersona = keys.getInt(1);

            // insertar en TBL_CLIENTE o TBL_EMPLEADO
            if (rbEmpleado.isSelected()) {
                insertarEmpleado(con, idPersona);
            } else {
                PreparedStatement psC = con.prepareStatement("INSERT INTO TBL_CLIENTE (id_persona) VALUES (?)");
                psC.setInt(1, idPersona);
                psC.executeUpdate();
            }

            JOptionPane.showMessageDialog(null, "Persona guardada correctamente.");
            actualizarTabla();
            Limpiar();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al guardar: " + e.getMessage());
        }
    }

    private void insertarEmpleado(Connection con, int idPersona) throws SQLException {
        if (dpHorario.getValue() == null || cmbCargo.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona horario y cargo para el empleado.");
            return;
        }
        // buscar id_cargo por nombre
        int idCargo = -1;
        PreparedStatement psCargo = con.prepareStatement("SELECT id_cargo FROM TBL_CARGO WHERE nombre = ?");
        psCargo.setString(1, cmbCargo.getValue());
        ResultSet rs = psCargo.executeQuery();
        if (rs.next()) idCargo = rs.getInt("id_cargo");

        if (idCargo == -1) { JOptionPane.showMessageDialog(null, "Cargo no encontrado."); return; }

        PreparedStatement psE = con.prepareStatement(
                "INSERT INTO TBL_EMPLEADO (horario_trabajo, id_persona, id_cargo) VALUES (?, ?, ?)");
        psE.setString(1, dpHorario.getValue().toString() + " 08:00:00");
        psE.setInt(2, idPersona);
        psE.setInt(3, idCargo);
        psE.executeUpdate();
    }

    // Botón "✏ Editar Seleccionado" → onAction="#onEditar"
    @FXML
    public void onEditar(ActionEvent event) {
        if (txtId.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Selecciona una persona de la tabla primero.");
            return;
        }
        String sql = "UPDATE TBL_PERSONA SET nombre=?, apellido=?, genero=?, numero_telefono=?, correo_electronico=? "
                + "WHERE id_persona=?";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, txtNombre.getText().trim());
            ps.setString(2, txtApellido.getText().trim());
            ps.setString(3, cmbGenero.getValue() != null ? cmbGenero.getValue() : "");
            ps.setString(4, txtTelefono.getText().trim());
            ps.setString(5, txtEmail.getText().trim());
            ps.setInt(6,    Integer.parseInt(txtId.getText().trim()));
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "Persona actualizada.");
            actualizarTabla();
            Limpiar();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al modificar: " + e.getMessage());
        }
    }

    // Botón "🗑 Eliminar" → onAction="#onEliminar"
    @FXML
    public void onEliminar(ActionEvent event) {
        if (txtId.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Selecciona una persona de la tabla primero.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(null, "¿Eliminar esta persona?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int idPersona = Integer.parseInt(txtId.getText().trim());

        try (Connection con = conexion.establecerConexion()) {

            // 1. Borrar de TBL_CLIENTE si existe (hijo antes que el padre)
            PreparedStatement psC = con.prepareStatement(
                    "DELETE FROM TBL_CLIENTE WHERE id_persona=?");
            psC.setInt(1, idPersona);
            psC.executeUpdate();

            // 2. Borrar de TBL_EMPLEADO si existe (hijo antes que el padre)
            PreparedStatement psE = con.prepareStatement(
                    "DELETE FROM TBL_EMPLEADO WHERE id_persona=?");
            psE.setInt(1, idPersona);
            psE.executeUpdate();

            // 3. Ahora sí borrar de TBL_PERSONA
            PreparedStatement psP = con.prepareStatement(
                    "DELETE FROM TBL_PERSONA WHERE id_persona=?");
            psP.setInt(1, idPersona);
            psP.executeUpdate();

            JOptionPane.showMessageDialog(null, "Persona eliminada.");
            actualizarTabla();
            Limpiar();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar: " + e.getMessage());
        }
    }

    // Botón "✖" limpiar → onAction="#onLimpiar"  (el ✖ pequeño del formulario)
    @FXML
    public void onLimpiar(ActionEvent event) { Limpiar(); }

    @FXML
    public void Limpiar() {
        txtId.clear();
        txtNombre.clear();
        txtApellido.clear();
        txtTelefono.clear();
        txtEmail.clear();
        cmbGenero.setValue(null);
        cmbCargo.setValue(null);
        dpHorario.setValue(null);
        rbCliente.setSelected(true);
        tablaPersonas.getSelectionModel().clearSelection();
    }

    private void actualizarTabla() {
        String sql = "SELECT p.id_persona, p.nombre, p.apellido, p.genero, "
                + "p.numero_telefono, p.correo_electronico, "
                + "CASE WHEN c.id_cliente IS NOT NULL THEN 'Cliente' ELSE 'Empleado' END AS tipo, "
                + "ISNULL(ca.nombre, '') AS cargo "
                + "FROM TBL_PERSONA p "
                + "LEFT JOIN TBL_CLIENTE  c  ON c.id_persona = p.id_persona "
                + "LEFT JOIN TBL_EMPLEADO e  ON e.id_persona = p.id_persona "
                + "LEFT JOIN TBL_CARGO    ca ON ca.id_cargo  = e.id_cargo";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ObservableList<Persona> lista = FXCollections.observableArrayList();
            while (rs.next()) {
                lista.add(new Persona(
                        rs.getInt("id_persona"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("tipo"),
                        rs.getString("numero_telefono"),
                        rs.getString("correo_electronico"),
                        rs.getString("genero"),
                        rs.getString("cargo")
                ));
            }
            tablaPersonas.setItems(lista);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar personas: " + e.getMessage());
        }
    }

    private void cargarEnFormulario(Persona p) {
        txtId.setText(String.valueOf(p.getIdPersona()));
        txtNombre.setText(p.getNombre());
        txtApellido.setText(p.getApellido());
        txtTelefono.setText(p.getTelefono());
        txtEmail.setText(p.getEmail());
        cmbGenero.setValue(p.getGenero());
        if ("Empleado".equals(p.getTipo())) {
            rbEmpleado.setSelected(true);
            cmbCargo.setValue(p.getCargo());
        } else {
            rbCliente.setSelected(true);
        }
    }
}