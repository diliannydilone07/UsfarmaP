package com.example.farmaventa;

import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.HistoricoReclamacion;
import com.example.farmaventa.modelo.ReclamacionVenta;
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

public class ReclamacionVentaController implements Initializable {

    Conexion conexion = new Conexion();

    @FXML private TextField        txtIdReclamacion;
    @FXML private TextField        txtIdVenta;
    @FXML private TextField        txtCliente;
    @FXML private DatePicker       dpFechaReclamacion;
    @FXML private ComboBox<String> cmbEstadoActual;
    @FXML private TextField        txtCantidadDevolver;
    @FXML private TextField        txtDescripcion;
    @FXML private TextField        txtIdProducto;
    @FXML private TextField        txtNombreProducto;

    @FXML private TableView<ReclamacionVenta>              tablaReclamaciones;
    @FXML private TableColumn<ReclamacionVenta, Integer>   colId;
    @FXML private TableColumn<ReclamacionVenta, String>    colVenta;
    @FXML private TableColumn<ReclamacionVenta, String>    colProducto;
    @FXML private TableColumn<ReclamacionVenta, String>    colCliente;
    @FXML private TableColumn<ReclamacionVenta, LocalDate> colFecha;
    @FXML private TableColumn<ReclamacionVenta, String>    colEstado;
    @FXML private TableColumn<ReclamacionVenta, Integer>   colCantidad;
    @FXML private TableColumn<ReclamacionVenta, String>    colDescripcion;

    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TextField        txtBusqueda;

    @FXML private TableView<HistoricoReclamacion>              tablaHistorico;
    @FXML private TableColumn<HistoricoReclamacion, Integer>   colHistId;
    @FXML private TableColumn<HistoricoReclamacion, LocalDate> colHistFecha;
    @FXML private TableColumn<HistoricoReclamacion, String>    colHistDescripcion;
    @FXML private TextArea txtDetalleHistorial;
    @FXML private TextArea txtNuevaNotaHistorial;
    @FXML private Label    lblHistorialDe;

    @FXML private Label lblContPendiente;
    @FXML private Label lblContRevision;
    @FXML private Label lblContAprobada;
    @FXML private Label lblContRechazada;

    private ObservableList<ReclamacionVenta>     listaReclamaciones = FXCollections.observableArrayList();
    private ObservableList<HistoricoReclamacion> listaHistorico     = FXCollections.observableArrayList();

    private static final String PENDIENTE   = "Pendiente";
    private static final String EN_REVISION = "En Revisión";
    private static final String APROBADA    = "Aprobada";
    private static final String RECHAZADA   = "Rechazada";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idReclamacionventa"));
        colVenta.setCellValueFactory(new PropertyValueFactory<>("idVenta"));
        colProducto.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaReclamacion"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoActualNombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidadAdevolver"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        tablaReclamaciones.setItems(listaReclamaciones);

        colHistId.setCellValueFactory(new PropertyValueFactory<>("idHistorico"));
        colHistFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));
        colHistDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        tablaHistorico.setItems(listaHistorico);

        tablaHistorico.getSelectionModel().selectedItemProperty().addListener((obs, old, h) -> {
            if (h != null) txtDetalleHistorial.setText(h.obtenerDetalleCambio());
        });

        cmbEstadoActual.getItems().addAll(PENDIENTE, EN_REVISION, APROBADA, RECHAZADA);
        cmbEstadoActual.setValue(PENDIENTE);
        cmbFiltroEstado.getItems().addAll("Todos", PENDIENTE, EN_REVISION, APROBADA, RECHAZADA);
        cmbFiltroEstado.setValue("Todos");
        dpFechaReclamacion.setValue(LocalDate.now());

        tablaReclamaciones.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) { cargarEnFormulario(sel); cargarHistorial(sel); }
        });

        actualizarTabla();
    }

    @FXML public void onBuscarVenta() {
        if (txtIdVenta.getText().isBlank()) return;
        String sql = "SELECT p.nombre + ' ' + p.apellido AS cliente FROM TBL_VENTA v " +
                "JOIN TBL_CLIENTE c ON c.id_cliente = v.id_cliente " +
                "JOIN TBL_PERSONA p ON p.id_persona = c.id_persona WHERE v.id_venta = ?";
        try (Connection con = conexion.establecerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(txtIdVenta.getText().trim()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) txtCliente.setText(rs.getString("cliente"));
            else JOptionPane.showMessageDialog(null, "Venta no encontrada.");
        } catch (Exception e) { JOptionPane.showMessageDialog(null, "Error: " + e.getMessage()); }
    }

    @FXML public void onBuscarProducto() {
        if (txtIdProducto.getText().isBlank()) return;
        String sql = "SELECT nombre FROM TBL_PRODUCTO WHERE id_producto = ?";
        try (Connection con = conexion.establecerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(txtIdProducto.getText().trim()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) txtNombreProducto.setText(rs.getString("nombre"));
            else JOptionPane.showMessageDialog(null, "Producto no encontrado.");
        } catch (Exception e) { JOptionPane.showMessageDialog(null, "Error: " + e.getMessage()); }
    }

    @FXML public void fnBuscar() {
        String busqueda = txtBusqueda.getText().trim().toLowerCase();
        String estado   = cmbFiltroEstado.getValue();
        ObservableList<ReclamacionVenta> listaFiltrada = FXCollections.observableArrayList();
        for (ReclamacionVenta r : listaReclamaciones) {
            boolean okEstado = "Todos".equals(estado) || estado == null || r.getEstadoActualNombre().equals(estado);
            boolean okBusq   = busqueda.isEmpty()
                    || String.valueOf(r.getIdReclamacionventa()).contains(busqueda)
                    || r.getNombreCliente().toLowerCase().contains(busqueda)
                    || r.getDescripcion().toLowerCase().contains(busqueda);
            if (okEstado && okBusq) listaFiltrada.add(r);
        }
        tablaReclamaciones.setItems(listaFiltrada);
    }

    @FXML public void onRegistrarReclamacion() {
        if (txtIdVenta.getText().isBlank()) { JOptionPane.showMessageDialog(null, "El ID de venta es obligatorio."); return; }
        if (txtIdProducto.getText().isBlank()) { JOptionPane.showMessageDialog(null, "El ID del producto es obligatorio."); return; }
        if (txtCantidadDevolver.getText().isBlank()) { JOptionPane.showMessageDialog(null, "La cantidad es obligatoria."); return; }
        if (txtDescripcion.getText().isBlank()) { JOptionPane.showMessageDialog(null, "La descripción es obligatoria."); return; }

        String sql = "INSERT INTO TBL_RECLAMACION_VENTA (id_venta, fecha_reclamacion, estado) VALUES (?, ?, ?)";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, Integer.parseInt(txtIdVenta.getText().trim()));
            ps.setDate(2, Date.valueOf(dpFechaReclamacion.getValue()));
            ps.setInt(3, APROBADA.equals(cmbEstadoActual.getValue()) ? 1 : 0);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            int idNuevo = -1;
            if (keys.next()) idNuevo = keys.getInt(1);
            if (idNuevo == -1) { JOptionPane.showMessageDialog(null, "Error al obtener ID generado."); return; }

            PreparedStatement psProd = con.prepareStatement(
                    "INSERT INTO TBL_PRODUCTO_RECLAMACION_VENTA (id_producto, id_reclamacionventa, cantidad, descripcion) VALUES (?, ?, ?, ?)");
            psProd.setInt(1, Integer.parseInt(txtIdProducto.getText().trim()));
            psProd.setInt(2, idNuevo);
            psProd.setInt(3, Integer.parseInt(txtCantidadDevolver.getText().trim()));
            psProd.setString(4, txtDescripcion.getText().trim());
            psProd.executeUpdate();

            insertarHistorial(con, idNuevo, "Estado: " + cmbEstadoActual.getValue() + " - " + txtDescripcion.getText().trim());
            JOptionPane.showMessageDialog(null, "Reclamacion registrada correctamente.");
            Limpiar(); actualizarTabla();
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, "Error al guardar: " + e.getMessage()); }
    }

    @FXML public void onAprobar() {
        if (txtIdReclamacion.getText().isBlank()) { JOptionPane.showMessageDialog(null, "Selecciona una reclamacion primero."); return; }
        cmbEstadoActual.setValue(APROBADA);
        onActualizarEstado();
    }

    @FXML public void onRechazar() {
        if (txtIdReclamacion.getText().isBlank()) { JOptionPane.showMessageDialog(null, "Selecciona una reclamacion primero."); return; }
        cmbEstadoActual.setValue(RECHAZADA);
        onActualizarEstado();
    }

    @FXML public void onActualizarEstado() {
        if (txtIdReclamacion.getText().isBlank()) { JOptionPane.showMessageDialog(null, "Selecciona una reclamacion primero."); return; }
        int idReclam = Integer.parseInt(txtIdReclamacion.getText().trim());
        String nuevoEstado = cmbEstadoActual.getValue();
        String nota = txtNuevaNotaHistorial != null ? txtNuevaNotaHistorial.getText().trim() : "";
        String sql = "UPDATE TBL_RECLAMACION_VENTA SET estado=? WHERE id_reclamacionventa=?";
        try (Connection con = conexion.establecerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, APROBADA.equals(nuevoEstado) ? 1 : 0);
            ps.setInt(2, idReclam);
            ps.executeUpdate();
            insertarHistorial(con, idReclam, "Estado: " + nuevoEstado + (nota.isEmpty() ? "" : " - " + nota));
            JOptionPane.showMessageDialog(null, "Estado actualizado a: " + nuevoEstado);
            actualizarTabla();
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, "Error: " + e.getMessage()); }
    }

    @FXML public void onVerHistorial() {
        ReclamacionVenta sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una reclamacion de la tabla."); return; }
        cargarHistorial(sel);
    }

    @FXML public void onEliminar() {
        if (txtIdReclamacion.getText().isBlank()) { JOptionPane.showMessageDialog(null, "Selecciona una reclamacion primero."); return; }
        int confirm = JOptionPane.showConfirmDialog(null, "Eliminar esta reclamacion?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        int idReclam = Integer.parseInt(txtIdReclamacion.getText().trim());
        try (Connection con = conexion.establecerConexion()) {
            PreparedStatement ps1 = con.prepareStatement("DELETE FROM TBL_HISTORICO_RECLAMACION_VENTA WHERE id_reclamacionventa=?");
            ps1.setInt(1, idReclam); ps1.executeUpdate();
            PreparedStatement ps2 = con.prepareStatement("DELETE FROM TBL_PRODUCTO_RECLAMACION_VENTA WHERE id_reclamacionventa=?");
            ps2.setInt(1, idReclam); ps2.executeUpdate();
            PreparedStatement ps3 = con.prepareStatement("DELETE FROM TBL_RECLAMACION_VENTA WHERE id_reclamacionventa=?");
            ps3.setInt(1, idReclam); ps3.executeUpdate();
            JOptionPane.showMessageDialog(null, "Reclamacion eliminada.");
            Limpiar(); actualizarTabla();
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, "Error: " + e.getMessage()); }
    }

    @FXML public void onAgregarNota() {
        if (txtIdReclamacion.getText().isBlank()) { JOptionPane.showMessageDialog(null, "Selecciona una reclamacion primero."); return; }
        if (txtNuevaNotaHistorial == null || txtNuevaNotaHistorial.getText().isBlank()) { JOptionPane.showMessageDialog(null, "Escribe una nota primero."); return; }
        int idReclam = Integer.parseInt(txtIdReclamacion.getText().trim());
        try (Connection con = conexion.establecerConexion()) {
            insertarHistorial(con, idReclam, txtNuevaNotaHistorial.getText().trim());
            JOptionPane.showMessageDialog(null, "Nota agregada correctamente.");
            txtNuevaNotaHistorial.clear();
            ReclamacionVenta sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
            if (sel != null) cargarHistorial(sel);
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, "Error: " + e.getMessage()); }
    }

    @FXML public void onLimpiarFormulario() { Limpiar(); }

    @FXML public void Limpiar() {
        txtIdReclamacion.clear(); txtIdVenta.clear(); txtCliente.clear();
        txtCantidadDevolver.clear(); txtDescripcion.clear(); txtIdProducto.clear();
        if (txtNombreProducto    != null) txtNombreProducto.clear();
        if (txtBusqueda          != null) txtBusqueda.clear();
        if (txtNuevaNotaHistorial != null) txtNuevaNotaHistorial.clear();
        if (txtDetalleHistorial  != null) txtDetalleHistorial.clear();
        cmbEstadoActual.setValue(PENDIENTE);
        cmbFiltroEstado.setValue("Todos");
        dpFechaReclamacion.setValue(LocalDate.now());
        listaHistorico.clear();
        tablaReclamaciones.getSelectionModel().clearSelection();
        tablaReclamaciones.setItems(listaReclamaciones);
    }

    private void actualizarTabla() {
        String sql =
                "SELECT r.id_reclamacionventa, r.id_venta, r.fecha_reclamacion, r.estado, " +
                        "p.nombre + ' ' + p.apellido AS nombre_cliente, " +
                        "ISNULL((SELECT TOP 1 pr.nombre FROM TBL_PRODUCTO_RECLAMACION_VENTA prv " +
                        "        JOIN TBL_PRODUCTO pr ON pr.id_producto = prv.id_producto " +
                        "        WHERE prv.id_reclamacionventa = r.id_reclamacionventa), '') AS nombre_producto, " +
                        "ISNULL((SELECT TOP 1 cantidad FROM TBL_PRODUCTO_RECLAMACION_VENTA " +
                        "        WHERE id_reclamacionventa = r.id_reclamacionventa), 0) AS cantidad, " +
                        "ISNULL((SELECT TOP 1 descripcion FROM TBL_PRODUCTO_RECLAMACION_VENTA " +
                        "        WHERE id_reclamacionventa = r.id_reclamacionventa), " +
                        "        ISNULL((SELECT TOP 1 descripcion FROM TBL_HISTORICO_RECLAMACION_VENTA " +
                        "                WHERE id_reclamacionventa = r.id_reclamacionventa " +
                        "                ORDER BY id_historico_reclam_venta), '')) AS descripcion, " +
                        "ISNULL((SELECT TOP 1 descripcion FROM TBL_HISTORICO_RECLAMACION_VENTA " +
                        "        WHERE id_reclamacionventa = r.id_reclamacionventa " +
                        "          AND descripcion LIKE 'Estado:%' " +
                        "        ORDER BY id_historico_reclam_venta DESC), '') AS ultimo_estado " +
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
                String ult = rs.getString("ultimo_estado");
                String estadoFinal;
                if (ult != null && ult.startsWith("Estado:")) {
                    String parte = ult.substring(7).trim();
                    int dash = parte.indexOf(" - ");
                    estadoFinal = dash >= 0 ? parte.substring(0, dash).trim() : parte.trim();
                    if (!cmbEstadoActual.getItems().contains(estadoFinal))
                        estadoFinal = rs.getInt("estado") == 1 ? APROBADA : PENDIENTE;
                } else {
                    estadoFinal = rs.getInt("estado") == 1 ? APROBADA : PENDIENTE;
                }
                String desc = rs.getString("descripcion");
                if (desc != null && desc.startsWith("Estado:")) {
                    int dash = desc.indexOf(" - ");
                    desc = dash >= 0 ? desc.substring(dash + 3).trim() : "";
                }
                ReclamacionVenta rv = new ReclamacionVenta(
                        rs.getInt("id_reclamacionventa"), rs.getInt("id_venta"),
                        rs.getString("nombre_cliente"),
                        rs.getDate("fecha_reclamacion").toLocalDate(),
                        estadoFinal, rs.getInt("cantidad"), desc != null ? desc : "");
                rv.setNombreProducto(rs.getString("nombre_producto"));
                listaReclamaciones.add(rv);
            }
            tablaReclamaciones.setItems(listaReclamaciones);
            actualizarContadores();
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, "Error al cargar: " + e.getMessage()); }
    }

    private void cargarHistorial(ReclamacionVenta r) {
        listaHistorico.clear();
        if (lblHistorialDe != null) lblHistorialDe.setText("Rec. #" + r.getIdReclamacionventa());
        String sql = "SELECT * FROM TBL_HISTORICO_RECLAMACION_VENTA WHERE id_reclamacionventa = ? ORDER BY id_historico_reclam_venta DESC";
        try (Connection con = conexion.establecerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, r.getIdReclamacionventa());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listaHistorico.add(new HistoricoReclamacion(
                        rs.getInt("id_historico_reclam_venta"), rs.getString("descripcion"),
                        rs.getString("creado_por"), rs.getDate("fecha_creacion").toLocalDate(),
                        rs.getInt("id_reclamacionventa")));
            }
            tablaHistorico.setItems(listaHistorico);
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, "Error historial: " + e.getMessage()); }
    }

    private void cargarEnFormulario(ReclamacionVenta r) {
        txtIdReclamacion.setText(String.valueOf(r.getIdReclamacionventa()));
        txtIdVenta.setText(String.valueOf(r.getIdVenta()));
        txtCliente.setText(r.getNombreCliente());
        dpFechaReclamacion.setValue(r.getFechaReclamacion());
        cmbEstadoActual.setValue(cmbEstadoActual.getItems().contains(r.getEstadoActualNombre()) ? r.getEstadoActualNombre() : PENDIENTE);
        txtCantidadDevolver.setText(String.valueOf(r.getCantidadAdevolver()));
        txtDescripcion.setText(r.getDescripcion() != null ? r.getDescripcion() : "");
        if (txtNombreProducto != null) txtNombreProducto.setText(r.getNombreProducto() != null ? r.getNombreProducto() : "");
        String sql = "SELECT id_producto FROM TBL_PRODUCTO_RECLAMACION_VENTA WHERE id_reclamacionventa=?";
        try (Connection con = conexion.establecerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, r.getIdReclamacionventa());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) txtIdProducto.setText(String.valueOf(rs.getInt("id_producto")));
            else txtIdProducto.clear();
        } catch (SQLException e) { txtIdProducto.clear(); }
    }

    private void insertarHistorial(Connection con, int idReclam, String texto) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
                "INSERT INTO TBL_HISTORICO_RECLAMACION_VENTA (descripcion, creado_por, fecha_creacion, id_reclamacionventa) VALUES (?,?,?,?)");
        ps.setString(1, texto); ps.setString(2, "Usuario");
        ps.setDate(3, Date.valueOf(LocalDate.now())); ps.setInt(4, idReclam);
        ps.executeUpdate();
    }

    private void actualizarContadores() {
        int pend = 0, rev = 0, aprob = 0, rech = 0;
        for (ReclamacionVenta r : listaReclamaciones) {
            switch (r.getEstadoActualNombre()) {
                case PENDIENTE   -> pend++;
                case EN_REVISION -> rev++;
                case APROBADA    -> aprob++;
                case RECHAZADA   -> rech++;
            }
        }
        if (lblContPendiente != null) lblContPendiente.setText(pend  + " Pendientes");
        if (lblContRevision  != null) lblContRevision.setText(rev    + " En Revision");
        if (lblContAprobada  != null) lblContAprobada.setText(aprob  + " Aprobadas");
        if (lblContRechazada != null) lblContRechazada.setText(rech  + " Rechazadas");
    }
}