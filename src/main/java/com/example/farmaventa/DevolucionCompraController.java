package com.example.farmaventa;

import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.DevolucionCompra;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
    @FXML private TextField        txtIdDevolucion;
    @FXML private TextField        txtIdCompra;
    @FXML private TextField        txtProveedor;
    @FXML private TextField        txtMontoCompra;
    @FXML private DatePicker       dpFechaDevolucion;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private TextArea         txtRazon;

    // ── Tabla ─────────────────────────────────────────────────────────────────
    @FXML private TableView<DevolucionCompra>              tablaDevolucion;
    @FXML private TableColumn<DevolucionCompra, Integer>   colId;
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
    private final ObservableList<DevolucionCompra> listaDevoluciones = FXCollections.observableArrayList();
    private final ObservableList<String>           listaNotas        = FXCollections.observableArrayList();
    private FilteredList<DevolucionCompra>         listaFiltrada;

    private static final String PENDIENTE  = "PENDIENTE";
    private static final String COMPLETADA = "COMPLETADA";
    private static final String ANULADA    = "ANULADA";

    // ── Inicialización ────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        configurarCombos();
        configurarFiltros();
        listNotas.setItems(listaNotas);

        tablaDevolucion.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) { cargarEnFormulario(sel); cargarNotas(sel); }
        });

        dpFechaDevolucion.setValue(LocalDate.now());
        actualizarTabla();
    }

    // ── Configuraciones internas ──────────────────────────────────────────────
    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idDevolucioncompra"));
        colCompra.setCellValueFactory(new PropertyValueFactory<>("idCompra"));
        colProveedor.setCellValueFactory(new PropertyValueFactory<>("nombreProveedor"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaDevolucion"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoNombre"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("montoTotal"));

        // Colores por estado
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) { setText(null); setStyle(""); return; }
                setText(estado);
                switch (estado) {
                    case PENDIENTE  -> setStyle("-fx-text-fill: #F57F17; -fx-font-weight: bold;");
                    case COMPLETADA -> setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                    case ANULADA    -> setStyle("-fx-text-fill: #C62828; -fx-font-weight: bold;");
                    default         -> setStyle("");
                }
            }
        });

        listaFiltrada = new FilteredList<>(listaDevoluciones, p -> true);
        tablaDevolucion.setItems(listaFiltrada);
    }

    private void configurarCombos() {
        cmbEstado.setItems(FXCollections.observableArrayList(PENDIENTE, COMPLETADA, ANULADA));
        cmbEstado.setValue(PENDIENTE);

        cmbFiltroEstado.setItems(FXCollections.observableArrayList("Todos", PENDIENTE, COMPLETADA, ANULADA));
        cmbFiltroEstado.setValue("Todos");
    }

    private void configurarFiltros() {
        cmbFiltroEstado.valueProperty().addListener((o, v, n) -> aplicarFiltros());
        txtBusqueda.textProperty().addListener((o, v, n) -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        String estado = cmbFiltroEstado.getValue();
        String busq   = txtBusqueda.getText().toLowerCase();
        listaFiltrada.setPredicate(d -> {
            boolean okEstado = "Todos".equals(estado) || estado == null || d.getEstadoNombre().equals(estado);
            boolean okBusq   = busq.isEmpty()
                    || String.valueOf(d.getIdDevolucioncompra()).contains(busq)
                    || d.getNombreProveedor().toLowerCase().contains(busq)
                    || String.valueOf(d.getIdCompra()).contains(busq);
            return okEstado && okBusq;
        });
    }

    // ── Botón "🔎 Buscar Compra" ──────────────────────────────────────────────
    @FXML
    private void onBuscarCompra() {
        String idC = txtIdCompra.getText().trim();
        if (idC.isBlank()) { JOptionPane.showMessageDialog(null, "Ingresa un ID de compra."); return; }

        String sql = "SELECT pr.nombre AS nombre_proveedor, c.monto_total "
                + "FROM TBL_COMPRA c "
                + "JOIN TBL_PEDIDO_C pc ON pc.id_pedido_c = c.id_pedido_c "
                + "JOIN TBL_PROVEEDOR pr ON pr.id_proveedor = pc.id_proveedor "
                + "WHERE c.id_compra = ?";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(idC));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtProveedor.setText(rs.getString("nombre_proveedor"));
                txtMontoCompra.setText(String.format("%.2f", rs.getDouble("monto_total")));
            } else {
                JOptionPane.showMessageDialog(null, "No se encontró la compra #" + idC);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar: " + e.getMessage());
        }
    }

    // ── Botón "📋 Registrar" ──────────────────────────────────────────────────
    @FXML
    private void onRegistrarDevolucion() {
        if (txtIdCompra.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El ID de Compra es obligatorio."); return;
        }
        if (dpFechaDevolucion.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona la fecha."); return;
        }
        if (txtRazon.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "La razón es obligatoria."); return;
        }

        String sql = "INSERT INTO TBL_DEVOLUCION_COMPRA (fecha_devolucion, estado_devolucion, id_compra) "
                + "VALUES (?, 0, ?)";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDate(1, Date.valueOf(dpFechaDevolucion.getValue()));
            ps.setInt(2,  Integer.parseInt(txtIdCompra.getText().trim()));
            ps.executeUpdate();

            // Crear nota de débito automáticamente
            int idDev = -1;
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) idDev = keys.getInt(1);

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

    // ── Botón "✔ Completar" ───────────────────────────────────────────────────
    @FXML
    private void onCompletar() {
        DevolucionCompra sel = tablaDevolucion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una devolución."); return; }
        cambiarEstado(sel.getIdDevolucioncompra(), 1, COMPLETADA);
    }

    // ── Botón "✖ Anular" ──────────────────────────────────────────────────────
    @FXML
    private void onAnular() {
        DevolucionCompra sel = tablaDevolucion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una devolución."); return; }
        cambiarEstado(sel.getIdDevolucioncompra(), 0, ANULADA);
    }

    // ── Botón "🗑 Eliminar" ───────────────────────────────────────────────────
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
            // Borrar notas de débito primero
            PreparedStatement psNota = con.prepareStatement(
                    "DELETE FROM TBL_NOTA_DEBITO WHERE id_devolucioncompra = ?");
            psNota.setInt(1, idDev);
            psNota.executeUpdate();

            // Borrar la devolución
            PreparedStatement psDev = con.prepareStatement(
                    "DELETE FROM TBL_DEVOLUCION_COMPRA WHERE id_devolucioncompra = ?");
            psDev.setInt(1, idDev);
            psDev.executeUpdate();

            listaNotas.clear();
            lblNotaDe.setText("—");
            actualizarTabla();
            limpiar();
            JOptionPane.showMessageDialog(null, "Devolución eliminada correctamente.");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar: " + e.getMessage());
        }
    }

    // ── Botón "📄 Ver Notas de Débito" ───────────────────────────────────────
    @FXML
    private void onVerNotas() {
        DevolucionCompra sel = tablaDevolucion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una devolución."); return; }
        cargarNotas(sel);
    }

    // ── Botón "➕ Agregar Nota de Débito" ─────────────────────────────────────
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

    // ── Botón "✖ Limpiar" ────────────────────────────────────────────────────
    @FXML
    private void onLimpiarFormulario() { limpiar(); }

    @FXML
    public void limpiar() {
        txtIdDevolucion.clear();
        txtIdCompra.clear();
        txtProveedor.clear();
        txtMontoCompra.clear();
        dpFechaDevolucion.setValue(LocalDate.now());
        cmbEstado.setValue(PENDIENTE);
        txtRazon.clear();
        tablaDevolucion.getSelectionModel().clearSelection();
    }

    // ── Cargar tabla desde BD ─────────────────────────────────────────────────
    private void actualizarTabla() {
        String sql = "SELECT d.id_devolucioncompra, d.id_compra, "
                + "pr.nombre AS nombre_proveedor, "
                + "d.fecha_devolucion, d.estado_devolucion, "
                + "c.monto_total "
                + "FROM TBL_DEVOLUCION_COMPRA d "
                + "JOIN TBL_COMPRA    c  ON c.id_compra    = d.id_compra "
                + "JOIN TBL_PEDIDO_C  pc ON pc.id_pedido_c = c.id_pedido_c "
                + "JOIN TBL_PROVEEDOR pr ON pr.id_proveedor = pc.id_proveedor";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            listaDevoluciones.clear();
            while (rs.next()) {
                boolean bit        = rs.getBoolean("estado_devolucion");
                String  estadoText = bit ? COMPLETADA : PENDIENTE;
                listaDevoluciones.add(new DevolucionCompra(
                        rs.getInt("id_devolucioncompra"),
                        rs.getInt("id_compra"),
                        rs.getString("nombre_proveedor"),
                        rs.getDate("fecha_devolucion").toLocalDate(),
                        estadoText,
                        String.format("%.2f", rs.getDouble("monto_total"))
                ));
            }
            actualizarContadores();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar devoluciones: " + e.getMessage());
        }
    }

    // ── Cargar notas de débito ────────────────────────────────────────────────
    private void cargarNotas(DevolucionCompra d) {
        listaNotas.clear();
        lblNotaDe.setText("Dev. #" + d.getIdDevolucioncompra());

        String sql = "SELECT id_notadebito, razon FROM TBL_NOTA_DEBITO "
                + "WHERE id_devolucioncompra = ? ORDER BY id_notadebito";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, d.getIdDevolucioncompra());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listaNotas.add("#" + rs.getInt("id_notadebito") + "  —  " + rs.getString("razon"));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar notas: " + e.getMessage());
        }
    }

    // ── Cambiar estado BIT ────────────────────────────────────────────────────
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

    // ── Cargar formulario al seleccionar fila ─────────────────────────────────
    private void cargarEnFormulario(DevolucionCompra d) {
        txtIdDevolucion.setText(String.valueOf(d.getIdDevolucioncompra()));
        txtIdCompra.setText(String.valueOf(d.getIdCompra()));
        txtProveedor.setText(d.getNombreProveedor());
        txtMontoCompra.setText(d.getMontoTotal());
        dpFechaDevolucion.setValue(d.getFechaDevolucion());
        cmbEstado.setValue(d.getEstadoNombre());
    }

    // ── Pastillas de conteo ───────────────────────────────────────────────────
    private void actualizarContadores() {
        long pend = listaDevoluciones.stream().filter(d -> PENDIENTE.equals(d.getEstadoNombre())).count();
        long comp = listaDevoluciones.stream().filter(d -> COMPLETADA.equals(d.getEstadoNombre())).count();
        long anul = listaDevoluciones.stream().filter(d -> ANULADA.equals(d.getEstadoNombre())).count();
        lblContPendiente.setText("⏳  " + pend + "  Pendientes");
        lblContCompletada.setText("✔  " + comp + "  Completadas");
        lblContAnulada.setText("✖  "   + anul + "  Anuladas");
    }
}