package com.example.farmaventa;

import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.HistoricoReclamacionCompra;
import com.example.farmaventa.modelo.ReclamacionCompra;
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

public class ReclamacionCompraController implements Initializable {

    Conexion conexion = new Conexion();

    // ── Formulario ────────────────────────────────────────────────────────────
    @FXML private TextField        txtIdReclamacion;
    @FXML private TextField        txtIdCompra;
    @FXML private TextField        txtProveedor;
    @FXML private DatePicker       dpFechaReclamacion;
    @FXML private ComboBox<String> cmbEstadoActual;
    @FXML private TextField        txtCantidadDevolver;
    @FXML private TextField        txtDescripcion;

    // ── Tabla reclamaciones ───────────────────────────────────────────────────
    @FXML private TableView<ReclamacionCompra>              tablaReclamaciones;
    @FXML private TableColumn<ReclamacionCompra, Integer>   colId;
    @FXML private TableColumn<ReclamacionCompra, Integer>   colCompra;
    @FXML private TableColumn<ReclamacionCompra, String>    colProveedor;
    @FXML private TableColumn<ReclamacionCompra, LocalDate> colFecha;
    @FXML private TableColumn<ReclamacionCompra, String>    colEstado;
    @FXML private TableColumn<ReclamacionCompra, Integer>   colCantidad;
    @FXML private TableColumn<ReclamacionCompra, String>    colDescripcion;

    // ── Filtros ───────────────────────────────────────────────────────────────
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TextField        txtBusqueda;

    // ── Historial ─────────────────────────────────────────────────────────────
    @FXML private TableView<HistoricoReclamacionCompra>              tablaHistorico;
    @FXML private TableColumn<HistoricoReclamacionCompra, Integer>   colHistId;
    @FXML private TableColumn<HistoricoReclamacionCompra, LocalDate> colHistFecha;
    @FXML private TableColumn<HistoricoReclamacionCompra, String>    colHistDescripcion;
    @FXML private TextArea txtDetalleHistorial;
    @FXML private TextArea txtNuevaNotaHistorial;
    @FXML private Label    lblHistorialDe;

    // ── Pastillas ─────────────────────────────────────────────────────────────
    @FXML private Label lblContPendiente;
    @FXML private Label lblContRevision;
    @FXML private Label lblContAprobada;
    @FXML private Label lblContRechazada;

    // ── Listas ────────────────────────────────────────────────────────────────
    private final ObservableList<ReclamacionCompra>        listaReclamaciones = FXCollections.observableArrayList();
    private final ObservableList<HistoricoReclamacionCompra> listaHistorico   = FXCollections.observableArrayList();
    private FilteredList<ReclamacionCompra> listaFiltrada;

    private static final String PENDIENTE   = "PENDIENTE";
    private static final String EN_REVISION = "EN_REVISION";
    private static final String APROBADA    = "APROBADA";
    private static final String RECHAZADA   = "RECHAZADA";

    // ── Inicialización ────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        configurarCombos();
        configurarFiltros();
        configurarHistorico();

        tablaReclamaciones.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) { cargarEnFormulario(sel); cargarHistorial(sel); }
        });

        dpFechaReclamacion.setValue(LocalDate.now());
        actualizarTabla();
    }

    // ── Configuraciones internas ──────────────────────────────────────────────
    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idReclamacioncompra"));
        colCompra.setCellValueFactory(new PropertyValueFactory<>("idCompra"));
        colProveedor.setCellValueFactory(new PropertyValueFactory<>("nombreProveedor"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaReclamacion"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoActualNombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidadAdevolver"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        // Colores por estado
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) { setText(null); setStyle(""); return; }
                setText(estado.replace("_", " "));
                switch (estado) {
                    case PENDIENTE   -> setStyle("-fx-text-fill: #F57F17; -fx-font-weight: bold;");
                    case EN_REVISION -> setStyle("-fx-text-fill: #1565C0; -fx-font-weight: bold;");
                    case APROBADA    -> setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                    case RECHAZADA   -> setStyle("-fx-text-fill: #C62828; -fx-font-weight: bold;");
                    default          -> setStyle("");
                }
            }
        });

        listaFiltrada = new FilteredList<>(listaReclamaciones, p -> true);
        tablaReclamaciones.setItems(listaFiltrada);
    }

    private void configurarCombos() {
        ObservableList<String> estados = FXCollections.observableArrayList(PENDIENTE, EN_REVISION, APROBADA, RECHAZADA);
        cmbEstadoActual.setItems(estados);
        cmbEstadoActual.setValue(PENDIENTE);

        ObservableList<String> filtros = FXCollections.observableArrayList("Todos");
        filtros.addAll(estados);
        cmbFiltroEstado.setItems(filtros);
        cmbFiltroEstado.setValue("Todos");
    }

    private void configurarFiltros() {
        cmbFiltroEstado.valueProperty().addListener((o, v, n) -> aplicarFiltros());
        txtBusqueda.textProperty().addListener((o, v, n) -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        String estado = cmbFiltroEstado.getValue();
        String busq   = txtBusqueda.getText().toLowerCase();
        listaFiltrada.setPredicate(r -> {
            boolean okEstado = "Todos".equals(estado) || estado == null || r.getEstadoActualNombre().equals(estado);
            boolean okBusq   = busq.isEmpty()
                    || String.valueOf(r.getIdReclamacioncompra()).contains(busq)
                    || r.getNombreProveedor().toLowerCase().contains(busq)
                    || String.valueOf(r.getIdCompra()).contains(busq);
            return okEstado && okBusq;
        });
    }

    private void configurarHistorico() {
        colHistId.setCellValueFactory(new PropertyValueFactory<>("idHistorico"));
        colHistFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));
        colHistDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        tablaHistorico.setItems(listaHistorico);

        tablaHistorico.getSelectionModel().selectedItemProperty().addListener((o, v, h) -> {
            if (h != null) txtDetalleHistorial.setText(h.obtenerDetalleCambio());
        });
    }

    // ── Botón "🔎 Buscar Compra" ──────────────────────────────────────────────
    @FXML
    private void onBuscarCompra() {
        String idC = txtIdCompra.getText().trim();
        if (idC.isBlank()) { JOptionPane.showMessageDialog(null, "Ingresa un ID de compra."); return; }

        String sql = "SELECT pr.nombre AS nombre_proveedor "
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
            } else {
                JOptionPane.showMessageDialog(null, "No se encontró la compra #" + idC);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar: " + e.getMessage());
        }
    }

    // ── Botón "📋 Registrar" ──────────────────────────────────────────────────
    @FXML
    private void onRegistrarReclamacion() {
        if (txtIdCompra.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El ID de Compra es obligatorio."); return;
        }
        if (dpFechaReclamacion.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona la fecha."); return;
        }
        if (txtDescripcion.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "La descripción es obligatoria."); return;
        }

        String sql = "INSERT INTO TBL_RECLAMACION_COMPRA (fecha_reclamacion, estado, id_compra) VALUES (?, 0, ?)";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDate(1, Date.valueOf(dpFechaReclamacion.getValue()));
            ps.setInt(2,  Integer.parseInt(txtIdCompra.getText().trim()));
            ps.executeUpdate();

            // Guardar descripción en historial
            int idReclam = -1;
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) idReclam = keys.getInt(1);

            if (idReclam != -1) {
                PreparedStatement psH = con.prepareStatement(
                        "INSERT INTO TBL_HISTORICO_RECLAMACION_COMPRA "
                                + "(descripcion, creado_por, fecha_creacion, id_reclamacioncompra) "
                                + "VALUES (?, ?, ?, ?)");
                psH.setString(1, txtDescripcion.getText().trim());
                psH.setString(2, "Usuario");
                psH.setDate(3,   Date.valueOf(LocalDate.now()));
                psH.setInt(4,    idReclam);
                psH.executeUpdate();
            }

            JOptionPane.showMessageDialog(null, "Reclamación registrada correctamente.");
            actualizarTabla();
            limpiar();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar: " + e.getMessage());
        }
    }

    // ── Botón "✔ Aprobar" ─────────────────────────────────────────────────────
    @FXML
    private void onAprobar() {
        ReclamacionCompra sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una reclamación."); return; }
        cambiarEstado(sel.getIdReclamacioncompra(), APROBADA);
    }

    // ── Botón "✖ Rechazar" ────────────────────────────────────────────────────
    @FXML
    private void onRechazar() {
        ReclamacionCompra sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una reclamación."); return; }
        cambiarEstado(sel.getIdReclamacioncompra(), RECHAZADA);
    }

    // ── Botón "🗑 Eliminar" ───────────────────────────────────────────────────
    @FXML
    private void onEliminar() {
        ReclamacionCompra sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una reclamación."); return; }

        int confirm = JOptionPane.showConfirmDialog(null,
                "¿Eliminar reclamación #" + sel.getIdReclamacioncompra() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int idReclam = sel.getIdReclamacioncompra();

        try (Connection con = conexion.establecerConexion()) {
            // Borrar hijos primero
            PreparedStatement psH = con.prepareStatement(
                    "DELETE FROM TBL_HISTORICO_RECLAMACION_COMPRA WHERE id_reclamacioncompra = ?");
            psH.setInt(1, idReclam);
            psH.executeUpdate();

            PreparedStatement psP = con.prepareStatement(
                    "DELETE FROM TBL_PRODUCTO_RECLAMACION_COMPRA WHERE id_reclamacioncompra = ?");
            psP.setInt(1, idReclam);
            psP.executeUpdate();

            PreparedStatement psR = con.prepareStatement(
                    "DELETE FROM TBL_RECLAMACION_COMPRA WHERE id_reclamacioncompra = ?");
            psR.setInt(1, idReclam);
            psR.executeUpdate();

            listaHistorico.clear();
            lblHistorialDe.setText("—");
            actualizarTabla();
            limpiar();
            JOptionPane.showMessageDialog(null, "Reclamación eliminada.");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar: " + e.getMessage());
        }
    }

    // ── Botón "🔍 Ver Historial" ──────────────────────────────────────────────
    @FXML
    private void onVerHistorial() {
        ReclamacionCompra sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una reclamación."); return; }
        cargarHistorial(sel);
    }

    // ── Botón "➕ Agregar Nota" ───────────────────────────────────────────────
    @FXML
    private void onAgregarNota() {
        ReclamacionCompra sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una reclamación."); return; }

        String nota = txtNuevaNotaHistorial.getText().trim();
        if (nota.isBlank()) { JOptionPane.showMessageDialog(null, "Escribe una nota primero."); return; }

        String sql = "INSERT INTO TBL_HISTORICO_RECLAMACION_COMPRA "
                + "(descripcion, creado_por, fecha_creacion, id_reclamacioncompra) VALUES (?, ?, ?, ?)";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nota);
            ps.setString(2, "Usuario");
            ps.setDate(3,   Date.valueOf(LocalDate.now()));
            ps.setInt(4,    sel.getIdReclamacioncompra());
            ps.executeUpdate();

            txtNuevaNotaHistorial.clear();
            cargarHistorial(sel);
            JOptionPane.showMessageDialog(null, "Nota agregada.");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al agregar nota: " + e.getMessage());
        }
    }

    // ── Botón "✖ Limpiar" ────────────────────────────────────────────────────
    @FXML
    private void onLimpiarFormulario() { limpiar(); }

    @FXML
    public void limpiar() {
        txtIdReclamacion.clear();
        txtIdCompra.clear();
        txtProveedor.clear();
        dpFechaReclamacion.setValue(LocalDate.now());
        cmbEstadoActual.setValue(PENDIENTE);
        txtCantidadDevolver.clear();
        txtDescripcion.clear();
        tablaReclamaciones.getSelectionModel().clearSelection();
    }

    // ── Cargar tabla desde BD ─────────────────────────────────────────────────
    private void actualizarTabla() {
        String sql = "SELECT r.id_reclamacioncompra, r.id_compra, "
                + "pr.nombre AS nombre_proveedor, "
                + "r.fecha_reclamacion, r.estado, "
                + "ISNULL((SELECT TOP 1 cantidad FROM TBL_PRODUCTO_RECLAMACION_COMPRA "
                + "        WHERE id_reclamacioncompra = r.id_reclamacioncompra), 0) AS cantidad, "
                + "ISNULL((SELECT TOP 1 descripcion FROM TBL_HISTORICO_RECLAMACION_COMPRA "
                + "        WHERE id_reclamacioncompra = r.id_reclamacioncompra "
                + "        ORDER BY id_historico_reclam_compra), '') AS descripcion "
                + "FROM TBL_RECLAMACION_COMPRA r "
                + "JOIN TBL_COMPRA   c  ON c.id_compra    = r.id_compra "
                + "JOIN TBL_PEDIDO_C pc ON pc.id_pedido_c = c.id_pedido_c "
                + "JOIN TBL_PROVEEDOR pr ON pr.id_proveedor = pc.id_proveedor";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            listaReclamaciones.clear();
            while (rs.next()) {
                String estadoTexto = rs.getInt("estado") == 1 ? APROBADA : PENDIENTE;
                listaReclamaciones.add(new ReclamacionCompra(
                        rs.getInt("id_reclamacioncompra"),
                        rs.getInt("id_compra"),
                        rs.getString("nombre_proveedor"),
                        rs.getDate("fecha_reclamacion").toLocalDate(),
                        estadoTexto,
                        rs.getInt("cantidad"),
                        rs.getString("descripcion")
                ));
            }
            actualizarContadores();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar reclamaciones: " + e.getMessage());
        }
    }

    // ── Cargar historial de una reclamación ───────────────────────────────────
    private void cargarHistorial(ReclamacionCompra r) {
        listaHistorico.clear();
        lblHistorialDe.setText("Rec. #" + r.getIdReclamacioncompra());

        String sql = "SELECT id_historico_reclam_compra, descripcion, creado_por, "
                + "fecha_creacion, id_reclamacioncompra "
                + "FROM TBL_HISTORICO_RECLAMACION_COMPRA "
                + "WHERE id_reclamacioncompra = ? ORDER BY id_historico_reclam_compra";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, r.getIdReclamacioncompra());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listaHistorico.add(new HistoricoReclamacionCompra(
                        rs.getInt("id_historico_reclam_compra"),
                        rs.getString("descripcion"),
                        rs.getString("creado_por"),
                        rs.getDate("fecha_creacion").toLocalDate(),
                        rs.getInt("id_reclamacioncompra")
                ));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar historial: " + e.getMessage());
        }
    }

    // ── Cambiar estado en BD ──────────────────────────────────────────────────
    private void cambiarEstado(int idReclam, String estadoUI) {
        int bit = APROBADA.equals(estadoUI) ? 1 : 0;
        String sql = "UPDATE TBL_RECLAMACION_COMPRA SET estado = ? WHERE id_reclamacioncompra = ?";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, bit);
            ps.setInt(2, idReclam);
            ps.executeUpdate();
            actualizarTabla();
            JOptionPane.showMessageDialog(null, "Estado actualizado a: " + estadoUI.replace("_", " "));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cambiar estado: " + e.getMessage());
        }
    }

    // ── Cargar formulario al seleccionar fila ─────────────────────────────────
    private void cargarEnFormulario(ReclamacionCompra r) {
        txtIdReclamacion.setText(String.valueOf(r.getIdReclamacioncompra()));
        txtIdCompra.setText(String.valueOf(r.getIdCompra()));
        txtProveedor.setText(r.getNombreProveedor());
        dpFechaReclamacion.setValue(r.getFechaReclamacion());
        cmbEstadoActual.setValue(r.getEstadoActualNombre());
        txtCantidadDevolver.setText(String.valueOf(r.getCantidadAdevolver()));
        txtDescripcion.setText(r.getDescripcion());
    }

    // ── Pastillas de conteo ───────────────────────────────────────────────────
    private void actualizarContadores() {
        long pend  = listaReclamaciones.stream().filter(r -> PENDIENTE.equals(r.getEstadoActualNombre())).count();
        long rev   = listaReclamaciones.stream().filter(r -> EN_REVISION.equals(r.getEstadoActualNombre())).count();
        long aprob = listaReclamaciones.stream().filter(r -> APROBADA.equals(r.getEstadoActualNombre())).count();
        long rech  = listaReclamaciones.stream().filter(r -> RECHAZADA.equals(r.getEstadoActualNombre())).count();
        lblContPendiente.setText("⏳  " + pend  + "  Pendientes");
        lblContRevision.setText("🔍  "  + rev   + "  En Revisión");
        lblContAprobada.setText("✔  "   + aprob + "  Aprobadas");
        lblContRechazada.setText("✖  "  + rech  + "  Rechazadas");
    }
}