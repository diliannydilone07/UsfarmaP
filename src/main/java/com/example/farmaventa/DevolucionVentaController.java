package com.example.farmaventa;

import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.DevolucionVenta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javax.swing.JOptionPane;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class DevolucionVentaController implements Initializable {

    Conexion conexion = new Conexion();

    // ── Formulario ────────────────────────────────────────────────────────────
    @FXML private TextField        txtIdDevolucion;
    @FXML private TextField        txtIdReclamacion;
    @FXML private TextField        txtIdVenta;
    @FXML private TextField        txtCliente;
    @FXML private TextField        txtMontoVenta;
    @FXML private DatePicker       dpFechaDevolucion;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private TextArea         txtRazon;

    // ── Tabla devoluciones ────────────────────────────────────────────────────
    @FXML private TableView<DevolucionVenta>              tablaDevolucion;
    @FXML private TableColumn<DevolucionVenta, Integer>   colId;
    @FXML private TableColumn<DevolucionVenta, Integer>   colReclamacion;
    @FXML private TableColumn<DevolucionVenta, Integer>   colVenta;
    @FXML private TableColumn<DevolucionVenta, String>    colCliente;
    @FXML private TableColumn<DevolucionVenta, LocalDate> colFecha;
    @FXML private TableColumn<DevolucionVenta, String>    colEstado;
    @FXML private TableColumn<DevolucionVenta, String>    colMonto;
    @FXML private TableColumn<DevolucionVenta, String>    colRazon;

    // ── Filtros ───────────────────────────────────────────────────────────────
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TextField        txtBusqueda;

    // ── Panel Nota de Crédito ─────────────────────────────────────────────────
    @FXML private Label            lblNotaDe;
    @FXML private TextArea         txtRazonNota;
    @FXML private ListView<String> listNotas;

    // ── Pastillas de conteo ───────────────────────────────────────────────────
    @FXML private Label lblContPendiente;
    @FXML private Label lblContCompletada;
    @FXML private Label lblContAnulada;

    // ── Listas ────────────────────────────────────────────────────────────────
    private ObservableList<DevolucionVenta> listaDevoluciones = FXCollections.observableArrayList();
    private ObservableList<String>          listaNotas        = FXCollections.observableArrayList();

    private static final String PENDIENTE  = "PENDIENTE";
    private static final String COMPLETADA = "COMPLETADA";
    private static final String ANULADA    = "ANULADA";

    // ── Inicializar ───────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idDevolucionventa"));
        colReclamacion.setCellValueFactory(new PropertyValueFactory<>("idReclamacionventa"));
        colVenta.setCellValueFactory(new PropertyValueFactory<>("idVenta"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaDevolucion"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoNombre"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("montoTotal"));
        colRazon.setCellValueFactory(new PropertyValueFactory<>("razon"));

        tablaDevolucion.setItems(listaDevoluciones);

        cmbEstado.getItems().addAll(PENDIENTE, COMPLETADA, ANULADA);
        cmbEstado.setValue(PENDIENTE);

        cmbFiltroEstado.getItems().addAll("Todos", PENDIENTE, COMPLETADA, ANULADA);
        cmbFiltroEstado.setValue("Todos");

        listNotas.setItems(listaNotas);

        // Clic en fila → cargar formulario y notas
        tablaDevolucion.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) { cargarEnFormulario(sel); cargarNotas(sel); }
        });

        dpFechaDevolucion.setValue(LocalDate.now());
        actualizarTabla();
    }

    // ── Buscar por ID de Reclamacion → trae id_venta, cliente y monto ─────────
    @FXML
    public void onBuscarReclamacion() {
        String idR = txtIdReclamacion.getText().trim();
        if (idR.isBlank()) { JOptionPane.showMessageDialog(null, "Ingresa un ID de reclamacion."); return; }

        String sql = "SELECT r.id_venta, p.nombre + ' ' + p.apellido AS nombre_cliente, v.monto_total " +
                "FROM TBL_RECLAMACION_VENTA r " +
                "JOIN TBL_VENTA v   ON v.id_venta   = r.id_venta " +
                "JOIN TBL_CLIENTE c ON c.id_cliente = v.id_cliente " +
                "JOIN TBL_PERSONA p ON p.id_persona = c.id_persona " +
                "WHERE r.id_reclamacionventa = ?";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(idR));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtIdVenta.setText(String.valueOf(rs.getInt("id_venta")));
                txtCliente.setText(rs.getString("nombre_cliente"));
                txtMontoVenta.setText(String.format("%.2f", rs.getDouble("monto_total")));
            } else {
                JOptionPane.showMessageDialog(null, "No se encontro la reclamacion #" + idR);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar: " + e.getMessage());
        }
    }

    // ── Buscar en tabla ───────────────────────────────────────────────────────
    @FXML
    private void fnBuscar() {
        String busqueda = txtBusqueda.getText().trim().toLowerCase();
        String estado   = cmbFiltroEstado.getValue();

        ObservableList<DevolucionVenta> listaFiltrada = FXCollections.observableArrayList();
        for (DevolucionVenta d : listaDevoluciones) {
            boolean okEstado = "Todos".equals(estado) || estado == null || d.getEstadoNombre().equals(estado);
            boolean okBusq   = busqueda.isEmpty()
                    || String.valueOf(d.getIdDevolucionventa()).contains(busqueda)
                    || d.getNombreCliente().toLowerCase().contains(busqueda)
                    || String.valueOf(d.getIdVenta()).contains(busqueda);
            if (okEstado && okBusq) listaFiltrada.add(d);
        }
        tablaDevolucion.setItems(listaFiltrada);
    }

    // ── Registrar nueva devolución ────────────────────────────────────────────
    @FXML
    private void onRegistrarDevolucion() {
        if (txtIdReclamacion.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El ID de Reclamacion es obligatorio."); return;
        }
        if (txtIdVenta.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Busca la reclamacion primero para obtener la venta."); return;
        }
        if (dpFechaDevolucion.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona la fecha."); return;
        }
        if (txtRazon.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "La razón es obligatoria."); return;
        }

        String sql = "INSERT INTO TBL_DEVOLUCION_VENTA (razon, fecha_devolucion, estado_devolucion, id_venta, id_reclamacionventa) " +
                "VALUES (?, ?, 0, ?, ?)";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, txtRazon.getText().trim());
            ps.setDate(2,   Date.valueOf(dpFechaDevolucion.getValue()));
            ps.setInt(3,    Integer.parseInt(txtIdVenta.getText().trim()));
            ps.setInt(4,    Integer.parseInt(txtIdReclamacion.getText().trim()));
            ps.executeUpdate();

            int idDev = -1;
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) idDev = keys.getInt(1);

            // Nota de crédito automática si hay monto
            if (idDev != -1 && !txtMontoVenta.getText().isBlank()) {
                PreparedStatement psNota = con.prepareStatement(
                        "INSERT INTO TBL_NOTA_CREDITO (razon, id_devolucionventa) VALUES (?, ?)");
                psNota.setString(1, "Nota de crédito generada automáticamente. Monto venta: " + txtMontoVenta.getText());
                psNota.setInt(2, idDev);
                psNota.executeUpdate();
            }

            JOptionPane.showMessageDialog(null, "Devolución registrada correctamente.");
            actualizarTabla();
            limpiar();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar: " + e.getMessage());
        }
    }

    // ── Completar ─────────────────────────────────────────────────────────────
    @FXML
    private void onCompletar() {
        DevolucionVenta sel = tablaDevolucion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una devolución."); return; }
        cambiarEstado(sel.getIdDevolucionventa(), 1, COMPLETADA);
    }

    // ── Anular ────────────────────────────────────────────────────────────────
    @FXML
    private void onAnular() {
        DevolucionVenta sel = tablaDevolucion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una devolución."); return; }
        cambiarEstado(sel.getIdDevolucionventa(), 0, ANULADA);
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────
    @FXML
    private void onEliminar() {
        DevolucionVenta sel = tablaDevolucion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una devolución."); return; }

        int confirm = JOptionPane.showConfirmDialog(null,
                "¿Eliminar devolución #" + sel.getIdDevolucionventa() + "?\nTambién se eliminarán sus notas de crédito.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int idDev = sel.getIdDevolucionventa();

        try (Connection con = conexion.establecerConexion()) {
            PreparedStatement ps1 = con.prepareStatement(
                    "DELETE FROM TBL_NOTA_CREDITO WHERE id_devolucionventa = ?");
            ps1.setInt(1, idDev);
            ps1.executeUpdate();

            PreparedStatement ps2 = con.prepareStatement(
                    "DELETE FROM TBL_DEVOLUCION_VENTA WHERE id_devolucionventa = ?");
            ps2.setInt(1, idDev);
            ps2.executeUpdate();

            listaNotas.clear();
            if (lblNotaDe != null) lblNotaDe.setText("—");
            actualizarTabla();
            limpiar();
            JOptionPane.showMessageDialog(null, "Devolución eliminada correctamente.");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar: " + e.getMessage());
        }
    }

    // ── Ver notas de crédito ──────────────────────────────────────────────────
    @FXML
    private void onVerNotas() {
        DevolucionVenta sel = tablaDevolucion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una devolución."); return; }
        cargarNotas(sel);
    }

    // ── Agregar nota de crédito ───────────────────────────────────────────────
    @FXML
    private void onAgregarNota() {
        DevolucionVenta sel = tablaDevolucion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una devolución."); return; }

        String razonNota = txtRazonNota.getText().trim();
        if (razonNota.isBlank()) { JOptionPane.showMessageDialog(null, "Escribe la razón de la nota primero."); return; }

        String sql = "INSERT INTO TBL_NOTA_CREDITO (razon, id_devolucionventa) VALUES (?, ?)";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, razonNota);
            ps.setInt(2,    sel.getIdDevolucionventa());
            ps.executeUpdate();
            txtRazonNota.clear();
            cargarNotas(sel);
            JOptionPane.showMessageDialog(null, "Nota de crédito agregada.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al agregar nota: " + e.getMessage());
        }
    }

    // ── Limpiar ───────────────────────────────────────────────────────────────
    @FXML
    private void onLimpiarFormulario() { limpiar(); }

    @FXML
    public void limpiar() {
        txtIdDevolucion.clear();
        txtIdReclamacion.clear();
        txtIdVenta.clear();
        txtCliente.clear();
        txtMontoVenta.clear();
        dpFechaDevolucion.setValue(LocalDate.now());
        cmbEstado.setValue(PENDIENTE);
        txtRazon.clear();
        tablaDevolucion.getSelectionModel().clearSelection();
        tablaDevolucion.setItems(listaDevoluciones);
    }

    // ── Cargar tabla desde BD ─────────────────────────────────────────────────
    private void actualizarTabla() {
        String sql = "SELECT d.id_devolucionventa, d.id_venta, d.id_reclamacionventa, " +
                "p.nombre + ' ' + p.apellido AS nombre_cliente, " +
                "d.fecha_devolucion, d.razon, d.estado_devolucion, v.monto_total " +
                "FROM TBL_DEVOLUCION_VENTA d " +
                "JOIN TBL_VENTA   v ON v.id_venta   = d.id_venta " +
                "JOIN TBL_CLIENTE c ON c.id_cliente = v.id_cliente " +
                "JOIN TBL_PERSONA p ON p.id_persona = c.id_persona";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            listaDevoluciones.clear();
            while (rs.next()) {
                boolean bit        = rs.getBoolean("estado_devolucion");
                String  estadoText = bit ? COMPLETADA : PENDIENTE;
                DevolucionVenta dv = new DevolucionVenta(
                        rs.getInt("id_devolucionventa"),
                        rs.getInt("id_venta"),
                        rs.getString("nombre_cliente"),
                        rs.getDate("fecha_devolucion").toLocalDate(),
                        rs.getString("razon"),
                        estadoText,
                        String.format("%.2f", rs.getDouble("monto_total"))
                );
                dv.setIdReclamacionventa(rs.getInt("id_reclamacionventa"));
                listaDevoluciones.add(dv);
            }
            tablaDevolucion.setItems(listaDevoluciones);
            actualizarContadores();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar devoluciones: " + e.getMessage());
        }
    }

    // ── Cargar notas de crédito ───────────────────────────────────────────────
    private void cargarNotas(DevolucionVenta d) {
        listaNotas.clear();
        if (lblNotaDe != null) lblNotaDe.setText("Dev. #" + d.getIdDevolucionventa());

        String sql = "SELECT id_notacredito, razon FROM TBL_NOTA_CREDITO " +
                "WHERE id_devolucionventa = ? ORDER BY id_notacredito";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, d.getIdDevolucionventa());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listaNotas.add("#" + rs.getInt("id_notacredito") + "  —  " + rs.getString("razon"));
            }
            listNotas.setItems(listaNotas);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar notas: " + e.getMessage());
        }
    }

    // ── Cambiar estado ────────────────────────────────────────────────────────
    private void cambiarEstado(int idDev, int bit, String estadoUI) {
        String sql = "UPDATE TBL_DEVOLUCION_VENTA SET estado_devolucion = ? WHERE id_devolucionventa = ?";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bit);
            ps.setInt(2, idDev);
            ps.executeUpdate();
            actualizarTabla();
            JOptionPane.showMessageDialog(null, "Estado actualizado a: " + estadoUI);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cambiar estado: " + e.getMessage());
        }
    }

    // ── Cargar fila en formulario ─────────────────────────────────────────────
    private void cargarEnFormulario(DevolucionVenta d) {
        txtIdDevolucion.setText(String.valueOf(d.getIdDevolucionventa()));
        if (txtIdReclamacion != null && d.getIdReclamacionventa() > 0)
            txtIdReclamacion.setText(String.valueOf(d.getIdReclamacionventa()));
        txtIdVenta.setText(String.valueOf(d.getIdVenta()));
        txtCliente.setText(d.getNombreCliente());
        txtMontoVenta.setText(d.getMontoTotal());
        dpFechaDevolucion.setValue(d.getFechaDevolucion());
        cmbEstado.setValue(d.getEstadoNombre());
        txtRazon.setText(d.getRazon());
    }

    // ── Pastillas de conteo ───────────────────────────────────────────────────
    private void actualizarContadores() {
        int pend = 0, comp = 0, anul = 0;
        for (DevolucionVenta d : listaDevoluciones) {
            switch (d.getEstadoNombre()) {
                case PENDIENTE  -> pend++;
                case COMPLETADA -> comp++;
                case ANULADA    -> anul++;
            }
        }
        if (lblContPendiente  != null) lblContPendiente.setText("⏳  " + pend + "  Pendientes");
        if (lblContCompletada != null) lblContCompletada.setText("✔  " + comp + "  Completadas");
        if (lblContAnulada    != null) lblContAnulada.setText("✖  "    + anul + "  Anuladas");
    }
}