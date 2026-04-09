package com.example.farmaventa;

import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.Envio;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javax.swing.JOptionPane;
import java.sql.*;

public class EnvioController {

    Conexion conexion = new Conexion();

    // ── Formulario ────────────────────────────────────────────────────────
    @FXML private TextField        txtIdVenta;
    @FXML private TextField        txtPersonaRecibe;
    @FXML private TextField        txtCostoServicio;
    @FXML private ComboBox<String> cmbMetodoEnvio;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private DatePicker       dpFechaEnvio;
    @FXML private Label            lblInfoVenta;

    // ── Tabla ─────────────────────────────────────────────────────────────
    @FXML private TextField                  txtBuscar;
    @FXML private TableView<Envio>           tablaEnvios;
    @FXML private TableColumn<Envio, Number> colId;
    @FXML private TableColumn<Envio, String> colInfoVenta;
    @FXML private TableColumn<Envio, String> colPersonaRecibe;
    @FXML private TableColumn<Envio, String> colMetodo;
    @FXML private TableColumn<Envio, Number> colCosto;
    @FXML private TableColumn<Envio, String> colFecha;
    @FXML private TableColumn<Envio, String> colEstado;

    // ── Lista de datos ────────────────────────────────────────────────────
    private ObservableList<Envio> listaEnvios = FXCollections.observableArrayList();
    private int idEnvioSeleccionado = -1;

    // ── Inicializar ───────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        dpFechaEnvio.setValue(java.time.LocalDate.now());
        cmbMetodoEnvio.getItems().addAll("Envío Estándar", "Envío Premium", "Delivery Express", "Recogida en tienda");
        cmbEstado.getItems().addAll("EN_PREPARACION", "EN_CAMINO", "ENTREGADO", "CANCELADO");
        cmbEstado.setValue("EN_PREPARACION");

        colId.setCellValueFactory(c -> c.getValue().idEnvioProperty());
        colInfoVenta.setCellValueFactory(c -> c.getValue().infoVentaProperty());
        colPersonaRecibe.setCellValueFactory(c -> c.getValue().personaRecibeProperty());
        colMetodo.setCellValueFactory(c -> c.getValue().metodoEnvioProperty());
        colCosto.setCellValueFactory(c -> c.getValue().costoServicioProperty());
        colFecha.setCellValueFactory(c -> c.getValue().fechaEnvioProperty());
        colEstado.setCellValueFactory(c -> c.getValue().estadoEnvioProperty());

        tablaEnvios.setItems(listaEnvios);

        // Clic en fila → carga datos en formulario
        tablaEnvios.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSel, newSel) -> {
                    if (newSel != null) cargarEnFormulario(newSel);
                });

        cargarEnvios();
    }

    // ── Buscar venta por ID ───────────────────────────────────────────────
    @FXML
    public void onBuscarVenta(ActionEvent event) {
        if (txtIdVenta.getText().isBlank()) return;

        String sql = "SELECT v.id_venta, " +
                "CONVERT(VARCHAR(10), v.fecha_transaccion, 120) AS fecha, " +
                "v.monto_total, " +
                "p.nombre + ' ' + p.apellido AS cliente " +
                "FROM TBL_VENTA v " +
                "JOIN TBL_CLIENTE cl ON cl.id_cliente = v.id_cliente " +
                "JOIN TBL_PERSONA p  ON p.id_persona  = cl.id_persona " +
                "WHERE v.id_venta = ?";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(txtIdVenta.getText().trim()));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                lblInfoVenta.setText("Venta #" + rs.getInt("id_venta")
                        + " - " + rs.getString("cliente")
                        + " | RD$ " + String.format("%.2f", rs.getDouble("monto_total")));
                lblInfoVenta.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 11px;");
            } else {
                lblInfoVenta.setText("Venta no encontrada.");
                lblInfoVenta.setStyle("-fx-text-fill: #C62828; -fx-font-size: 11px;");
            }

        } catch (NumberFormatException e) {
            lblInfoVenta.setText("ID inválido.");
            lblInfoVenta.setStyle("-fx-text-fill: #C62828; -fx-font-size: 11px;");
        } catch (SQLException e) {
            lblInfoVenta.setText("Error: " + e.getMessage());
        }
    }

    // ── Buscar en tabla ───────────────────────────────────────────────────
    @FXML
    public void fnBuscar(ActionEvent event) {
        String busqueda = txtBuscar.getText().trim().toLowerCase();
        if (busqueda.isEmpty()) {
            tablaEnvios.setItems(listaEnvios);
            return;
        }

        ObservableList<Envio> listaFiltrada = FXCollections.observableArrayList();
        for (Envio e : listaEnvios) {
            if (e.getPersonaRecibe().toLowerCase().contains(busqueda)
                    || e.getInfoVenta().toLowerCase().contains(busqueda)
                    || e.getMetodoEnvio().toLowerCase().contains(busqueda)
                    || e.getEstadoEnvio().toLowerCase().contains(busqueda)) {
                listaFiltrada.add(e);
            }
        }
        tablaEnvios.setItems(listaFiltrada);
    }

    // ── Cargar tabla ──────────────────────────────────────────────────────
    @FXML
    public void cargarEnvios() {
        listaEnvios.clear();
        idEnvioSeleccionado = -1;

        String sql = "SELECT e.id_envio, " +
                "CONVERT(VARCHAR(10), e.fecha_envio, 120) AS fecha_envio, " +
                "e.costo_servicio, e.persona_recibe, e.metodo_envio, e.estado, e.id_venta, " +
                "'Venta #' + CAST(v.id_venta AS VARCHAR) + ' - ' + p.nombre + ' ' + p.apellido AS info_venta " +
                "FROM TBL_ENVIO e " +
                "JOIN TBL_VENTA   v  ON v.id_venta   = e.id_venta " +
                "JOIN TBL_CLIENTE cl ON cl.id_cliente = v.id_cliente " +
                "JOIN TBL_PERSONA p  ON p.id_persona  = cl.id_persona " +
                "ORDER BY e.id_envio DESC";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                listaEnvios.add(new Envio(
                        rs.getInt("id_envio"),
                        rs.getString("fecha_envio"),
                        rs.getDouble("costo_servicio"),
                        rs.getString("persona_recibe"),
                        rs.getString("metodo_envio"),
                        rs.getInt("id_venta"),
                        rs.getString("info_venta"),
                        rs.getString("estado")
                ));
            }
            tablaEnvios.setItems(listaEnvios);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar envíos: " + e.getMessage());
        }
    }

    // ── Guardar nuevo ─────────────────────────────────────────────────────
    @FXML
    public void onGuardarEnvio(ActionEvent event) {
        if (!validarFormulario()) return;

        String sql = "INSERT INTO TBL_ENVIO (fecha_envio, id_venta, costo_servicio, persona_recibe, metodo_envio, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1,   Date.valueOf(dpFechaEnvio.getValue()));
            ps.setInt(2,    Integer.parseInt(txtIdVenta.getText().trim()));
            ps.setDouble(3, Double.parseDouble(txtCostoServicio.getText().trim()));
            ps.setString(4, txtPersonaRecibe.getText().trim());
            ps.setString(5, cmbMetodoEnvio.getValue());
            ps.setString(6, cmbEstado.getValue());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "Envío registrado correctamente.");
            limpiar();
            cargarEnvios();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al guardar envío: " + e.getMessage());
        }
    }

    // ── Editar envío seleccionado ─────────────────────────────────────────
    @FXML
    public void onEditarEnvio(ActionEvent event) {
        if (idEnvioSeleccionado == -1) {
            JOptionPane.showMessageDialog(null, "Selecciona un envío de la tabla primero.");
            return;
        }
        if (!validarFormulario()) return;

        String sql = "UPDATE TBL_ENVIO " +
                "SET fecha_envio = ?, costo_servicio = ?, persona_recibe = ?, metodo_envio = ?, estado = ? " +
                "WHERE id_envio = ?";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1,   Date.valueOf(dpFechaEnvio.getValue()));
            ps.setDouble(2, Double.parseDouble(txtCostoServicio.getText().trim()));
            ps.setString(3, txtPersonaRecibe.getText().trim());
            ps.setString(4, cmbMetodoEnvio.getValue());
            ps.setString(5, cmbEstado.getValue());
            ps.setInt(6,    idEnvioSeleccionado);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "Envío #" + idEnvioSeleccionado + " actualizado.");
            limpiar();
            cargarEnvios();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al editar envío: " + e.getMessage());
        }
    }

    // ── Eliminar ──────────────────────────────────────────────────────────
    @FXML
    public void onEliminarEnvio(ActionEvent event) {
        if (idEnvioSeleccionado == -1) {
            JOptionPane.showMessageDialog(null, "Selecciona un envío primero.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(null,
                "¿Eliminar el envío #" + idEnvioSeleccionado + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM TBL_ENVIO WHERE id_envio = ?";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idEnvioSeleccionado);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Envío eliminado.");
            limpiar();
            cargarEnvios();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar: " + e.getMessage());
        }
    }

    // ── Limpiar formulario ────────────────────────────────────────────────
    @FXML
    public void limpiar() {
        txtIdVenta.clear();
        txtPersonaRecibe.clear();
        txtCostoServicio.clear();
        txtBuscar.clear();
        cmbMetodoEnvio.setValue(null);
        cmbEstado.setValue("EN_PREPARACION");
        dpFechaEnvio.setValue(java.time.LocalDate.now());
        if (lblInfoVenta != null) lblInfoVenta.setText("");
        idEnvioSeleccionado = -1;
        tablaEnvios.getSelectionModel().clearSelection();
        tablaEnvios.setItems(listaEnvios);
    }

    // ── Cargar fila seleccionada en formulario ────────────────────────────
    private void cargarEnFormulario(Envio e) {
        idEnvioSeleccionado = e.getIdEnvio();
        txtIdVenta.setText(String.valueOf(e.getIdVenta()));
        txtPersonaRecibe.setText(e.getPersonaRecibe());
        txtCostoServicio.setText(String.valueOf(e.getCostoServicio()));
        cmbMetodoEnvio.setValue(e.getMetodoEnvio());
        cmbEstado.setValue(e.getEstadoEnvio());
        if (lblInfoVenta != null) {
            lblInfoVenta.setText(e.getInfoVenta());
            lblInfoVenta.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 11px;");
        }
        try {
            dpFechaEnvio.setValue(java.time.LocalDate.parse(e.getFechaEnvio().substring(0, 10)));
        } catch (Exception ignored) {}
    }

    // ── Validaciones ──────────────────────────────────────────────────────
    private boolean validarFormulario() {
        if (txtIdVenta.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El ID de Venta es obligatorio."); return false;
        }
        if (txtPersonaRecibe.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "La persona que recibe es obligatoria."); return false;
        }
        if (txtCostoServicio.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El costo de servicio es obligatorio."); return false;
        }
        if (cmbMetodoEnvio.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona el método de envío."); return false;
        }
        if (dpFechaEnvio.getValue() == null) {
            JOptionPane.showMessageDialog(null, "La fecha de envío es obligatoria."); return false;
        }
        try {
            Integer.parseInt(txtIdVenta.getText().trim());
            Double.parseDouble(txtCostoServicio.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID Venta debe ser número y Costo debe ser decimal.");
            return false;
        }
        return true;
    }
}