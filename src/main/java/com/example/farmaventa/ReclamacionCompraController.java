package com.example.farmaventa;

import Usuarios.Permisos;
import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.HistoricoReclamacionCompra;
import com.example.farmaventa.modelo.ReclamacionCompra;
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

public class ReclamacionCompraController implements Initializable {

    Conexion conexion = new Conexion();

    // ── Formulario ────────────────────────────────────────────────────────────
    // ── Botones con restricción de permisos ───────────────────────────────
    @FXML private Button btnRegistrarReclamacion;
    @FXML private Button btnAprobar;
    @FXML private Button btnRechazar;
    @FXML private Button btnEliminar;

    @FXML private TextField        txtIdReclamacion;
    @FXML private TextField        txtIdCompra;
    @FXML private TextField        txtProveedor;
    @FXML private DatePicker       dpFechaReclamacion;
    @FXML private ComboBox<String> cmbEstadoActual;
    @FXML private TextField        txtCantidadDevolver;
    @FXML private TextField        txtDescripcion;
    @FXML private TextField        txtIdProducto;      // ← NUEVO
    @FXML private TextField        txtNombreProducto;  // ← NUEVO

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
    private ObservableList<ReclamacionCompra>          listaReclamaciones = FXCollections.observableArrayList();
    private ObservableList<HistoricoReclamacionCompra> listaHistorico     = FXCollections.observableArrayList();

    private static final String PENDIENTE   = "PENDIENTE";
    private static final String EN_REVISION = "EN_REVISION";
    private static final String APROBADA    = "APROBADA";
    private static final String RECHAZADA   = "RECHAZADA";

    // ── Inicializar ───────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idReclamacioncompra"));
        colCompra.setCellValueFactory(new PropertyValueFactory<>("idCompra"));
        colProveedor.setCellValueFactory(new PropertyValueFactory<>("nombreProveedor"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaReclamacion"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoActualNombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidadAdevolver"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        tablaReclamaciones.setItems(listaReclamaciones);

        colHistId.setCellValueFactory(new PropertyValueFactory<>("idHistorico"));
        colHistFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));
        colHistDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        tablaHistorico.setItems(listaHistorico);

        // Al seleccionar historial → mostrar detalle
        tablaHistorico.getSelectionModel().selectedItemProperty().addListener((obs, old, h) -> {
            if (h != null && txtDetalleHistorial != null)
                txtDetalleHistorial.setText(h.obtenerDetalleCambio());
        });

        cmbEstadoActual.getItems().addAll(PENDIENTE, EN_REVISION, APROBADA, RECHAZADA);
        cmbEstadoActual.setValue(PENDIENTE);

        cmbFiltroEstado.getItems().addAll("Todos", PENDIENTE, EN_REVISION, APROBADA, RECHAZADA);
        cmbFiltroEstado.setValue("Todos");

        dpFechaReclamacion.setValue(LocalDate.now());

        // Clic en fila → cargar formulario e historial
        tablaReclamaciones.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) { cargarEnFormulario(sel); cargarHistorial(sel); }
        });

        actualizarTabla();

        // ── Permisos ──────────────────────────────────────────────────────
        Permisos.aplicarBtn(btnRegistrarReclamacion, Permisos.Accion.REGISTRAR);
        Permisos.aplicarBtn(btnAprobar,              Permisos.Accion.EDITAR);
        Permisos.aplicarBtn(btnRechazar,             Permisos.Accion.EDITAR);
        Permisos.aplicarBtn(btnEliminar,             Permisos.Accion.ELIMINAR);

    }

    // ── Buscar compra por ID ──────────────────────────────────────────────────
    @FXML
    private void onBuscarCompra() {
        String idC = txtIdCompra.getText().trim();
        if (idC.isBlank()) { JOptionPane.showMessageDialog(null, "Ingresa un ID de compra."); return; }

        String sql = "SELECT pr.nombre AS nombre_proveedor " +
                "FROM TBL_COMPRA c " +
                "JOIN TBL_PEDIDO_C pc ON pc.id_pedido_c = c.id_pedido_c " +
                "JOIN TBL_PROVEEDOR pr ON pr.id_proveedor = pc.id_proveedor " +
                "WHERE c.id_compra = ?";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(idC));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) txtProveedor.setText(rs.getString("nombre_proveedor"));
            else JOptionPane.showMessageDialog(null, "No se encontró la compra #" + idC);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar: " + e.getMessage());
        }
    }

    // ── Buscar en tabla ───────────────────────────────────────────────────────
    @FXML
    private void fnBuscar() {
        String busqueda = txtBusqueda.getText().trim().toLowerCase();
        String estado   = cmbFiltroEstado.getValue();

        ObservableList<ReclamacionCompra> listaFiltrada = FXCollections.observableArrayList();
        for (ReclamacionCompra r : listaReclamaciones) {
            boolean okEstado = "Todos".equals(estado) || estado == null || r.getEstadoActualNombre().equals(estado);
            boolean okBusq   = busqueda.isEmpty()
                    || String.valueOf(r.getIdReclamacioncompra()).contains(busqueda)
                    || r.getNombreProveedor().toLowerCase().contains(busqueda)
                    || String.valueOf(r.getIdCompra()).contains(busqueda);
            if (okEstado && okBusq) listaFiltrada.add(r);
        }
        tablaReclamaciones.setItems(listaFiltrada);
    }

    // ── Buscar producto por ID ────────────────────────────────────────────────
    @FXML
    private void onBuscarProducto() {
        if (txtIdProducto.getText().isBlank()) return;
        String sql = "SELECT nombre FROM TBL_PRODUCTO WHERE id_producto = ?";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(txtIdProducto.getText().trim()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) txtNombreProducto.setText(rs.getString("nombre"));
            else JOptionPane.showMessageDialog(null, "Producto no encontrado.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al buscar producto: " + e.getMessage());
        }
    }

    // ── Registrar reclamación ─────────────────────────────────────────────────
    @FXML
    private void onRegistrarReclamacion() {
        if (txtIdCompra.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El ID de Compra es obligatorio."); return;
        }
        if (txtIdProducto.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El ID del producto reclamado es obligatorio."); return;
        }
        if (txtCantidadDevolver.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "La cantidad a devolver es obligatoria."); return;
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

            int idReclam = -1;
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) idReclam = keys.getInt(1);

            if (idReclam == -1) {
                JOptionPane.showMessageDialog(null, "Error al obtener el ID generado."); return;
            }

            // Insertar producto reclamado — PK es (id_producto, id_reclamacioncompra)
            String sqlProd = "INSERT INTO TBL_PRODUCTO_RECLAMACION_COMPRA " +
                    "(id_producto, id_reclamacioncompra, cantidad, descripcion) VALUES (?, ?, ?, ?)";
            PreparedStatement psProd = con.prepareStatement(sqlProd);
            psProd.setInt(1,    Integer.parseInt(txtIdProducto.getText().trim()));
            psProd.setInt(2,    idReclam);
            psProd.setInt(3,    Integer.parseInt(txtCantidadDevolver.getText().trim()));
            psProd.setString(4, txtDescripcion.getText().trim());
            psProd.executeUpdate();

            // Guardar descripción en historial
            PreparedStatement psH = con.prepareStatement(
                    "INSERT INTO TBL_HISTORICO_RECLAMACION_COMPRA " +
                            "(descripcion, creado_por, fecha_creacion, id_reclamacioncompra) VALUES (?, ?, ?, ?)");
            psH.setString(1, txtDescripcion.getText().trim());
            psH.setString(2, "Usuario");
            psH.setDate(3,   Date.valueOf(LocalDate.now()));
            psH.setInt(4,    idReclam);
            psH.executeUpdate();

            JOptionPane.showMessageDialog(null, "Reclamación registrada correctamente.");
            actualizarTabla();
            limpiar();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar: " + e.getMessage());
        }
    }

    // ── Aprobar ───────────────────────────────────────────────────────────────
    @FXML
    private void onAprobar() {
        ReclamacionCompra sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una reclamación."); return; }
        cambiarEstado(sel.getIdReclamacioncompra(), APROBADA);
    }

    // ── Rechazar ──────────────────────────────────────────────────────────────
    @FXML
    private void onRechazar() {
        ReclamacionCompra sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una reclamación."); return; }
        cambiarEstado(sel.getIdReclamacioncompra(), RECHAZADA);
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────
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
            PreparedStatement ps1 = con.prepareStatement(
                    "DELETE FROM TBL_HISTORICO_RECLAMACION_COMPRA WHERE id_reclamacioncompra = ?");
            ps1.setInt(1, idReclam);
            ps1.executeUpdate();

            PreparedStatement ps2 = con.prepareStatement(
                    "DELETE FROM TBL_PRODUCTO_RECLAMACION_COMPRA WHERE id_reclamacioncompra = ?");
            ps2.setInt(1, idReclam);
            ps2.executeUpdate();

            PreparedStatement ps3 = con.prepareStatement(
                    "DELETE FROM TBL_RECLAMACION_COMPRA WHERE id_reclamacioncompra = ?");
            ps3.setInt(1, idReclam);
            ps3.executeUpdate();

            listaHistorico.clear();
            if (lblHistorialDe != null) lblHistorialDe.setText("—");
            actualizarTabla();
            limpiar();
            JOptionPane.showMessageDialog(null, "Reclamación eliminada.");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar: " + e.getMessage());
        }
    }

    // ── Ver historial ─────────────────────────────────────────────────────────
    @FXML
    private void onVerHistorial() {
        ReclamacionCompra sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una reclamación."); return; }
        cargarHistorial(sel);
    }

    // ── Agregar nota ──────────────────────────────────────────────────────────
    @FXML
    private void onAgregarNota() {
        ReclamacionCompra sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una reclamación."); return; }

        String nota = txtNuevaNotaHistorial.getText().trim();
        if (nota.isBlank()) { JOptionPane.showMessageDialog(null, "Escribe una nota primero."); return; }

        String sql = "INSERT INTO TBL_HISTORICO_RECLAMACION_COMPRA " +
                "(descripcion, creado_por, fecha_creacion, id_reclamacioncompra) VALUES (?, ?, ?, ?)";

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

    // ── Limpiar ───────────────────────────────────────────────────────────────
    @FXML
    private void onLimpiarFormulario() { limpiar(); }

    @FXML
    public void limpiar() {
        txtIdReclamacion.clear();
        txtIdCompra.clear();
        txtProveedor.clear();
        if (txtIdProducto     != null) txtIdProducto.clear();
        if (txtNombreProducto != null) txtNombreProducto.clear();
        dpFechaReclamacion.setValue(LocalDate.now());
        cmbEstadoActual.setValue(PENDIENTE);
        txtCantidadDevolver.clear();
        txtDescripcion.clear();
        tablaReclamaciones.getSelectionModel().clearSelection();
        tablaReclamaciones.setItems(listaReclamaciones);
    }

    // ── Cargar tabla desde BD ─────────────────────────────────────────────────
    private void actualizarTabla() {
        String sql = "SELECT r.id_reclamacioncompra, r.id_compra, " +
                "pr.nombre AS nombre_proveedor, " +
                "r.fecha_reclamacion, r.estado, " +
                "ISNULL((SELECT TOP 1 cantidad FROM TBL_PRODUCTO_RECLAMACION_COMPRA " +
                "        WHERE id_reclamacioncompra = r.id_reclamacioncompra), 0) AS cantidad, " +
                "ISNULL((SELECT TOP 1 descripcion FROM TBL_HISTORICO_RECLAMACION_COMPRA " +
                "        WHERE id_reclamacioncompra = r.id_reclamacioncompra " +
                "        ORDER BY id_historico_reclam_compra), '') AS descripcion " +
                "FROM TBL_RECLAMACION_COMPRA r " +
                "JOIN TBL_COMPRA   c  ON c.id_compra    = r.id_compra " +
                "JOIN TBL_PEDIDO_C pc ON pc.id_pedido_c = c.id_pedido_c " +
                "JOIN TBL_PROVEEDOR pr ON pr.id_proveedor = pc.id_proveedor";

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
            tablaReclamaciones.setItems(listaReclamaciones);
            actualizarContadores();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar reclamaciones: " + e.getMessage());
        }
    }

    // ── Cargar historial ──────────────────────────────────────────────────────
    private void cargarHistorial(ReclamacionCompra r) {
        listaHistorico.clear();
        if (lblHistorialDe != null) lblHistorialDe.setText("Rec. #" + r.getIdReclamacioncompra());

        String sql = "SELECT id_historico_reclam_compra, descripcion, creado_por, " +
                "fecha_creacion, id_reclamacioncompra " +
                "FROM TBL_HISTORICO_RECLAMACION_COMPRA " +
                "WHERE id_reclamacioncompra = ? ORDER BY id_historico_reclam_compra";

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
            tablaHistorico.setItems(listaHistorico);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar historial: " + e.getMessage());
        }
    }

    // ── Cambiar estado ────────────────────────────────────────────────────────
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

    // ── Cargar fila en formulario ─────────────────────────────────────────────
    private void cargarEnFormulario(ReclamacionCompra r) {
        txtIdReclamacion.setText(String.valueOf(r.getIdReclamacioncompra()));
        txtIdCompra.setText(String.valueOf(r.getIdCompra()));
        txtProveedor.setText(r.getNombreProveedor());
        dpFechaReclamacion.setValue(r.getFechaReclamacion());
        cmbEstadoActual.setValue(r.getEstadoActualNombre());
        txtCantidadDevolver.setText(String.valueOf(r.getCantidadAdevolver()));
        txtDescripcion.setText(r.getDescripcion());
        if (txtIdProducto != null) {
            String sqlP = "SELECT p.id_producto, p.nombre FROM TBL_PRODUCTO_RECLAMACION_COMPRA prc JOIN TBL_PRODUCTO p ON p.id_producto = prc.id_producto WHERE prc.id_reclamacioncompra = ?";
            try (Connection conP = conexion.establecerConexion(); PreparedStatement psP = conP.prepareStatement(sqlP)) {
                psP.setInt(1, r.getIdReclamacioncompra());
                ResultSet rsP = psP.executeQuery();
                if (rsP.next()) { txtIdProducto.setText(String.valueOf(rsP.getInt("id_producto"))); if (txtNombreProducto != null) txtNombreProducto.setText(rsP.getString("nombre")); }
                else { txtIdProducto.clear(); if (txtNombreProducto != null) txtNombreProducto.clear(); }
            } catch (SQLException ex) { txtIdProducto.clear(); if (txtNombreProducto != null) txtNombreProducto.clear(); }
        }
    }

    // ── Pastillas de conteo ───────────────────────────────────────────────────
    private void actualizarContadores() {
        int pend = 0, rev = 0, aprob = 0, rech = 0;
        for (ReclamacionCompra r : listaReclamaciones) {
            switch (r.getEstadoActualNombre()) {
                case PENDIENTE   -> pend++;
                case EN_REVISION -> rev++;
                case APROBADA    -> aprob++;
                case RECHAZADA   -> rech++;
            }
        }
        if (lblContPendiente  != null) lblContPendiente.setText("⏳  " + pend  + "  Pendientes");
        if (lblContRevision   != null) lblContRevision.setText("🔍  "  + rev   + "  En Revisión");
        if (lblContAprobada   != null) lblContAprobada.setText("✔  "   + aprob + "  Aprobadas");
        if (lblContRechazada  != null) lblContRechazada.setText("✖  "  + rech  + "  Rechazadas");
    }
}