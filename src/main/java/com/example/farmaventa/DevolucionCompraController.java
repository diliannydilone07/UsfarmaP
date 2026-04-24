package com.example.farmaventa;

import Usuarios.Permisos;
import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.DevolucionCompra;
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

public class DevolucionCompraController implements Initializable {

    Conexion conexion = new Conexion();

    // ── Formulario ────────────────────────────────────────────────────────────
    // ── Botones con restricción de permisos ───────────────────────────────
    @FXML private Button btnRegistrarDevolucion;
    @FXML private Button btnCompletar;
    @FXML private Button btnAnular;
    @FXML private Button btnEliminar;

    @FXML private TextField        txtIdDevolucion;
    @FXML private TextField        txtIdReclamacion;
    @FXML private TextField        txtIdCompra;
    @FXML private TextField        txtProveedor;
    @FXML private TextField        txtMontoCompra;
    @FXML private DatePicker       dpFechaDevolucion;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private TextField txtRazon;

    // ── Tabla ─────────────────────────────────────────────────────────────────
    @FXML private TableView<DevolucionCompra>              tablaDevolucion;
    @FXML private TableColumn<DevolucionCompra, Integer>   colId;
    @FXML private TableColumn<DevolucionCompra, Integer>   colReclamacion;
    @FXML private TableColumn<DevolucionCompra, Integer>   colCompra;
    @FXML private TableColumn<DevolucionCompra, String>    colProveedor;
    @FXML private TableColumn<DevolucionCompra, LocalDate> colFecha;
    @FXML private TableColumn<DevolucionCompra, String>    colEstado;
    @FXML private TableColumn<DevolucionCompra, String>    colMonto;

    // ── Filtros ───────────────────────────────────────────────────────────────
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TextField        txtBusqueda;

    // ── Panel Notas de Débito ─────────────────────────────────────────────────
    @FXML private Label            lblNotaDe;
    @FXML private TextArea         txtRazonNota;
    @FXML private ListView<String> listNotas;

    // ── Pastillas ─────────────────────────────────────────────────────────────
    @FXML private Label lblContPendiente;
    @FXML private Label lblContCompletada;
    @FXML private Label lblContAnulada;

    // ── Listas ────────────────────────────────────────────────────────────────
    private ObservableList<DevolucionCompra> listaDevoluciones = FXCollections.observableArrayList();
    private ObservableList<String>           listaNotas        = FXCollections.observableArrayList();

    private static final String PENDIENTE  = "PENDIENTE";
    private static final String COMPLETADA = "COMPLETADA";
    private static final String ANULADA    = "ANULADA";

    // ── Inicializar ───────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idDevolucioncompra"));
        colReclamacion.setCellValueFactory(new PropertyValueFactory<>("idReclamacioncompra"));
        colCompra.setCellValueFactory(new PropertyValueFactory<>("idCompra"));
        colProveedor.setCellValueFactory(new PropertyValueFactory<>("nombreProveedor"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaDevolucion"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoNombre"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("montoTotal"));

        tablaDevolucion.setItems(listaDevoluciones);

        cmbEstado.getItems().addAll(PENDIENTE, COMPLETADA, ANULADA);
        cmbEstado.setValue(PENDIENTE);

        cmbFiltroEstado.getItems().addAll("Todos", PENDIENTE, COMPLETADA, ANULADA);
        cmbFiltroEstado.setValue("Todos");

        listNotas.setItems(listaNotas);

        tablaDevolucion.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) { cargarEnFormulario(sel); cargarNotas(sel); }
        });

        dpFechaDevolucion.setValue(LocalDate.now());
        actualizarTabla();

        // ── Permisos ──────────────────────────────────────────────────────
        Permisos.aplicarBtn(btnRegistrarDevolucion, Permisos.Accion.REGISTRAR);
        Permisos.aplicarBtn(btnCompletar,           Permisos.Accion.EDITAR);
        Permisos.aplicarBtn(btnAnular,              Permisos.Accion.ELIMINAR);
        Permisos.aplicarBtn(btnEliminar,            Permisos.Accion.ELIMINAR);

    }

    // ── Buscar por ID de Reclamacion → trae id_compra, proveedor y monto ──────
    @FXML
    public void onBuscarReclamacion() {
        String idR = txtIdReclamacion.getText().trim();
        if (idR.isBlank()) { JOptionPane.showMessageDialog(null, "Ingresa un ID de reclamacion."); return; }

        String sql = "SELECT r.id_compra, pr.nombre AS nombre_proveedor, c.monto_total " +
                "FROM TBL_RECLAMACION_COMPRA r " +
                "JOIN TBL_COMPRA    c  ON c.id_compra    = r.id_compra " +
                "JOIN TBL_PEDIDO_C  pc ON pc.id_pedido_c = c.id_pedido_c " +
                "JOIN TBL_PROVEEDOR pr ON pr.id_proveedor = pc.id_proveedor " +
                "WHERE r.id_reclamacioncompra = ?";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(idR));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtIdCompra.setText(String.valueOf(rs.getInt("id_compra")));
                txtProveedor.setText(rs.getString("nombre_proveedor"));
                txtMontoCompra.setText(String.format("%.2f", rs.getDouble("monto_total")));
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

        ObservableList<DevolucionCompra> listaFiltrada = FXCollections.observableArrayList();
        for (DevolucionCompra d : listaDevoluciones) {
            boolean okEstado = "Todos".equals(estado) || estado == null || d.getEstadoNombre().equals(estado);
            boolean okBusq   = busqueda.isEmpty()
                    || String.valueOf(d.getIdDevolucioncompra()).contains(busqueda)
                    || d.getNombreProveedor().toLowerCase().contains(busqueda)
                    || String.valueOf(d.getIdCompra()).contains(busqueda);
            if (okEstado && okBusq) listaFiltrada.add(d);
        }
        tablaDevolucion.setItems(listaFiltrada);
    }

    // ── Registrar devolución ──────────────────────────────────────────────────
    @FXML
    private void onRegistrarDevolucion() {
        if (txtIdReclamacion.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El ID de Reclamacion es obligatorio."); return;
        }
        if (txtIdCompra.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Busca la reclamacion primero para obtener la compra."); return;
        }
        if (dpFechaDevolucion.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona la fecha."); return;
        }
        if (txtRazon.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "La razón es obligatoria."); return;
        }

        String sql = "INSERT INTO TBL_DEVOLUCION_COMPRA (fecha_devolucion, estado_devolucion, id_compra, id_reclamacioncompra) " +
                "VALUES (?, 0, ?, ?)";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDate(1, Date.valueOf(dpFechaDevolucion.getValue()));
            ps.setInt(2,  Integer.parseInt(txtIdCompra.getText().trim()));
            ps.setInt(3,  Integer.parseInt(txtIdReclamacion.getText().trim()));
            ps.executeUpdate();

            int idDev = -1;
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) idDev = keys.getInt(1);

            // Nota de débito automática
            if (idDev != -1) {
                PreparedStatement psNota = con.prepareStatement(
                        "INSERT INTO TBL_NOTA_DEBITO (razon, id_devolucioncompra) VALUES (?, ?)");
                psNota.setString(1, "Nota de débito generada automáticamente. Razón: "
                        + txtRazon.getText().trim()
                        + (!txtMontoCompra.getText().isBlank() ? " | Monto compra: " + txtMontoCompra.getText() : ""));
                psNota.setInt(2, idDev);
                psNota.executeUpdate();
            }

            JOptionPane.showMessageDialog(null, "Devolución de compra registrada correctamente.");
            actualizarTabla();
            limpiar();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar: " + e.getMessage());
        }
    }

    // ── Completar ─────────────────────────────────────────────────────────────
    @FXML
    private void onCompletar() {
        DevolucionCompra sel = tablaDevolucion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una devolución."); return; }
        cambiarEstado(sel.getIdDevolucioncompra(), 1, COMPLETADA);
    }

    // ── Anular ────────────────────────────────────────────────────────────────
    @FXML
    private void onAnular() {
        DevolucionCompra sel = tablaDevolucion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una devolución."); return; }
        cambiarEstado(sel.getIdDevolucioncompra(), 0, ANULADA);
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────
    @FXML
    private void onEliminar() {
        DevolucionCompra sel = tablaDevolucion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una devolución."); return; }

        int confirm = JOptionPane.showConfirmDialog(null,
                "¿Eliminar devolución #" + sel.getIdDevolucioncompra() + "?\nTambién se eliminarán sus notas de débito.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int idDev = sel.getIdDevolucioncompra();

        try (Connection con = conexion.establecerConexion()) {
            PreparedStatement ps1 = con.prepareStatement(
                    "DELETE FROM TBL_NOTA_DEBITO WHERE id_devolucioncompra = ?");
            ps1.setInt(1, idDev);
            ps1.executeUpdate();

            PreparedStatement ps2 = con.prepareStatement(
                    "DELETE FROM TBL_DEVOLUCION_COMPRA WHERE id_devolucioncompra = ?");
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

    // ── Ver notas de débito ───────────────────────────────────────────────────
    @FXML
    private void onVerNotas() {
        DevolucionCompra sel = tablaDevolucion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una devolución."); return; }
        cargarNotas(sel);
    }

    // ── Agregar nota de débito ────────────────────────────────────────────────
    @FXML
    private void onAgregarNota() {
        DevolucionCompra sel = tablaDevolucion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una devolución."); return; }

        String razonNota = txtRazonNota.getText().trim();
        if (razonNota.isBlank()) { JOptionPane.showMessageDialog(null, "Escribe la razón de la nota primero."); return; }

        String sql = "INSERT INTO TBL_NOTA_DEBITO (razon, id_devolucioncompra) VALUES (?, ?)";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, razonNota);
            ps.setInt(2,    sel.getIdDevolucioncompra());
            ps.executeUpdate();
            txtRazonNota.clear();
            cargarNotas(sel);
            JOptionPane.showMessageDialog(null, "Nota de débito agregada.");
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
        txtIdCompra.clear();
        txtProveedor.clear();
        txtMontoCompra.clear();
        dpFechaDevolucion.setValue(LocalDate.now());
        cmbEstado.setValue(PENDIENTE);
        txtRazon.clear();
        tablaDevolucion.getSelectionModel().clearSelection();
        tablaDevolucion.setItems(listaDevoluciones);
    }

    // ── Cargar tabla desde BD ─────────────────────────────────────────────────
    private void actualizarTabla() {
        String sql = "SELECT d.id_devolucioncompra, d.id_compra, d.id_reclamacioncompra, " +
                "pr.nombre AS nombre_proveedor, " +
                "d.fecha_devolucion, d.estado_devolucion, c.monto_total " +
                "FROM TBL_DEVOLUCION_COMPRA d " +
                "JOIN TBL_COMPRA    c  ON c.id_compra    = d.id_compra " +
                "JOIN TBL_PEDIDO_C  pc ON pc.id_pedido_c = c.id_pedido_c " +
                "JOIN TBL_PROVEEDOR pr ON pr.id_proveedor = pc.id_proveedor";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            listaDevoluciones.clear();
            while (rs.next()) {
                boolean bit        = rs.getBoolean("estado_devolucion");
                String  estadoText = bit ? COMPLETADA : PENDIENTE;
                DevolucionCompra dc = new DevolucionCompra(
                        rs.getInt("id_devolucioncompra"),
                        rs.getInt("id_compra"),
                        rs.getString("nombre_proveedor"),
                        rs.getDate("fecha_devolucion").toLocalDate(),
                        estadoText,
                        String.format("%.2f", rs.getDouble("monto_total"))
                );
                dc.setIdReclamacioncompra(rs.getInt("id_reclamacioncompra"));
                listaDevoluciones.add(dc);
            }
            tablaDevolucion.setItems(listaDevoluciones);
            actualizarContadores();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar devoluciones: " + e.getMessage());
        }
    }

    // ── Cargar notas de débito ────────────────────────────────────────────────
    private void cargarNotas(DevolucionCompra d) {
        listaNotas.clear();
        if (lblNotaDe != null) lblNotaDe.setText("Dev. #" + d.getIdDevolucioncompra());

        String sql = "SELECT id_notadebito, razon FROM TBL_NOTA_DEBITO " +
                "WHERE id_devolucioncompra = ? ORDER BY id_notadebito";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, d.getIdDevolucioncompra());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listaNotas.add("#" + rs.getInt("id_notadebito") + "  —  " + rs.getString("razon"));
            }
            listNotas.setItems(listaNotas);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar notas: " + e.getMessage());
        }
    }

    // ── Cambiar estado ────────────────────────────────────────────────────────
    private void cambiarEstado(int idDev, int bit, String estadoUI) {
        String sql = "UPDATE TBL_DEVOLUCION_COMPRA SET estado_devolucion = ? WHERE id_devolucioncompra = ?";
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
    private void cargarEnFormulario(DevolucionCompra d) {
        txtIdDevolucion.setText(String.valueOf(d.getIdDevolucioncompra()));
        if (txtIdReclamacion != null && d.getIdReclamacioncompra() > 0)
            txtIdReclamacion.setText(String.valueOf(d.getIdReclamacioncompra()));
        txtIdCompra.setText(String.valueOf(d.getIdCompra()));
        txtProveedor.setText(d.getNombreProveedor());
        txtMontoCompra.setText(d.getMontoTotal());
        dpFechaDevolucion.setValue(d.getFechaDevolucion());
        cmbEstado.setValue(d.getEstadoNombre());
    }

    // ── Pastillas de conteo ───────────────────────────────────────────────────
    private void actualizarContadores() {
        int pend = 0, comp = 0, anul = 0;
        for (DevolucionCompra d : listaDevoluciones) {
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