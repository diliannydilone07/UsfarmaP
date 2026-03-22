package com.example.farmaventa;

import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.SistemaFidelizacion;
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

public class FidelizacionController implements Initializable {

    Conexion conexion = new Conexion();

    // ── Formulario ────────────────────────────────────────────────────────────
    @FXML private TextField  txtIdFidelizacion;
    @FXML private TextField  txtIdCliente;
    @FXML private TextField  txtNombreCliente;
    @FXML private TextField  txtPuntos;
    @FXML private DatePicker dpFechaCaducidad;

    // ── Panel canjear ─────────────────────────────────────────────────────────
    @FXML private TextField  txtPuntosACanjear;
    @FXML private Label      lblPuntosRestantes;

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

    // ── Listas ────────────────────────────────────────────────────────────────
    private final ObservableList<SistemaFidelizacion> listaFidelizacion = FXCollections.observableArrayList();
    private final ObservableList<String>              listaHistorial    = FXCollections.observableArrayList();
    private FilteredList<SistemaFidelizacion>         listaFiltrada;

    private static final String ACTIVO  = "ACTIVO";
    private static final String VENCIDO = "VENCIDO";

    // ── Inicialización ────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        configurarCombos();
        configurarFiltros();

        listHistorial.setItems(listaHistorial);

        tablaFidelizacion.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) { cargarEnFormulario(sel); cargarHistorial(sel); }
        });

        dpFechaCaducidad.setValue(LocalDate.now().plusYears(1));
        actualizarTabla();
    }

    // ── Configuraciones internas ──────────────────────────────────────────────
    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idFidelizacion"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colPuntos.setCellValueFactory(new PropertyValueFactory<>("puntosAcumulados"));
        colCaducidad.setCellValueFactory(new PropertyValueFactory<>("fechaCaducidad"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoPuntos"));

        // Colores por estado
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) { setText(null); setStyle(""); return; }
                setText(estado);
                switch (estado) {
                    case ACTIVO  -> setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                    case VENCIDO -> setStyle("-fx-text-fill: #C62828; -fx-font-weight: bold;");
                    default      -> setStyle("");
                }
            }
        });

        // Color en columna puntos (verde si alto, naranja si bajo)
        colPuntos.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer puntos, boolean empty) {
                super.updateItem(puntos, empty);
                if (empty || puntos == null) { setText(null); setStyle(""); return; }
                setText(String.valueOf(puntos));
                if (puntos >= 500)       setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                else if (puntos >= 100)  setStyle("-fx-text-fill: #F57F17; -fx-font-weight: bold;");
                else                     setStyle("-fx-text-fill: #C62828; -fx-font-weight: bold;");
            }
        });

        listaFiltrada = new FilteredList<>(listaFidelizacion, p -> true);
        tablaFidelizacion.setItems(listaFiltrada);
    }

    private void configurarCombos() {
        cmbFiltroEstado.setItems(FXCollections.observableArrayList("Todos", ACTIVO, VENCIDO));
        cmbFiltroEstado.setValue("Todos");
    }

    private void configurarFiltros() {
        cmbFiltroEstado.valueProperty().addListener((o, v, n) -> aplicarFiltros());
        txtBusqueda.textProperty().addListener((o, v, n) -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        String estado = cmbFiltroEstado.getValue();
        String busq   = txtBusqueda.getText().toLowerCase();
        listaFiltrada.setPredicate(f -> {
            boolean okEstado = "Todos".equals(estado) || estado == null || f.getEstadoPuntos().equals(estado);
            boolean okBusq   = busq.isEmpty()
                    || String.valueOf(f.getIdFidelizacion()).contains(busq)
                    || f.getNombreCliente().toLowerCase().contains(busq)
                    || String.valueOf(f.getIdCliente()).contains(busq);
            return okEstado && okBusq;
        });
    }

    // ── Botón "🔎 Buscar cliente" ─────────────────────────────────────────────
    @FXML
    private void onBuscarCliente() {
        String idC = txtIdCliente.getText().trim();
        if (idC.isBlank()) { JOptionPane.showMessageDialog(null, "Ingresa un ID de cliente."); return; }

        String sql = "SELECT p.nombre + ' ' + p.apellido AS nombre_cliente "
                + "FROM TBL_CLIENTE c "
                + "JOIN TBL_PERSONA p ON p.id_persona = c.id_persona "
                + "WHERE c.id_cliente = ?";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(idC));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtNombreCliente.setText(rs.getString("nombre_cliente"));
            } else {
                JOptionPane.showMessageDialog(null, "No se encontró el cliente #" + idC);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar: " + e.getMessage());
        }
    }

    // ── Botón "📋 Registrar" ──────────────────────────────────────────────────
    @FXML
    private void onRegistrar() {
        if (txtIdCliente.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El ID de Cliente es obligatorio."); return;
        }
        if (txtPuntos.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Los puntos son obligatorios."); return;
        }
        if (dpFechaCaducidad.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona la fecha de caducidad."); return;
        }

        // Verificar si el cliente ya tiene fidelización
        String sqlCheck = "SELECT id_fidelizacion FROM TBL_SISTEMA_FIDELIZACION WHERE id_cliente = ?";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {

            psCheck.setInt(1, Integer.parseInt(txtIdCliente.getText().trim()));
            ResultSet rs = psCheck.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(null,
                        "Este cliente ya tiene un registro de fidelización.\n" +
                                "Selecciónalo en la tabla y usa '➕ Agregar Puntos'.");
                return;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al verificar: " + e.getMessage());
            return;
        }

        String sql = "INSERT INTO TBL_SISTEMA_FIDELIZACION (id_cliente, puntos_acumulados, fecha_caducidad) "
                + "VALUES (?, ?, ?)";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1,  Integer.parseInt(txtIdCliente.getText().trim()));
            ps.setInt(2,  Integer.parseInt(txtPuntos.getText().trim()));
            ps.setDate(3, Date.valueOf(dpFechaCaducidad.getValue()));
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "Fidelización registrada correctamente.");
            actualizarTabla();
            limpiar();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar: " + e.getMessage());
        }
    }

    // ── Botón "➕ Agregar Puntos" ─────────────────────────────────────────────
    @FXML
    private void onAgregarPuntos() {
        SistemaFidelizacion sel = tablaFidelizacion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona un cliente."); return; }

        String puntosStr = txtPuntos.getText().trim();
        if (puntosStr.isBlank()) { JOptionPane.showMessageDialog(null, "Ingresa los puntos a agregar."); return; }

        int puntosAAgregar;
        try { puntosAAgregar = Integer.parseInt(puntosStr); }
        catch (NumberFormatException e) { JOptionPane.showMessageDialog(null, "Los puntos deben ser un número."); return; }

        if (puntosAAgregar <= 0) { JOptionPane.showMessageDialog(null, "Los puntos deben ser mayores a 0."); return; }

        String sql = "UPDATE TBL_SISTEMA_FIDELIZACION SET puntos_acumulados = puntos_acumulados + ? "
                + "WHERE id_fidelizacion = ?";

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

    // ── Botón "🔄 Canjear Puntos" ─────────────────────────────────────────────
    @FXML
    private void onCanjearPuntos() {
        SistemaFidelizacion sel = tablaFidelizacion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona un cliente."); return; }

        if (VENCIDO.equals(sel.getEstadoPuntos())) {
            JOptionPane.showMessageDialog(null, "Los puntos de este cliente están VENCIDOS y no se pueden canjear.");
            return;
        }

        String canjearStr = txtPuntosACanjear.getText().trim();
        if (canjearStr.isBlank()) { JOptionPane.showMessageDialog(null, "Ingresa los puntos a canjear."); return; }

        int puntosACanjear;
        try { puntosACanjear = Integer.parseInt(canjearStr); }
        catch (NumberFormatException e) { JOptionPane.showMessageDialog(null, "Los puntos deben ser un número."); return; }

        if (puntosACanjear <= 0) {
            JOptionPane.showMessageDialog(null, "Los puntos a canjear deben ser mayores a 0."); return;
        }
        if (puntosACanjear > sel.getPuntosAcumulados()) {
            JOptionPane.showMessageDialog(null,
                    "No tiene suficientes puntos.\nDisponibles: " + sel.getPuntosAcumulados());
            return;
        }

        String sql = "UPDATE TBL_SISTEMA_FIDELIZACION SET puntos_acumulados = puntos_acumulados - ? "
                + "WHERE id_fidelizacion = ?";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, puntosACanjear);
            ps.setInt(2, sel.getIdFidelizacion());
            ps.executeUpdate();

            int restantes = sel.getPuntosAcumulados() - puntosACanjear;
            JOptionPane.showMessageDialog(null,
                    "Canje exitoso ✔\nPuntos canjeados: " + puntosACanjear +
                            "\nPuntos restantes: " + restantes);
            actualizarTabla();
            txtPuntosACanjear.clear();
            lblPuntosRestantes.setText("Restantes: —");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al canjear: " + e.getMessage());
        }
    }

    // ── Botón "📅 Renovar Caducidad" ──────────────────────────────────────────
    @FXML
    private void onRenovarCaducidad() {
        SistemaFidelizacion sel = tablaFidelizacion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona un cliente."); return; }
        if (dpFechaCaducidad.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona la nueva fecha de caducidad."); return;
        }

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

    // ── Botón "🗑 Eliminar" ───────────────────────────────────────────────────
    @FXML
    private void onEliminar() {
        SistemaFidelizacion sel = tablaFidelizacion.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona un registro."); return; }

        int confirm = JOptionPane.showConfirmDialog(null,
                "¿Eliminar fidelización del cliente " + sel.getNombreCliente() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM TBL_SISTEMA_FIDELIZACION WHERE id_fidelizacion = ?";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, sel.getIdFidelizacion());
            ps.executeUpdate();

            listaHistorial.clear();
            lblHistorialDe.setText("—");
            actualizarTabla();
            limpiar();
            JOptionPane.showMessageDialog(null, "Registro eliminado.");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar: " + e.getMessage());
        }
    }

    // ── Botón "✖ Limpiar" ────────────────────────────────────────────────────
    @FXML
    private void onLimpiar() { limpiar(); }

    @FXML
    public void limpiar() {
        txtIdFidelizacion.clear();
        txtIdCliente.clear();
        txtNombreCliente.clear();
        txtPuntos.clear();
        txtPuntosACanjear.clear();
        dpFechaCaducidad.setValue(LocalDate.now().plusYears(1));
        lblPuntosRestantes.setText("Restantes: —");
        tablaFidelizacion.getSelectionModel().clearSelection();
    }

    // ── Cargar tabla desde BD ─────────────────────────────────────────────────
    private void actualizarTabla() {
        String sql = "SELECT f.id_fidelizacion, f.id_cliente, "
                + "p.nombre + ' ' + p.apellido AS nombre_cliente, "
                + "f.puntos_acumulados, f.fecha_caducidad "
                + "FROM TBL_SISTEMA_FIDELIZACION f "
                + "JOIN TBL_CLIENTE c ON c.id_cliente = f.id_cliente "
                + "JOIN TBL_PERSONA p ON p.id_persona = c.id_persona";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            listaFidelizacion.clear();
            while (rs.next()) {
                listaFidelizacion.add(new SistemaFidelizacion(
                        rs.getInt("id_fidelizacion"),
                        rs.getInt("id_cliente"),
                        rs.getString("nombre_cliente"),
                        rs.getInt("puntos_acumulados"),
                        rs.getDate("fecha_caducidad").toLocalDate()
                ));
            }
            actualizarContadores();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar: " + e.getMessage());
        }
    }

    // ── Historial simulado (movimientos de puntos del cliente) ────────────────
    private void cargarHistorial(SistemaFidelizacion f) {
        listaHistorial.clear();
        lblHistorialDe.setText(f.getNombreCliente());
        listaHistorial.add("📋  ID Fidelización   : " + f.getIdFidelizacion());
        listaHistorial.add("👤  ID Cliente        : " + f.getIdCliente());
        listaHistorial.add("⭐  Puntos acumulados : " + f.getPuntosAcumulados());
        listaHistorial.add("📅  Fecha caducidad   : " + f.getFechaCaducidad());
        listaHistorial.add("🔵  Estado            : " + f.getEstadoPuntos());
        listaHistorial.add("──────────────────────────────");

        // Calcular cuánto vale en descuento (ejemplo: 100 puntos = RD$50)
        double descuento = f.getPuntosAcumulados() * 0.50;
        listaHistorial.add("💰  Valor en descuento: RD$ " + String.format("%.2f", descuento));

        // Nivel del cliente según puntos
        String nivel;
        if      (f.getPuntosAcumulados() >= 1000) nivel = "🥇 ORO";
        else if (f.getPuntosAcumulados() >= 500)  nivel = "🥈 PLATA";
        else if (f.getPuntosAcumulados() >= 100)  nivel = "🥉 BRONCE";
        else                                       nivel = "⚪ BÁSICO";
        listaHistorial.add("🏆  Nivel del cliente : " + nivel);

        // Actualizar label puntos restantes al seleccionar
        lblPuntosRestantes.setText("Disponibles: " + f.getPuntosAcumulados() + " pts");
    }

    // ── Listener en campo canjear para mostrar restantes en tiempo real ────────
    @FXML
    private void onCanjearInput() {
        SistemaFidelizacion sel = tablaFidelizacion.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try {
            int canjear    = Integer.parseInt(txtPuntosACanjear.getText().trim());
            int restantes  = sel.getPuntosAcumulados() - canjear;
            lblPuntosRestantes.setText("Restantes: " + (restantes >= 0 ? restantes : "⚠ Insuficientes") + " pts");
        } catch (NumberFormatException e) {
            lblPuntosRestantes.setText("Restantes: —");
        }
    }

    // ── Cargar formulario al seleccionar fila ─────────────────────────────────
    private void cargarEnFormulario(SistemaFidelizacion f) {
        txtIdFidelizacion.setText(String.valueOf(f.getIdFidelizacion()));
        txtIdCliente.setText(String.valueOf(f.getIdCliente()));
        txtNombreCliente.setText(f.getNombreCliente());
        txtPuntos.clear();
        dpFechaCaducidad.setValue(f.getFechaCaducidad());
    }

    // ── Pastillas de conteo ───────────────────────────────────────────────────
    private void actualizarContadores() {
        long activos  = listaFidelizacion.stream().filter(f -> ACTIVO.equals(f.getEstadoPuntos())).count();
        long vencidos = listaFidelizacion.stream().filter(f -> VENCIDO.equals(f.getEstadoPuntos())).count();
        long totalPts = listaFidelizacion.stream().mapToLong(SistemaFidelizacion::getPuntosAcumulados).sum();
        lblContActivo.setText("✔  "  + activos  + "  Activos");
        lblContVencido.setText("✖  " + vencidos + "  Vencidos");
        lblTotalPuntos.setText("⭐  " + totalPts + "  Puntos totales");
    }
}