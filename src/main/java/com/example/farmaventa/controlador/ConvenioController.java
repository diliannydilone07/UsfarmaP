package com.example.farmaventa.controlador;

import Usuarios.Permisos;
import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.Convenio;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javax.swing.JOptionPane;
import java.sql.*;

public class ConvenioController {

    Conexion conexion = new Conexion();


    @FXML private Button btnGuardarConvenio;
    @FXML private Button btnEditarConvenio;
    @FXML private Button btnEliminarConvenio;

    @FXML private TextField  txtIdProveedor;
    @FXML private TextField  txtIdProducto;
    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;
    @FXML private TextArea   taAcuerdo;
    @FXML private Label      lblVigencia;

    @FXML private TextField                     txtBuscar;
    @FXML private TableView<Convenio>           tablaConvenios;
    @FXML private TableColumn<Convenio, Number> colId;
    @FXML private TableColumn<Convenio, String> colProveedor;
    @FXML private TableColumn<Convenio, String> colProducto;
    @FXML private TableColumn<Convenio, String> colFechaInicio;
    @FXML private TableColumn<Convenio, String> colFechaFin;
    @FXML private TableColumn<Convenio, String> colVigencia;

    private ObservableList<Convenio> listaConvenios = FXCollections.observableArrayList();
    private int idConvenioSeleccionado = -1;

    @FXML
    public void initialize() {
        dpFechaInicio.setValue(java.time.LocalDate.now());
        dpFechaFin.setValue(java.time.LocalDate.now().plusMonths(6));

        colId.setCellValueFactory(c -> c.getValue().idConvenioProperty());
        colProveedor.setCellValueFactory(c -> c.getValue().proveedorProperty());
        colProducto.setCellValueFactory(c -> c.getValue().productoProperty());
        colFechaInicio.setCellValueFactory(c -> c.getValue().fechaInicioProperty());
        colFechaFin.setCellValueFactory(c -> c.getValue().fechaFinProperty());
        colVigencia.setCellValueFactory(c -> c.getValue().vigenciaProperty());

        tablaConvenios.setItems(listaConvenios);

        tablaConvenios.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSel, newSel) -> {
                    if (newSel != null) cargarEnFormulario(newSel);
                });

        Permisos.aplicarBtn(btnGuardarConvenio,  Permisos.Accion.REGISTRAR);
        Permisos.aplicarBtn(btnEditarConvenio,   Permisos.Accion.EDITAR);
        Permisos.aplicarBtn(btnEliminarConvenio, Permisos.Accion.ELIMINAR);
        cargarConvenios();
    }

    @FXML
    public void cargarConvenios() {
        listaConvenios.clear();
        idConvenioSeleccionado = -1;

        String sql = "SELECT c.id_convenio, " +
                "CONVERT(VARCHAR(10), c.fecha_inicio, 120) AS fecha_inicio, " +
                "CONVERT(VARCHAR(10), c.fecha_fin, 120) AS fecha_fin, " +
                "c.acuerdo, " +
                "p.nombre AS nombre_proveedor, " +
                "pr.nombre AS nombre_producto, " +
                "c.id_proveedor, c.id_producto " +
                "FROM TBL_CONVENIO c " +
                "JOIN TBL_PROVEEDOR p  ON p.id_proveedor = c.id_proveedor " +
                "JOIN TBL_PRODUCTO  pr ON pr.id_producto  = c.id_producto " +
                "ORDER BY c.id_convenio DESC";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                listaConvenios.add(new Convenio(
                        rs.getInt("id_convenio"),
                        rs.getString("fecha_inicio"),
                        rs.getString("fecha_fin"),
                        rs.getString("acuerdo"),
                        rs.getString("nombre_proveedor"),
                        rs.getString("nombre_producto"),
                        rs.getInt("id_proveedor"),
                        rs.getInt("id_producto")
                ));
            }
            tablaConvenios.setItems(listaConvenios);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar convenios: " + e.getMessage());
        }
    }

    @FXML
    public void fnBuscar() {
        String busqueda = txtBuscar.getText().trim().toLowerCase();
        if (busqueda.isEmpty()) {
            cargarConvenios();
            return;
        }

        ObservableList<Convenio> listaFiltrada = FXCollections.observableArrayList();
        for (Convenio c : listaConvenios) {
            if (c.getProveedor().toLowerCase().contains(busqueda)
                    || c.getProducto().toLowerCase().contains(busqueda)
                    || c.getAcuerdo().toLowerCase().contains(busqueda)) {
                listaFiltrada.add(c);
            }
        }
        tablaConvenios.setItems(listaFiltrada);
    }

    @FXML
    public void onGuardarConvenio(ActionEvent event) {
        if (!validarFormulario()) return;

        String sql = "INSERT INTO TBL_CONVENIO (fecha_inicio, fecha_fin, acuerdo, id_proveedor, id_producto) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1,   Date.valueOf(dpFechaInicio.getValue()));
            ps.setDate(2,   Date.valueOf(dpFechaFin.getValue()));
            ps.setString(3, taAcuerdo.getText().trim());
            ps.setInt(4,    Integer.parseInt(txtIdProveedor.getText().trim()));
            ps.setInt(5,    Integer.parseInt(txtIdProducto.getText().trim()));
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "Convenio registrado correctamente.");
            limpiar();
            cargarConvenios();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al guardar convenio: " + e.getMessage());
        }
    }

    @FXML
    public void onEditarConvenio(ActionEvent event) {
        if (idConvenioSeleccionado == -1) {
            JOptionPane.showMessageDialog(null, "Selecciona un convenio de la tabla primero.");
            return;
        }
        if (!validarFormulario()) return;

        String sql = "UPDATE TBL_CONVENIO " +
                "SET fecha_inicio = ?, fecha_fin = ?, acuerdo = ?, id_proveedor = ?, id_producto = ? " +
                "WHERE id_convenio = ?";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1,   Date.valueOf(dpFechaInicio.getValue()));
            ps.setDate(2,   Date.valueOf(dpFechaFin.getValue()));
            ps.setString(3, taAcuerdo.getText().trim());
            ps.setInt(4,    Integer.parseInt(txtIdProveedor.getText().trim()));
            ps.setInt(5,    Integer.parseInt(txtIdProducto.getText().trim()));
            ps.setInt(6,    idConvenioSeleccionado);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "Convenio #" + idConvenioSeleccionado + " actualizado.");
            limpiar();
            cargarConvenios();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al editar convenio: " + e.getMessage());
        }
    }

    @FXML
    public void onEliminarConvenio(ActionEvent event) {
        if (idConvenioSeleccionado == -1) {
            JOptionPane.showMessageDialog(null, "Selecciona un convenio de la tabla primero.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(null,
                "¿Eliminar el convenio #" + idConvenioSeleccionado + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM TBL_CONVENIO WHERE id_convenio = ?";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idConvenioSeleccionado);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Convenio eliminado.");
            limpiar();
            cargarConvenios();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar: " + e.getMessage());
        }
    }

    @FXML
    public void limpiar() {
        txtIdProveedor.clear();
        txtIdProducto.clear();
        taAcuerdo.clear();
        txtBuscar.clear();
        dpFechaInicio.setValue(java.time.LocalDate.now());
        dpFechaFin.setValue(java.time.LocalDate.now().plusMonths(6));
        idConvenioSeleccionado = -1;
        tablaConvenios.getSelectionModel().clearSelection();
        tablaConvenios.setItems(listaConvenios);
        if (lblVigencia != null) lblVigencia.setText("");
    }

    private void cargarEnFormulario(Convenio c) {
        idConvenioSeleccionado = c.getIdConvenio();
        txtIdProveedor.setText(String.valueOf(c.getIdProveedor()));
        txtIdProducto.setText(String.valueOf(c.getIdProducto()));
        taAcuerdo.setText(c.getAcuerdo());

        try {
            dpFechaInicio.setValue(java.time.LocalDate.parse(c.getFechaInicio().substring(0, 10)));
            dpFechaFin.setValue(java.time.LocalDate.parse(c.getFechaFin().substring(0, 10)));
        } catch (Exception ignored) {}

        if (lblVigencia != null && dpFechaFin.getValue() != null) {
            boolean vigente = !dpFechaFin.getValue().isBefore(java.time.LocalDate.now());
            lblVigencia.setText(vigente ? "Vigente" : "Vencido");
            lblVigencia.setStyle(vigente
                    ? "-fx-text-fill: #2E7D32; -fx-font-weight: bold;"
                    : "-fx-text-fill: #C62828; -fx-font-weight: bold;");
        }
    }

    private boolean validarFormulario() {
        if (txtIdProveedor.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El ID del Proveedor es obligatorio."); return false;
        }
        if (txtIdProducto.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El ID del Producto es obligatorio."); return false;
        }
        if (taAcuerdo.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El campo Acuerdo es obligatorio."); return false;
        }
        if (dpFechaInicio.getValue() == null || dpFechaFin.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Las fechas son obligatorias."); return false;
        }
        if (dpFechaFin.getValue().isBefore(dpFechaInicio.getValue())) {
            JOptionPane.showMessageDialog(null, "La fecha fin no puede ser anterior a la fecha inicio."); return false;
        }
        try {
            Integer.parseInt(txtIdProveedor.getText().trim());
            Integer.parseInt(txtIdProducto.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Los IDs deben ser números."); return false;
        }
        return true;
    }
}