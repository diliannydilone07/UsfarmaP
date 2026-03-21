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

    // ── Formulario ────────────────────────────────────────────────────────
    @FXML private TextField        txtIdReclamacion;
    @FXML private TextField        txtIdVenta;
    @FXML private TextField        txtCliente;
    @FXML private DatePicker       dpFechaReclamacion;
    @FXML private ComboBox<String> cmbEstadoActual;
    @FXML private TextField        txtCantidadDevolver;
    @FXML private TextField        txtDescripcion;
    @FXML private TextField        txtIdProducto;       // ← NUEVO
    @FXML private TextField        txtNombreProducto;   // ← NUEVO (solo lectura)

    // ── Tabla reclamaciones ───────────────────────────────────────────────
    @FXML private TableView<ReclamacionVenta>              tablaReclamaciones;
    @FXML private TableColumn<ReclamacionVenta, Integer>   colId;
    @FXML private TableColumn<ReclamacionVenta, String>    colVenta;
    @FXML private TableColumn<ReclamacionVenta, String>    colProducto;    // ← NUEVO
    @FXML private TableColumn<ReclamacionVenta, String>    colCliente;
    @FXML private TableColumn<ReclamacionVenta, LocalDate> colFecha;
    @FXML private TableColumn<ReclamacionVenta, String>    colEstado;
    @FXML private TableColumn<ReclamacionVenta, Integer>   colCantidad;
    @FXML private TableColumn<ReclamacionVenta, String>    colDescripcion;

    // ── Filtros ───────────────────────────────────────────────────────────
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TextField        txtBusqueda;

    // ── Historial ─────────────────────────────────────────────────────────
    @FXML private TableView<HistoricoReclamacion>              tablaHistorico;
    @FXML private TableColumn<HistoricoReclamacion, Integer>   colHistId;
    @FXML private TableColumn<HistoricoReclamacion, LocalDate> colHistFecha;
    @FXML private TableColumn<HistoricoReclamacion, String>    colHistDescripcion;
    @FXML private TextArea txtDetalleHistorial;
    @FXML private TextArea txtNuevaNotaHistorial;
    @FXML private Label    lblHistorialDe;

    // ── Pastillas ─────────────────────────────────────────────────────────
    @FXML private Label lblContPendiente;
    @FXML private Label lblContRevision;
    @FXML private Label lblContAprobada;
    @FXML private Label lblContRechazada;

    // ── Listas ───────────────────────────────────────────────────────────
    private final ObservableList<ReclamacionVenta>     listaReclamaciones = FXCollections.observableArrayList();
    private final ObservableList<HistoricoReclamacion> listaHistorico     = FXCollections.observableArrayList();
    private FilteredList<ReclamacionVenta> listaFiltrada;

    private static final String PENDIENTE   = "Pendiente";
    private static final String EN_REVISION = "En Revisión";
    private static final String APROBADA    = "Aprobada";
    private static final String RECHAZADA   = "Rechazada";

    private static final ObservableList<String> ESTADOS =
            FXCollections.observableArrayList(PENDIENTE, EN_REVISION, APROBADA, RECHAZADA);

    // ══════════════════════════════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Columnas tabla principal
        colId.setCellValueFactory(new PropertyValueFactory<>("idReclamacionventa"));
        colVenta.setCellValueFactory(new PropertyValueFactory<>("idVenta"));
        colProducto.setCellValueFactory(new PropertyValueFactory<>("nombreProducto")); // ← NUEVO
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaReclamacion"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoActualNombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidadAdevolver"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v);
                setStyle(switch (v) {
                    case PENDIENTE   -> "-fx-text-fill: #F57F17; -fx-font-weight: bold;";
                    case EN_REVISION -> "-fx-text-fill: #1565C0; -fx-font-weight: bold;";
                    case APROBADA    -> "-fx-text-fill: #2E7D32; -fx-font-weight: bold;";
                    case RECHAZADA   -> "-fx-text-fill: #C62828; -fx-font-weight: bold;";
                    default          -> "";
                });
            }
        });

        listaFiltrada = new FilteredList<>(listaReclamaciones, p -> true);
        tablaReclamaciones.setItems(listaFiltrada);

        // Columnas historial
        colHistId.setCellValueFactory(new PropertyValueFactory<>("idHistorico"));
        colHistFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));
        colHistDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        tablaHistorico.setItems(listaHistorico);
        tablaHistorico.getSelectionModel().selectedItemProperty()
                .addListener((o, v, h) -> { if (h != null) txtDetalleHistorial.setText(h.obtenerDetalleCambio()); });

        // Combos
        cmbEstadoActual.setItems(ESTADOS);
        cmbEstadoActual.setValue(PENDIENTE);
        ObservableList<String> filtros = FXCollections.observableArrayList("Todos");
        filtros.addAll(ESTADOS);
        cmbFiltroEstado.setItems(filtros);
        cmbFiltroEstado.setValue("Todos");

        // Filtros
        cmbFiltroEstado.valueProperty().addListener((o, v, n) -> aplicarFiltros());
        txtBusqueda.textProperty().addListener((o, v, n) -> aplicarFiltros());

        // Selección en tabla
        tablaReclamaciones.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> { if (sel != null) { cargarEnFormulario(sel); cargarHistorial(sel); } });

        dpFechaReclamacion.setValue(LocalDate.now());
        actualizarTabla();
    }

    // ── Filtro ────────────────────────────────────────────────────────────
    private void aplicarFiltros() {
        String estado = cmbFiltroEstado.getValue();
        String busq   = txtBusqueda.getText().toLowerCase();
        listaFiltrada.setPredicate(r -> {
            boolean okEstado = "Todos".equals(estado) || estado == null
                    || r.getEstadoActualNombre().equals(estado);
            boolean okBusq   = busq.isEmpty()
                    || String.valueOf(r.getIdReclamacionventa()).contains(busq)
                    || r.getNombreCliente().toLowerCase().contains(busq)
                    || String.valueOf(r.getIdVenta()).contains(busq);
            return okEstado && okBusq;
        });
    }

    // ── Buscar cliente por venta ──────────────────────────────────────────
    @FXML
    private void onBuscarVenta() {
        if (txtIdVenta.getText().isBlank()) return;
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT p.nombre + ' ' + p.apellido AS nc " +
                             "FROM TBL_VENTA v " +
                             "JOIN TBL_CLIENTE c ON c.id_cliente = v.id_cliente " +
                             "JOIN TBL_PERSONA p ON p.id_persona = c.id_persona " +
                             "WHERE v.id_venta = ?")) {
            ps.setInt(1, Integer.parseInt(txtIdVenta.getText().trim()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) txtCliente.setText(rs.getString("nc"));
            else JOptionPane.showMessageDialog(null, "Venta no encontrada.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    // ── Buscar producto por ID ────────────────────────────────────────────
    @FXML
    private void onBuscarProducto() {
        if (txtIdProducto.getText().isBlank()) return;
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT nombre FROM TBL_PRODUCTO WHERE id_producto = ?")) {
            ps.setInt(1, Integer.parseInt(txtIdProducto.getText().trim()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) txtNombreProducto.setText(rs.getString("nombre"));
            else JOptionPane.showMessageDialog(null, "Producto no encontrado.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    // ── Registrar o Actualizar ────────────────────────────────────────────
    @FXML
    private void onRegistrarReclamacion() {
        if (txtIdVenta.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El ID de Venta es obligatorio."); return;
        }
        if (dpFechaReclamacion.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona la fecha."); return;
        }
        if (!txtIdReclamacion.getText().isBlank()) {
            actualizar(); return;
        }
        // Registro nuevo
        if (txtDescripcion.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "La descripción es obligatoria."); return;
        }
        if (txtIdProducto.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El ID de Producto es obligatorio."); return;
        }
        registrarNuevo();
    }

    private void registrarNuevo() {
        String estado = cmbEstadoActual.getValue() != null ? cmbEstadoActual.getValue() : PENDIENTE;
        int idProducto;
        int cantidad;
        try {
            idProducto = Integer.parseInt(txtIdProducto.getText().trim());
            cantidad   = txtCantidadDevolver.getText().isBlank() ? 0
                    : Integer.parseInt(txtCantidadDevolver.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID Producto y Cantidad deben ser números."); return;
        }

        Connection con = null;
        try {
            con = conexion.establecerConexion();
            con.setAutoCommit(false); // ← transacción

            // 1. Insertar TBL_RECLAMACION_VENTA
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO TBL_RECLAMACION_VENTA (fecha_reclamacion, estado, id_venta) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setDate(1, Date.valueOf(dpFechaReclamacion.getValue()));
            ps.setInt(2, APROBADA.equals(estado) ? 1 : 0);
            ps.setInt(3, Integer.parseInt(txtIdVenta.getText().trim()));
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (!keys.next()) { con.rollback(); return; }
            int idReclam = keys.getInt(1);

            // 2. Insertar TBL_PRODUCTO_RECLAMACION_VENTA
            PreparedStatement psProd = con.prepareStatement(
                    "INSERT INTO TBL_PRODUCTO_RECLAMACION_VENTA (id_producto, id_reclamacionventa, cantidad, descripcion) VALUES (?,?,?,?)");
            psProd.setInt(1, idProducto);
            psProd.setInt(2, idReclam);
            psProd.setInt(3, cantidad);
            psProd.setString(4, txtDescripcion.getText().trim());
            psProd.executeUpdate();

            // 3. Historial
            insertarHistorial(con, idReclam,
                    "Estado: " + estado + " — " + txtDescripcion.getText().trim());

            con.commit(); // ← confirmar todo junto
            JOptionPane.showMessageDialog(null, "✔ Reclamación registrada.");
            actualizarTabla();
            Limpiar();

        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
            JOptionPane.showMessageDialog(null, "Error al registrar: " + e.getMessage());
        } finally {
            try { if (con != null) con.close(); } catch (SQLException ignored) {}
        }
    }

    private void actualizar() {
        int id = Integer.parseInt(txtIdReclamacion.getText().trim());
        String estado = cmbEstadoActual.getValue() != null ? cmbEstadoActual.getValue() : PENDIENTE;
        String desc   = txtDescripcion.getText().trim();

        try (Connection con = conexion.establecerConexion()) {

            // Actualizar cabecera
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE TBL_RECLAMACION_VENTA SET fecha_reclamacion=?, estado=?, id_venta=? WHERE id_reclamacionventa=?");
            ps.setDate(1, Date.valueOf(dpFechaReclamacion.getValue()));
            ps.setInt(2, APROBADA.equals(estado) ? 1 : 0);
            ps.setInt(3, Integer.parseInt(txtIdVenta.getText().trim()));
            ps.setInt(4, id);
            ps.executeUpdate();

            // Actualizar cantidad y descripción en producto_reclamacion si hay producto
            if (!txtIdProducto.getText().isBlank()) {
                int idProducto = Integer.parseInt(txtIdProducto.getText().trim());
                int cantidad   = txtCantidadDevolver.getText().isBlank() ? 0
                        : Integer.parseInt(txtCantidadDevolver.getText().trim());

                // Si ya existe el registro → UPDATE, si no → INSERT
                PreparedStatement psCheck = con.prepareStatement(
                        "SELECT COUNT(*) FROM TBL_PRODUCTO_RECLAMACION_VENTA WHERE id_reclamacionventa=?");
                psCheck.setInt(1, id);
                ResultSet rsCheck = psCheck.executeQuery();
                rsCheck.next();
                int existe = rsCheck.getInt(1);

                if (existe > 0) {
                    PreparedStatement psUpd = con.prepareStatement(
                            "UPDATE TBL_PRODUCTO_RECLAMACION_VENTA SET cantidad=?, descripcion=?, id_producto=? WHERE id_reclamacionventa=?");
                    psUpd.setInt(1, cantidad);
                    psUpd.setString(2, desc.isBlank() ? null : desc);
                    psUpd.setInt(3, idProducto);
                    psUpd.setInt(4, id);
                    psUpd.executeUpdate();
                } else {
                    PreparedStatement psIns = con.prepareStatement(
                            "INSERT INTO TBL_PRODUCTO_RECLAMACION_VENTA (id_producto, id_reclamacionventa, cantidad, descripcion) VALUES (?,?,?,?)");
                    psIns.setInt(1, idProducto);
                    psIns.setInt(2, id);
                    psIns.setInt(3, cantidad);
                    psIns.setString(4, desc.isBlank() ? "" : desc);
                    psIns.executeUpdate();
                }
            }

            // Registrar en historial
            insertarHistorial(con, id,
                    "Estado: " + estado + (desc.isBlank() ? "" : " — " + desc));

            JOptionPane.showMessageDialog(null, "✔ Reclamación #" + id + " actualizada.");
            actualizarTabla();
            Limpiar();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    // ── Aprobar / Rechazar rápido ─────────────────────────────────────────
    @FXML private void onAprobar() {
        ReclamacionVenta sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una reclamación."); return; }
        cambioRapido(sel, APROBADA);
    }

    @FXML private void onRechazar() {
        ReclamacionVenta sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una reclamación."); return; }
        cambioRapido(sel, RECHAZADA);
    }

    private void cambioRapido(ReclamacionVenta r, String nuevoEstado) {
        try (Connection con = conexion.establecerConexion()) {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE TBL_RECLAMACION_VENTA SET estado=? WHERE id_reclamacionventa=?");
            ps.setInt(1, APROBADA.equals(nuevoEstado) ? 1 : 0);
            ps.setInt(2, r.getIdReclamacionventa());
            ps.executeUpdate();
            insertarHistorial(con, r.getIdReclamacionventa(), "Estado: " + nuevoEstado);
            actualizarTabla();
            JOptionPane.showMessageDialog(null, "Estado → " + nuevoEstado);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    // ── Eliminar ──────────────────────────────────────────────────────────
    @FXML private void onEliminar() {
        ReclamacionVenta sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una reclamación."); return; }
        if (JOptionPane.showConfirmDialog(null,
                "¿Eliminar reclamación #" + sel.getIdReclamacionventa() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        int id = sel.getIdReclamacionventa();
        try (Connection con = conexion.establecerConexion()) {
            con.prepareStatement("DELETE FROM TBL_HISTORICO_RECLAMACION_VENTA WHERE id_reclamacionventa=" + id).executeUpdate();
            con.prepareStatement("DELETE FROM TBL_PRODUCTO_RECLAMACION_VENTA  WHERE id_reclamacionventa=" + id).executeUpdate();
            con.prepareStatement("DELETE FROM TBL_RECLAMACION_VENTA           WHERE id_reclamacionventa=" + id).executeUpdate();
            actualizarTabla();
            Limpiar();
            JOptionPane.showMessageDialog(null, "Reclamación eliminada.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    @FXML private void onVerHistorial() {
        ReclamacionVenta sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel != null) cargarHistorial(sel);
        else JOptionPane.showMessageDialog(null, "Selecciona una reclamación.");
    }

    @FXML private void onAgregarNota() {
        ReclamacionVenta sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una reclamación."); return; }
        String nota = txtNuevaNotaHistorial.getText().trim();
        if (nota.isBlank()) { JOptionPane.showMessageDialog(null, "Escribe una nota."); return; }
        try (Connection con = conexion.establecerConexion()) {
            insertarHistorial(con, sel.getIdReclamacionventa(), nota);
            txtNuevaNotaHistorial.clear();
            cargarHistorial(sel);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    @FXML private void onLimpiarFormulario() { Limpiar(); }

    @FXML
    public void Limpiar() {
        txtIdReclamacion.clear();
        txtIdVenta.clear();
        txtCliente.clear();
        txtIdProducto.clear();
        txtNombreProducto.clear();
        dpFechaReclamacion.setValue(LocalDate.now());
        cmbEstadoActual.setValue(PENDIENTE);
        txtCantidadDevolver.clear();
        txtDescripcion.clear();
        cmbFiltroEstado.setValue("Todos");
        txtBusqueda.clear();
        listaHistorico.clear();
        lblHistorialDe.setText("—");
        txtDetalleHistorial.clear();
        tablaReclamaciones.getSelectionModel().clearSelection();
    }

    // ── Cargar tabla ──────────────────────────────────────────────────────
    private void actualizarTabla() {
        String sql =
                "SELECT r.id_reclamacionventa, r.id_venta, r.fecha_reclamacion, r.estado, " +
                        "       p.nombre + ' ' + p.apellido AS nombre_cliente, " +
                        // nombre del producto reclamado
                        "       ISNULL((SELECT TOP 1 pr.nombre FROM TBL_PRODUCTO_RECLAMACION_VENTA prv " +
                        "               JOIN TBL_PRODUCTO pr ON pr.id_producto = prv.id_producto " +
                        "               WHERE prv.id_reclamacionventa = r.id_reclamacionventa), '') AS nombre_producto, " +
                        // cantidad
                        "       ISNULL((SELECT TOP 1 cantidad FROM TBL_PRODUCTO_RECLAMACION_VENTA " +
                        "               WHERE id_reclamacionventa = r.id_reclamacionventa), 0) AS cantidad, " +
                        // descripción: primero de producto_reclamacion, luego de historial
                        "       ISNULL(" +
                        "           (SELECT TOP 1 descripcion FROM TBL_PRODUCTO_RECLAMACION_VENTA " +
                        "            WHERE id_reclamacionventa = r.id_reclamacionventa), " +
                        "           ISNULL((SELECT TOP 1 descripcion FROM TBL_HISTORICO_RECLAMACION_VENTA " +
                        "                   WHERE id_reclamacionventa = r.id_reclamacionventa " +
                        "                   ORDER BY id_historico_reclam_venta), '')" +
                        "       ) AS descripcion, " +
                        // estado real desde historial
                        "       ISNULL((SELECT TOP 1 descripcion FROM TBL_HISTORICO_RECLAMACION_VENTA " +
                        "               WHERE id_reclamacionventa = r.id_reclamacionventa " +
                        "                 AND descripcion LIKE 'Estado:%' " +
                        "               ORDER BY id_historico_reclam_venta DESC), '') AS ultimo_estado " +
                        "FROM TBL_RECLAMACION_VENTA r " +
                        "JOIN TBL_VENTA v   ON v.id_venta   = r.id_venta " +
                        "JOIN TBL_CLIENTE c ON c.id_cliente = v.id_cliente " +
                        "JOIN TBL_PERSONA p ON p.id_persona = c.id_persona " +
                        "ORDER BY r.id_reclamacionventa DESC";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            listaReclamaciones.clear();
            while (rs.next()) {
                // Estado
                String ult = rs.getString("ultimo_estado");
                String estadoFinal;
                if (ult != null && ult.startsWith("Estado:")) {
                    String parte = ult.substring(7).trim();
                    int dash = parte.indexOf(" — ");
                    estadoFinal = dash >= 0 ? parte.substring(0, dash).trim() : parte.trim();
                    if (!ESTADOS.contains(estadoFinal))
                        estadoFinal = rs.getInt("estado") == 1 ? APROBADA : PENDIENTE;
                } else {
                    estadoFinal = rs.getInt("estado") == 1 ? APROBADA : PENDIENTE;
                }

                // Descripción
                String desc = rs.getString("descripcion");
                if (desc != null && desc.startsWith("Estado:")) {
                    int dash = desc.indexOf(" — ");
                    desc = dash >= 0 ? desc.substring(dash + 3).trim() : "";
                }

                ReclamacionVenta rv = new ReclamacionVenta(
                        rs.getInt("id_reclamacionventa"),
                        rs.getInt("id_venta"),
                        rs.getString("nombre_cliente"),
                        rs.getDate("fecha_reclamacion").toLocalDate(),
                        estadoFinal,
                        rs.getInt("cantidad"),
                        desc != null ? desc : ""
                );
                rv.setNombreProducto(rs.getString("nombre_producto")); // ← NUEVO
                listaReclamaciones.add(rv);
            }
            actualizarContadores();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar: " + e.getMessage());
        }
    }

    // ── Historial ─────────────────────────────────────────────────────────
    private void cargarHistorial(ReclamacionVenta r) {
        listaHistorico.clear();
        lblHistorialDe.setText("Rec. #" + r.getIdReclamacionventa());
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM TBL_HISTORICO_RECLAMACION_VENTA " +
                             "WHERE id_reclamacionventa = ? ORDER BY id_historico_reclam_venta DESC")) {
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
            JOptionPane.showMessageDialog(null, "Error historial: " + e.getMessage());
        }
    }

    private void cargarEnFormulario(ReclamacionVenta r) {
        txtIdReclamacion.setText(String.valueOf(r.getIdReclamacionventa()));
        txtIdVenta.setText(String.valueOf(r.getIdVenta()));
        txtCliente.setText(r.getNombreCliente());
        dpFechaReclamacion.setValue(r.getFechaReclamacion());
        cmbEstadoActual.setValue(ESTADOS.contains(r.getEstadoActualNombre())
                ? r.getEstadoActualNombre() : PENDIENTE);
        txtCantidadDevolver.setText(String.valueOf(r.getCantidadAdevolver()));
        txtDescripcion.setText(r.getDescripcion() != null ? r.getDescripcion() : "");
        // Cargar producto si existe
        txtNombreProducto.setText(r.getNombreProducto() != null ? r.getNombreProducto() : "");
        // Buscar el id_producto de esta reclamación
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id_producto FROM TBL_PRODUCTO_RECLAMACION_VENTA WHERE id_reclamacionventa=?")) {
            ps.setInt(1, r.getIdReclamacionventa());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) txtIdProducto.setText(String.valueOf(rs.getInt("id_producto")));
            else txtIdProducto.clear();
        } catch (SQLException e) { txtIdProducto.clear(); }
    }

    private void insertarHistorial(Connection con, int idReclam, String texto) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
                "INSERT INTO TBL_HISTORICO_RECLAMACION_VENTA " +
                        "(descripcion, creado_por, fecha_creacion, id_reclamacionventa) VALUES (?,?,?,?)");
        ps.setString(1, texto);
        ps.setString(2, "Usuario");
        ps.setDate(3, Date.valueOf(LocalDate.now()));
        ps.setInt(4, idReclam);
        ps.executeUpdate();
    }

    private void actualizarContadores() {
        long pend  = listaReclamaciones.stream().filter(r -> PENDIENTE.equals(r.getEstadoActualNombre())).count();
        long rev   = listaReclamaciones.stream().filter(r -> EN_REVISION.equals(r.getEstadoActualNombre())).count();
        long aprob = listaReclamaciones.stream().filter(r -> APROBADA.equals(r.getEstadoActualNombre())).count();
        long rech  = listaReclamaciones.stream().filter(r -> RECHAZADA.equals(r.getEstadoActualNombre())).count();
        lblContPendiente.setText("⏳ " + pend  + " Pendientes");
        lblContRevision.setText( "🔍 " + rev   + " En Revisión");
        lblContAprobada.setText( "✔ "  + aprob + " Aprobadas");
        lblContRechazada.setText("✖ "  + rech  + " Rechazadas");
    }
}