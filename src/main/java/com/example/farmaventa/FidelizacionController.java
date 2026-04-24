package com.example.farmaventa;

import Usuarios.Permisos;
import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.SistemaFidelizacion;
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

public class FidelizacionController implements Initializable {

    Conexion conexion = new Conexion();

    // ── Formulario ────────────────────────────────────────────────────────────
    // ── Botones con restricción de permisos ───────────────────────────────
    @FXML private Button btnRegistrarFidelizacion;
    @FXML private Button btnAgregarPuntos;
    @FXML private Button btnCanjearPuntos;
    @FXML private Button btnEliminarFidelizacion;
    @FXML private Button btnRenovarCaducidad;

    @FXML private TextField  txtIdFidelizacion;
    @FXML private TextField  txtIdCliente;
    @FXML private TextField  txtNombreCliente;
    @FXML private TextField  txtPuntos;
    @FXML private DatePicker dpFechaCaducidad;

    // ── Panel canjear ─────────────────────────────────────────────────────────
    @FXML private TextField txtPuntosACanjear;
    @FXML private Label     lblPuntosRestantes;

    // ── Tabla ─────────────────────────────────────────────────────────────────
    @FXML private TableView<SistemaFidelizacion>              tablaFidelizacion;
    @FXML private TableColumn<SistemaFidelizacion, Integer>   colId;
    @FXML private TableColumn<SistemaFidelizacion, Integer>   colCliente;
    @FXML private TableColumn<SistemaFidelizacion, String>    colNombre;
    @FXML private TableColumn<SistemaFidelizacion, Integer>   colPuntos;
    @FXML private TableColumn<SistemaFidelizacion, LocalDate> colCaducidad;
    @FXML private TableColumn<SistemaFidelizacion, String>    colEstado;

    // ── Filtros ───────────────────────────────────────────────────────────────
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TextField        txtBusqueda;

    // ── Historial ─────────────────────────────────────────────────────────────
    @FXML private ListView<String> listHistorial;
    @FXML private Label            lblHistorialDe;

    // ── Pastillas ─────────────────────────────────────────────────────────────
    @FXML private Label lblContActivo;
    @FXML private Label lblContVencido;
    @FXML private Label lblTotalPuntos;

    // ── Lista de datos ────────────────────────────────────────────────────────
    private ObservableList<SistemaFidelizacion> listaFidelizacion = FXCollections.observableArrayList();
    private ObservableList<String>              listaHistorial    = FXCollections.observableArrayList();

    private static final String ACTIVO  = "ACTIVO";
    private static final String VENCIDO = "VENCIDO";

    // ── Inicializar ───────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idFidelizacion"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colPuntos.setCellValueFactory(new PropertyValueFactory<>("puntosAcumulados"));
        colCaducidad.setCellValueFactory(new PropertyValueFactory<>("fechaCaducidad"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoPuntos"));

        tablaFidelizacion.setItems(listaFidelizacion);

        cmbFiltroEstado.getItems().addAll("Todos", ACTIVO, VENCIDO);
        cmbFiltroEstado.setValue("Todos");

        listHistorial.setItems(listaHistorial);

        // Clic en fila → cargar formulario e historial
        tablaFidelizacion.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) { cargarEnFormulario(sel); cargarHistorial(sel); }
        });

        dpFechaCaducidad.setValue(LocalDate.now().plusYears(1));
        actualizarTabla();

        // ── Permisos ──────────────────────────────────────────────────────
        Permisos.aplicarBtn(btnRegistrarFidelizacion, Permisos.Accion.REGISTRAR);
        Permisos.aplicarBtn(btnAgregarPuntos,         Permisos.Accion.EDITAR);
        Permisos.aplicarBtn(btnCanjearPuntos,         Permisos.Accion.EDITAR);
        Permisos.aplicarBtn(btnEliminarFidelizacion,  Permisos.Accion.ELIMINAR);
        Permisos.aplicarBtn(btnRenovarCaducidad,      Permisos.Accion.EDITAR);

    }

    // ── Buscar cliente por ID ─────────────────────────────────────────────────
    @FXML
    public void onBuscarCliente() {
        String idC = txtIdCliente.getText().trim();
        if (idC.isBlank()) { JOptionPane.showMessageDialog(null, "Ingresa un ID de cliente."); return; }

        String sql = "SELECT p.nombre + ' ' + p.apellido AS nombre_cliente " +
                "FROM TBL_CLIENTE c " +
                "JOIN TBL_PERSONA p ON p.id_persona = c.id_persona " +
                "WHERE c.id_cliente = ?";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(idC));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) txtNombreCliente.setText(rs.getString("nombre_cliente"));
            else JOptionPane.showMessageDialog(null, "No se encontro el cliente #" + idC);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar: " + e.getMessage());
        }
    }

    // ── Buscar en tabla ───────────────────────────────────────────────────────
    @FXML
    public void fnBuscar() {
        String busqueda = txtBusqueda.getText().trim().toLowerCase();
        String estado   = cmbFiltroEstado.getValue();

        ObservableList<SistemaFidelizacion> listaFiltrada = FXCollections.observableArrayList();
        for (SistemaFidelizacion f : listaFidelizacion) {
            boolean okEstado = "Todos".equals(estado) || estado == null || f.getEstadoPuntos().equals(estado);
            boolean okBusq   = busqueda.isEmpty()
                    || String.valueOf(f.getIdFidelizacion()).contains(busqueda)
                    || f.getNombreCliente().toLowerCase().contains(busqueda)
                    || String.valueOf(f.getIdCliente()).contains(busqueda);
            if (okEstado && okBusq) listaFiltrada.add(f);
        }
        tablaFidelizacion.setItems(listaFiltrada);
    }

    // ── Registrar nueva fidelización ──────────────────────────────────────────
    @FXML
    public void onRegistrar() {
        if (txtIdCliente.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El ID de Cliente es obligatorio."); return;
        }
        if (txtPuntos.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Los puntos son obligatorios."); return;
        }
        if (dpFechaCaducidad.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona la fecha de caducidad."); return;
        }

        // Verificar si el cliente ya tiene fidelizacion
        String sqlCheck = "SELECT id_fidelizacion FROM TBL_SISTEMA_FIDELIZACION WHERE id_cliente = ?";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {
            psCheck.setInt(1, Integer.parseInt(txtIdCliente.getText().trim()));
            ResultSet rs = psCheck.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(null,
                        "Este cliente ya tiene un registro de fidelizacion.\n" +
                                "Seleccionalo en la tabla y usa Agregar Puntos.");
                return;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al verificar: " + e.getMessage()); return;
        }

        String sql = "INSERT INTO TBL_SISTEMA_FIDELIZACION (id_cliente, puntos_acumulados, fecha_caducidad) " +
                "VALUES (?, ?, ?)";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1,  Integer.parseInt(txtIdCliente.getText().trim()));
            ps.setInt(2,  Integer.parseInt(txtPuntos.getText().trim()));
            ps.setDate(3, Date.valueOf(dpFechaCaducidad.getValue()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Fidelizacion registrada correctamente.");
            actualizarTabla();
            limpiar();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar: " + e.getMessage());
        }
    }

    // ── Agregar puntos al cliente seleccionado ────────────────────────────────
    @FXML
    public void onAgregarPuntos() {
        SistemaFidelizacion sel = tablaFidelizacion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona un cliente."); return; }
        if (txtPuntos.getText().isBlank()) { JOptionPane.showMessageDialog(null, "Ingresa los puntos a agregar."); return; }

        int puntosAAgregar;
        try { puntosAAgregar = Integer.parseInt(txtPuntos.getText().trim()); }
        catch (NumberFormatException e) { JOptionPane.showMessageDialog(null, "Los puntos deben ser un numero."); return; }
        if (puntosAAgregar <= 0) { JOptionPane.showMessageDialog(null, "Los puntos deben ser mayores a 0."); return; }

        String sql = "UPDATE TBL_SISTEMA_FIDELIZACION SET puntos_acumulados = puntos_acumulados + ? " +
                "WHERE id_fidelizacion = ?";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, puntosAAgregar);
            ps.setInt(2, sel.getIdFidelizacion());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Se agregaron " + puntosAAgregar + " puntos correctamente.");
            actualizarTabla();
            txtPuntos.clear();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al agregar puntos: " + e.getMessage());
        }
    }

    // ── Canjear puntos ────────────────────────────────────────────────────────
    @FXML
    public void onCanjearPuntos() {
        SistemaFidelizacion sel = tablaFidelizacion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona un cliente."); return; }

        if (VENCIDO.equals(sel.getEstadoPuntos())) {
            JOptionPane.showMessageDialog(null, "Los puntos de este cliente estan VENCIDOS y no se pueden canjear.");
            return;
        }

        if (txtPuntosACanjear.getText().isBlank()) { JOptionPane.showMessageDialog(null, "Ingresa los puntos a canjear."); return; }

        int puntosACanjear;
        try { puntosACanjear = Integer.parseInt(txtPuntosACanjear.getText().trim()); }
        catch (NumberFormatException e) { JOptionPane.showMessageDialog(null, "Los puntos deben ser un numero."); return; }

        if (puntosACanjear <= 0) { JOptionPane.showMessageDialog(null, "Los puntos a canjear deben ser mayores a 0."); return; }
        if (puntosACanjear > sel.getPuntosAcumulados()) {
            JOptionPane.showMessageDialog(null, "No tiene suficientes puntos.\nDisponibles: " + sel.getPuntosAcumulados());
            return;
        }

        String sql = "UPDATE TBL_SISTEMA_FIDELIZACION SET puntos_acumulados = puntos_acumulados - ? " +
                "WHERE id_fidelizacion = ?";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, puntosACanjear);
            ps.setInt(2, sel.getIdFidelizacion());
            ps.executeUpdate();
            int restantes = sel.getPuntosAcumulados() - puntosACanjear;
            JOptionPane.showMessageDialog(null,
                    "Canje exitoso\nPuntos canjeados: " + puntosACanjear +
                            "\nPuntos restantes: " + restantes);
            actualizarTabla();
            txtPuntosACanjear.clear();
            if (lblPuntosRestantes != null) lblPuntosRestantes.setText("Restantes: -");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al canjear: " + e.getMessage());
        }
    }

    // ── Renovar fecha de caducidad ────────────────────────────────────────────
    @FXML
    public void onRenovarCaducidad() {
        SistemaFidelizacion sel = tablaFidelizacion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona un cliente."); return; }
        if (dpFechaCaducidad.getValue() == null) { JOptionPane.showMessageDialog(null, "Selecciona la nueva fecha de caducidad."); return; }

        String sql = "UPDATE TBL_SISTEMA_FIDELIZACION SET fecha_caducidad = ? WHERE id_fidelizacion = ?";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(dpFechaCaducidad.getValue()));
            ps.setInt(2,  sel.getIdFidelizacion());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Fecha de caducidad actualizada.");
            actualizarTabla();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al renovar: " + e.getMessage());
        }
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────
    @FXML
    public void onEliminar() {
        SistemaFidelizacion sel = tablaFidelizacion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona un registro."); return; }

        int confirm = JOptionPane.showConfirmDialog(null,
                "Eliminar fidelizacion del cliente " + sel.getNombreCliente() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM TBL_SISTEMA_FIDELIZACION WHERE id_fidelizacion = ?";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, sel.getIdFidelizacion());
            ps.executeUpdate();
            listaHistorial.clear();
            if (lblHistorialDe != null) lblHistorialDe.setText("-");
            actualizarTabla();
            limpiar();
            JOptionPane.showMessageDialog(null, "Registro eliminado.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar: " + e.getMessage());
        }
    }

    // ── Limpiar formulario ────────────────────────────────────────────────────
    @FXML
    public void onLimpiar() { limpiar(); }

    @FXML
    public void limpiar() {
        txtIdFidelizacion.clear();
        txtIdCliente.clear();
        txtNombreCliente.clear();
        txtPuntos.clear();
        txtPuntosACanjear.clear();
        dpFechaCaducidad.setValue(LocalDate.now().plusYears(1));
        if (lblPuntosRestantes != null) lblPuntosRestantes.setText("Restantes: -");
        if (txtBusqueda != null) txtBusqueda.clear();
        tablaFidelizacion.getSelectionModel().clearSelection();
        tablaFidelizacion.setItems(listaFidelizacion);
    }

    // ── Listener campo canjear → mostrar restantes ────────────────────────────
    @FXML
    public void onCanjearInput() {
        SistemaFidelizacion sel = tablaFidelizacion.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try {
            int canjear   = Integer.parseInt(txtPuntosACanjear.getText().trim());
            int restantes = sel.getPuntosAcumulados() - canjear;
            if (lblPuntosRestantes != null)
                lblPuntosRestantes.setText("Restantes: " + (restantes >= 0 ? restantes : "Insuficientes") + " pts");
        } catch (NumberFormatException e) {
            if (lblPuntosRestantes != null) lblPuntosRestantes.setText("Restantes: -");
        }
    }

    // ── Cargar tabla desde BD ─────────────────────────────────────────────────
    private void actualizarTabla() {
        listaFidelizacion.clear();
        String sql = "SELECT f.id_fidelizacion, f.id_cliente, " +
                "p.nombre + ' ' + p.apellido AS nombre_cliente, " +
                "f.puntos_acumulados, f.fecha_caducidad " +
                "FROM TBL_SISTEMA_FIDELIZACION f " +
                "JOIN TBL_CLIENTE c ON c.id_cliente = f.id_cliente " +
                "JOIN TBL_PERSONA p ON p.id_persona = c.id_persona";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                listaFidelizacion.add(new SistemaFidelizacion(
                        rs.getInt("id_fidelizacion"),
                        rs.getInt("id_cliente"),
                        rs.getString("nombre_cliente"),
                        rs.getInt("puntos_acumulados"),
                        rs.getDate("fecha_caducidad").toLocalDate()
                ));
            }
            tablaFidelizacion.setItems(listaFidelizacion);
            actualizarContadores();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar: " + e.getMessage());
        }
    }

    // ── Cargar historial del cliente seleccionado ─────────────────────────────
    private void cargarHistorial(SistemaFidelizacion f) {
        listaHistorial.clear();
        if (lblHistorialDe != null) lblHistorialDe.setText(f.getNombreCliente());

        listaHistorial.add("ID Fidelizacion   : " + f.getIdFidelizacion());
        listaHistorial.add("ID Cliente        : " + f.getIdCliente());
        listaHistorial.add("Puntos acumulados : " + f.getPuntosAcumulados());
        listaHistorial.add("Fecha caducidad   : " + f.getFechaCaducidad());
        listaHistorial.add("Estado            : " + f.getEstadoPuntos());
        listaHistorial.add("------------------------------");

        double descuento = f.getPuntosAcumulados() * 0.50;
        listaHistorial.add("Valor en descuento: RD$ " + String.format("%.2f", descuento));

        String nivel;
        if      (f.getPuntosAcumulados() >= 1000) nivel = "ORO";
        else if (f.getPuntosAcumulados() >= 500)  nivel = "PLATA";
        else if (f.getPuntosAcumulados() >= 100)  nivel = "BRONCE";
        else                                       nivel = "BASICO";
        listaHistorial.add("Nivel del cliente : " + nivel);

        if (lblPuntosRestantes != null)
            lblPuntosRestantes.setText("Disponibles: " + f.getPuntosAcumulados() + " pts");
    }

    // ── Cargar fila en formulario ─────────────────────────────────────────────
    private void cargarEnFormulario(SistemaFidelizacion f) {
        txtIdFidelizacion.setText(String.valueOf(f.getIdFidelizacion()));
        txtIdCliente.setText(String.valueOf(f.getIdCliente()));
        txtNombreCliente.setText(f.getNombreCliente());
        txtPuntos.clear();
        dpFechaCaducidad.setValue(f.getFechaCaducidad());
    }

    // ── Pastillas de conteo ───────────────────────────────────────────────────
    private void actualizarContadores() {
        int activos = 0, vencidos = 0, totalPts = 0;
        for (SistemaFidelizacion f : listaFidelizacion) {
            if (ACTIVO.equals(f.getEstadoPuntos()))  activos++;
            if (VENCIDO.equals(f.getEstadoPuntos())) vencidos++;
            totalPts += f.getPuntosAcumulados();
        }
        if (lblContActivo  != null) lblContActivo.setText(activos  + "  Activos");
        if (lblContVencido != null) lblContVencido.setText(vencidos + "  Vencidos");
        if (lblTotalPuntos != null) lblTotalPuntos.setText(totalPts + "  Puntos totales");
    }
}