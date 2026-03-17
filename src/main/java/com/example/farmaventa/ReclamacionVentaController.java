package com.example.farmaventa;

import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.HistoricoReclamacion;
import com.example.farmaventa.modelo.ReclamacionVenta;
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

public class ReclamacionVentaController implements Initializable {

    Conexion conexion = new Conexion();

    // Formulario
    @FXML private TextField        txtIdReclamacion;
    @FXML private TextField        txtIdVenta;
    @FXML private TextField        txtCliente;
    @FXML private DatePicker       dpFechaReclamacion;
    @FXML private ComboBox<String> cmbEstadoActual;
    @FXML private TextField        txtCantidadDevolver;
    @FXML private TextField        txtDescripcion;

    // Tabla reclamaciones
    @FXML private TableView<ReclamacionVenta>              tablaReclamaciones;
    @FXML private TableColumn<ReclamacionVenta, Integer>   colId;
    @FXML private TableColumn<ReclamacionVenta, String>    colVenta;
    @FXML private TableColumn<ReclamacionVenta, String>    colCliente;
    @FXML private TableColumn<ReclamacionVenta, LocalDate> colFecha;
    @FXML private TableColumn<ReclamacionVenta, String>    colEstado;
    @FXML private TableColumn<ReclamacionVenta, Integer>   colCantidad;
    @FXML private TableColumn<ReclamacionVenta, String>    colDescripcion;

    // Filtros
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TextField        txtBusqueda;

    // Historial
    @FXML private TableView<HistoricoReclamacion>              tablaHistorico;
    @FXML private TableColumn<HistoricoReclamacion, Integer>   colHistId;
    @FXML private TableColumn<HistoricoReclamacion, LocalDate> colHistFecha;
    @FXML private TableColumn<HistoricoReclamacion, String>    colHistDescripcion;
    @FXML private TextArea txtDetalleHistorial;
    @FXML private TextArea txtNuevaNotaHistorial;
    @FXML private Label    lblHistorialDe;

    // Pastillas
    @FXML private Label lblContPendiente;
    @FXML private Label lblContRevision;
    @FXML private Label lblContAprobada;
    @FXML private Label lblContRechazada;

    private final ObservableList<ReclamacionVenta>     listaReclamaciones = FXCollections.observableArrayList();
    private final ObservableList<HistoricoReclamacion> listaHistorico     = FXCollections.observableArrayList();
    private FilteredList<ReclamacionVenta> listaFiltrada;

    private static final String PENDIENTE   = "ESTADO_RECLAMACION_PENDIENTE";
    private static final String EN_REVISION = "ESTADO_EN_REVISION";
    private static final String APROBADA    = "ESTADO_APROBADA";
    private static final String RECHAZADA   = "ESTADO_RECHAZADA";

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

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idReclamacionventa"));
        colVenta.setCellValueFactory(new PropertyValueFactory<>("idVenta"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaReclamacion"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoActualNombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidadAdevolver"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        // colores por estado
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) { setText(null); setStyle(""); return; }
                setText(estado.replace("ESTADO_RECLAMACION_", "").replace("ESTADO_", "").replace("_", " "));
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
                    || String.valueOf(r.getIdReclamacionventa()).contains(busq)
                    || r.getNombreCliente().toLowerCase().contains(busq)
                    || String.valueOf(r.getIdVenta()).contains(busq);
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

    // Botón "🔎 Buscar" → onAction="#onBuscarVenta"
    @FXML
    private void onBuscarVenta() {
        String idV = txtIdVenta.getText().trim();
        if (idV.isBlank()) { JOptionPane.showMessageDialog(null, "Ingresa un ID de venta."); return; }

        String sql = "SELECT p.nombre + ' ' + p.apellido AS nombre_cliente "
                + "FROM TBL_VENTA v "
                + "JOIN TBL_CLIENTE c ON c.id_cliente = v.id_cliente "
                + "JOIN TBL_PERSONA p ON p.id_persona = c.id_persona "
                + "WHERE v.id_venta = ?";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(idV));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtCliente.setText(rs.getString("nombre_cliente"));
            } else {
                JOptionPane.showMessageDialog(null, "No se encontró la venta #" + idV);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar: " + e.getMessage());
        }
    }

    // Botón "📋 Registrar" → onAction="#onRegistrarReclamacion"
    @FXML
    private void onRegistrarReclamacion() {
        if (txtIdVenta.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El ID de Venta es obligatorio."); return;
        }
        if (dpFechaReclamacion.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona la fecha."); return;
        }
        if (txtDescripcion.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "La descripción es obligatoria."); return;
        }

        String sql = "INSERT INTO TBL_RECLAMACION_VENTA (fecha_reclamacion, estado, id_venta) VALUES (?, 0, ?)";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDate(1, Date.valueOf(dpFechaReclamacion.getValue()));
            ps.setInt(2,  Integer.parseInt(txtIdVenta.getText().trim()));
            ps.executeUpdate();

            // guardar descripción en el historial
            int idReclam = -1;
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) idReclam = keys.getInt(1);

            if (idReclam != -1) {
                PreparedStatement psH = con.prepareStatement(
                        "INSERT INTO TBL_HISTORICO_RECLAMACION_VENTA (descripcion, creado_por, fecha_creacion, id_reclamacionventa) "
                                + "VALUES (?, ?, ?, ?)");
                psH.setString(1, txtDescripcion.getText().trim());
                psH.setString(2, "Usuario");
                psH.setDate(3,   Date.valueOf(LocalDate.now()));
                psH.setInt(4,    idReclam);
                psH.executeUpdate();
            }

            JOptionPane.showMessageDialog(null, "Reclamación registrada correctamente.");
            actualizarTabla();
            Limpiar();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar: " + e.getMessage());
        }
    }

    // Botón "✔ Aprobar" → onAction="#onAprobar"
    @FXML
    private void onAprobar() {
        ReclamacionVenta sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una reclamación."); return; }
        cambiarEstado(sel.getIdReclamacionventa(), 1, APROBADA);
    }

    // Botón "✖ Rechazar" → onAction="#onRechazar"
    @FXML
    private void onRechazar() {
        ReclamacionVenta sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una reclamación."); return; }
        cambiarEstado(sel.getIdReclamacionventa(), 0, RECHAZADA);
    }

    // Botón "🗑 Eliminar" → onAction="#onEliminar"
    @FXML
    private void onEliminar() {
        ReclamacionVenta sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una reclamación."); return; }

        int confirm = JOptionPane.showConfirmDialog(null,
                "¿Eliminar reclamación #" + sel.getIdReclamacionventa() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM TBL_RECLAMACION_VENTA WHERE id_reclamacionventa=?";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, sel.getIdReclamacionventa());
            ps.executeUpdate();
            listaHistorico.clear();
            lblHistorialDe.setText("—");
            actualizarTabla();
            Limpiar();
            JOptionPane.showMessageDialog(null, "Reclamación eliminada.");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar: " + e.getMessage());
        }
    }

    // Botón "🔍 Ver Historial" → onAction="#onVerHistorial"
    @FXML
    private void onVerHistorial() {
        ReclamacionVenta sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una reclamación."); return; }
        cargarHistorial(sel);
    }

    // Botón "➕ Agregar Nota" → onAction="#onAgregarNota"
    @FXML
    private void onAgregarNota() {
        ReclamacionVenta sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una reclamación."); return; }

        String nota = txtNuevaNotaHistorial.getText().trim();
        if (nota.isBlank()) { JOptionPane.showMessageDialog(null, "Escribe una nota primero."); return; }

        String sql = "INSERT INTO TBL_HISTORICO_RECLAMACION_VENTA (descripcion, creado_por, fecha_creacion, id_reclamacionventa) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nota);
            ps.setString(2, "Usuario");
            ps.setDate(3,   Date.valueOf(LocalDate.now()));
            ps.setInt(4,    sel.getIdReclamacionventa());
            ps.executeUpdate();

            txtNuevaNotaHistorial.clear();
            cargarHistorial(sel);
            JOptionPane.showMessageDialog(null, "Nota agregada.");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al agregar nota: " + e.getMessage());
        }
    }

    // Botón "✖ Limpiar" → onAction="#onLimpiarFormulario"
    @FXML
    private void onLimpiarFormulario() { Limpiar(); }

    @FXML
    public void Limpiar() {
        txtIdReclamacion.clear();
        txtIdVenta.clear();
        txtCliente.clear();
        dpFechaReclamacion.setValue(LocalDate.now());
        cmbEstadoActual.setValue(PENDIENTE);
        txtCantidadDevolver.clear();
        txtDescripcion.clear();
        tablaReclamaciones.getSelectionModel().clearSelection();
    }

    private void actualizarTabla() {
        String sql = "SELECT r.id_reclamacionventa, r.id_venta, "
                + "p.nombre + ' ' + p.apellido AS nombre_cliente, "
                + "r.fecha_reclamacion, r.estado, "
                + "ISNULL((SELECT TOP 1 cantidad FROM TBL_PRODUCTO_RECLAMACION_VENTA WHERE id_reclamacionventa = r.id_reclamacionventa), 0) AS cantidad, "
                + "ISNULL((SELECT TOP 1 descripcion FROM TBL_HISTORICO_RECLAMACION_VENTA WHERE id_reclamacionventa = r.id_reclamacionventa ORDER BY id_historico_reclam_venta), '') AS descripcion "
                + "FROM TBL_RECLAMACION_VENTA r "
                + "JOIN TBL_VENTA   v ON v.id_venta   = r.id_venta "
                + "JOIN TBL_CLIENTE c ON c.id_cliente = v.id_cliente "
                + "JOIN TBL_PERSONA p ON p.id_persona = c.id_persona";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            listaReclamaciones.clear();
            while (rs.next()) {
                // estado BIT: 1 = aprobada, 0 = pendiente
                String estadoTexto = rs.getInt("estado") == 1 ? APROBADA : PENDIENTE;
                listaReclamaciones.add(new ReclamacionVenta(
                        rs.getInt("id_reclamacionventa"),
                        rs.getInt("id_venta"),
                        rs.getString("nombre_cliente"),
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

    private void cargarHistorial(ReclamacionVenta r) {
        listaHistorico.clear();
        lblHistorialDe.setText("Rec. #" + r.getIdReclamacionventa());

        String sql = "SELECT id_historico_reclam_venta, descripcion, creado_por, fecha_creacion, id_reclamacionventa "
                + "FROM TBL_HISTORICO_RECLAMACION_VENTA "
                + "WHERE id_reclamacionventa = ? ORDER BY id_historico_reclam_venta";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, r.getIdReclamacionventa());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listaHistorico.add(new HistoricoReclamacion(
                        rs.getInt("id_historico_reclam_venta"),
                        rs.getString("descripcion"),
                        rs.getString("creado_por"),
                        rs.getDate("fecha_creacion").toLocalDate(),
                        rs.getInt("id_reclamacionventa")
                ));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar historial: " + e.getMessage());
        }
    }

    private void cambiarEstado(int idReclam, int bit, String estadoUI) {
        String sql = "UPDATE TBL_RECLAMACION_VENTA SET estado=? WHERE id_reclamacionventa=?";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, bit);
            ps.setInt(2, idReclam);
            ps.executeUpdate();
            actualizarTabla();
            JOptionPane.showMessageDialog(null, "Estado actualizado.");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cambiar estado: " + e.getMessage());
        }
    }

    private void cargarEnFormulario(ReclamacionVenta r) {
        txtIdReclamacion.setText(String.valueOf(r.getIdReclamacionventa()));
        txtIdVenta.setText(String.valueOf(r.getIdVenta()));
        txtCliente.setText(r.getNombreCliente());
        dpFechaReclamacion.setValue(r.getFechaReclamacion());
        cmbEstadoActual.setValue(r.getEstadoActualNombre());
        txtCantidadDevolver.setText(String.valueOf(r.getCantidadAdevolver()));
        txtDescripcion.setText(r.getDescripcion());
    }

    private void actualizarContadores() {
        long pend  = listaReclamaciones.stream().filter(r -> PENDIENTE.equals(r.getEstadoActualNombre())).count();
        long rev   = listaReclamaciones.stream().filter(r -> EN_REVISION.equals(r.getEstadoActualNombre())).count();
        long aprob = listaReclamaciones.stream().filter(r -> APROBADA.equals(r.getEstadoActualNombre())).count();
        long rech  = listaReclamaciones.stream().filter(r -> RECHAZADA.equals(r.getEstadoActualNombre())).count();
        lblContPendiente.setText("⏳  " + pend  + "  Pendientes");
        lblContRevision.setText("🔍  " + rev   + "  En Revisión");
        lblContAprobada.setText("✔  "  + aprob + "  Aprobadas");
        lblContRechazada.setText("✖  " + rech  + "  Rechazadas");
    }
}