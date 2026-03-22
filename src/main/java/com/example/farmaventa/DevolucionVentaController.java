package com.example.farmaventa;

import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.DevolucionVenta;
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

public class DevolucionVentaController implements Initializable {

    Conexion conexion = new Conexion();

    // ── Formulario ───────────────────────────────────────────────────────────
    @FXML private TextField        txtIdDevolucion;
    @FXML private TextField        txtIdVenta;
    @FXML private TextField        txtCliente;
    @FXML private TextField        txtMontoVenta;
    @FXML private DatePicker       dpFechaDevolucion;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private TextArea         txtRazon;

    // ── Tabla devoluciones ───────────────────────────────────────────────────
    @FXML private TableView<DevolucionVenta>              tablaDevolucion;
    @FXML private TableColumn<DevolucionVenta, Integer>   colId;
    @FXML private TableColumn<DevolucionVenta, Integer>   colVenta;
    @FXML private TableColumn<DevolucionVenta, String>    colCliente;
    @FXML private TableColumn<DevolucionVenta, LocalDate> colFecha;
    @FXML private TableColumn<DevolucionVenta, String>    colEstado;
    @FXML private TableColumn<DevolucionVenta, String>    colMonto;
    @FXML private TableColumn<DevolucionVenta, String>    colRazon;

    // ── Filtros ──────────────────────────────────────────────────────────────
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TextField        txtBusqueda;

    // ── Panel Nota de Crédito ────────────────────────────────────────────────
    @FXML private Label   lblNotaDe;
    @FXML private TextArea txtRazonNota;
    @FXML private ListView<String> listNotas;

    // ── Pastillas de conteo ──────────────────────────────────────────────────
    @FXML private Label lblContPendiente;
    @FXML private Label lblContCompletada;
    @FXML private Label lblContAnulada;

    // ── Listas ───────────────────────────────────────────────────────────────
    private final ObservableList<DevolucionVenta> listaDevoluciones = FXCollections.observableArrayList();
    private final ObservableList<String>          listaNotas        = FXCollections.observableArrayList();
    private FilteredList<DevolucionVenta>         listaFiltrada;

    private static final String PENDIENTE  = "PENDIENTE";
    private static final String COMPLETADA = "COMPLETADA";
    private static final String ANULADA    = "ANULADA";

    // ── Inicialización ───────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        configurarCombos();
        configurarFiltros();
        configurarListaNotas();

        tablaDevolucion.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) { cargarEnFormulario(sel); cargarNotas(sel); }
        });

        dpFechaDevolucion.setValue(LocalDate.now());
        actualizarTabla();
    }

    // ── Configuraciones internas ──────────────────────────────────────────────
    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idDevolucionventa"));
        colVenta.setCellValueFactory(new PropertyValueFactory<>("idVenta"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaDevolucion"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoNombre"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("montoTotal"));
        colRazon.setCellValueFactory(new PropertyValueFactory<>("razon"));

        // Colores por estado en la columna Estado
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
        ObservableList<String> estados = FXCollections.observableArrayList(PENDIENTE, COMPLETADA, ANULADA);
        cmbEstado.setItems(estados);
        cmbEstado.setValue(PENDIENTE);

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
        listaFiltrada.setPredicate(d -> {
            boolean okEstado = "Todos".equals(estado) || estado == null || d.getEstadoNombre().equals(estado);
            boolean okBusq   = busq.isEmpty()
                    || String.valueOf(d.getIdDevolucionventa()).contains(busq)
                    || d.getNombreCliente().toLowerCase().contains(busq)
                    || String.valueOf(d.getIdVenta()).contains(busq);
            return okEstado && okBusq;
        });
    }

    private void configurarListaNotas() {
        listNotas.setItems(listaNotas);
    }

    // ── Botón "🔎 Buscar" ─────────────────────────────────────────────────────
    @FXML
    private void onBuscarVenta() {
        String idV = txtIdVenta.getText().trim();
        if (idV.isBlank()) { JOptionPane.showMessageDialog(null, "Ingresa un ID de venta."); return; }

        String sql = "SELECT p.nombre + ' ' + p.apellido AS nombre_cliente, v.monto_total "
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
                txtMontoVenta.setText(String.format("%.2f", rs.getDouble("monto_total")));
            } else {
                JOptionPane.showMessageDialog(null, "No se encontró la venta #" + idV);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar: " + e.getMessage());
        }
    }

    // ── Botón "📋 Registrar" ──────────────────────────────────────────────────
    @FXML
    private void onRegistrarDevolucion() {
        if (txtIdVenta.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El ID de Venta es obligatorio."); return;
        }
        if (dpFechaDevolucion.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona la fecha."); return;
        }
        if (txtRazon.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "La razón es obligatoria."); return;
        }

        // estado_devolucion: 0 = Pendiente al crear
        String sql = "INSERT INTO TBL_DEVOLUCION_VENTA (razon, fecha_devolucion, estado_devolucion, id_venta) "
                + "VALUES (?, ?, 0, ?)";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, txtRazon.getText().trim());
            ps.setDate(2,   Date.valueOf(dpFechaDevolucion.getValue()));
            ps.setInt(3,    Integer.parseInt(txtIdVenta.getText().trim()));
            ps.executeUpdate();

            // Crear nota de crédito automáticamente si hay monto
            int idDev = -1;
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) idDev = keys.getInt(1);

            if (idDev != -1 && !txtMontoVenta.getText().isBlank()) {
                PreparedStatement psNota = con.prepareStatement(
                        "INSERT INTO TBL_NOTA_CREDITO (razon, id_devolucionventa) VALUES (?, ?)");
                psNota.setString(1, "Nota de crédito generada automáticamente por devolución. Monto venta: " + txtMontoVenta.getText());
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

    // ── Botón "✔ Completar" ───────────────────────────────────────────────────
    @FXML
    private void onCompletar() {
        DevolucionVenta sel = tablaDevolucion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una devolución."); return; }
        cambiarEstado(sel.getIdDevolucionventa(), 1, COMPLETADA);
    }

    // ── Botón "✖ Anular" ──────────────────────────────────────────────────────
    @FXML
    private void onAnular() {
        DevolucionVenta sel = tablaDevolucion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una devolución."); return; }
        cambiarEstado(sel.getIdDevolucionventa(), 0, ANULADA);
    }

    // ── Botón "🗑 Eliminar" ───────────────────────────────────────────────────
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
            // Borrar notas de crédito hijas primero
            PreparedStatement psNota = con.prepareStatement(
                    "DELETE FROM TBL_NOTA_CREDITO WHERE id_devolucionventa = ?");
            psNota.setInt(1, idDev);
            psNota.executeUpdate();

            // Borrar la devolución
            PreparedStatement psDev = con.prepareStatement(
                    "DELETE FROM TBL_DEVOLUCION_VENTA WHERE id_devolucionventa = ?");
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

    // ── Botón "📄 Ver Notas de Crédito" ──────────────────────────────────────
    @FXML
    private void onVerNotas() {
        DevolucionVenta sel = tablaDevolucion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona una devolución."); return; }
        cargarNotas(sel);
    }

    // ── Botón "➕ Agregar Nota de Crédito" ─────────────────────────────────────
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

    // ── Botón "✖ Limpiar" ────────────────────────────────────────────────────
    @FXML
    private void onLimpiarFormulario() { limpiar(); }

    // ── Limpiar formulario ────────────────────────────────────────────────────
    @FXML
    public void limpiar() {
        txtIdDevolucion.clear();
        txtIdVenta.clear();
        txtCliente.clear();
        txtMontoVenta.clear();
        dpFechaDevolucion.setValue(LocalDate.now());
        cmbEstado.setValue(PENDIENTE);
        txtRazon.clear();
        tablaDevolucion.getSelectionModel().clearSelection();
    }

    // ── Cargar tabla desde BD ─────────────────────────────────────────────────
    private void actualizarTabla() {
        String sql = "SELECT d.id_devolucionventa, d.id_venta, "
                + "p.nombre + ' ' + p.apellido AS nombre_cliente, "
                + "d.fecha_devolucion, d.razon, d.estado_devolucion, "
                + "v.monto_total "
                + "FROM TBL_DEVOLUCION_VENTA d "
                + "JOIN TBL_VENTA   v ON v.id_venta   = d.id_venta "
                + "JOIN TBL_CLIENTE c ON c.id_cliente = v.id_cliente "
                + "JOIN TBL_PERSONA p ON p.id_persona = c.id_persona";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            listaDevoluciones.clear();
            while (rs.next()) {
                boolean bit        = rs.getBoolean("estado_devolucion");
                String  estadoText = bit ? COMPLETADA : PENDIENTE;
                listaDevoluciones.add(new DevolucionVenta(
                        rs.getInt("id_devolucionventa"),
                        rs.getInt("id_venta"),
                        rs.getString("nombre_cliente"),
                        rs.getDate("fecha_devolucion").toLocalDate(),
                        rs.getString("razon"),
                        estadoText,
                        String.format("%.2f", rs.getDouble("monto_total"))
                ));
            }
            actualizarContadores();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar devoluciones: " + e.getMessage());
        }
    }

    // ── Cargar notas de crédito de una devolución ─────────────────────────────
    private void cargarNotas(DevolucionVenta d) {
        listaNotas.clear();
        lblNotaDe.setText("Dev. #" + d.getIdDevolucionventa());

        String sql = "SELECT id_notacredito, razon FROM TBL_NOTA_CREDITO "
                + "WHERE id_devolucionventa = ? ORDER BY id_notacredito";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, d.getIdDevolucionventa());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listaNotas.add("#" + rs.getInt("id_notacredito") + "  —  " + rs.getString("razon"));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar notas: " + e.getMessage());
        }
    }

    // ── Cambiar estado BIT en BD ──────────────────────────────────────────────
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

    // ── Cargar fila seleccionada en el formulario ─────────────────────────────
    private void cargarEnFormulario(DevolucionVenta d) {
        txtIdDevolucion.setText(String.valueOf(d.getIdDevolucionventa()));
        txtIdVenta.setText(String.valueOf(d.getIdVenta()));
        txtCliente.setText(d.getNombreCliente());
        txtMontoVenta.setText(d.getMontoTotal());
        dpFechaDevolucion.setValue(d.getFechaDevolucion());
        cmbEstado.setValue(d.getEstadoNombre());
        txtRazon.setText(d.getRazon());
    }

    // ── Actualizar pastillas de conteo ────────────────────────────────────────
    private void actualizarContadores() {
        long pend  = listaDevoluciones.stream().filter(d -> PENDIENTE.equals(d.getEstadoNombre())).count();
        long comp  = listaDevoluciones.stream().filter(d -> COMPLETADA.equals(d.getEstadoNombre())).count();
        long anul  = listaDevoluciones.stream().filter(d -> ANULADA.equals(d.getEstadoNombre())).count();
        lblContPendiente.setText("⏳  " + pend + "  Pendientes");
        lblContCompletada.setText("✔  "  + comp + "  Completadas");
        lblContAnulada.setText("✖  "    + anul + "  Anuladas");
    }
}