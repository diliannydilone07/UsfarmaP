package com.example.farmaventa;

import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.NominaItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javax.swing.JOptionPane;
import java.sql.*;
import java.util.LinkedHashMap;

/**
 * Módulo de Nómina independiente.
 *
 * Pestaña 0 — Crear / Editar Nómina   : buscar empleado por ID, agregar a la lista,
 *                                        ajustar bonif/comis/horas/descuento y guardar.
 * Pestaña 1 — Registrar Pago           : seleccionar nóminas pendientes y pagar
 *                                        (efectivo o cuenta bancaria TBL_CUENTA).
 * Pestaña 2 — Historial                : historial de nóminas pagadas por empleado.
 *
 * Tablas BD:
 *   TBL_NOMINA        (id_nomina, fecha_pago, periodo, id_cuenta, monto_total_nomina, tipo_pago)
 *   TBL_DETALLE_NOMINA(id_nomina, id_empleado, bonificacion, comision_venta,
 *                       horas_extras, monto_horas_extras, descuento, salario_neto)
 *   TBL_EMPLEADO → TBL_PERSONA  (nombre, apellido)
 *   TBL_CARGO         (salario_base, nombre)
 *   TBL_CUENTA        (id_cuenta, nombre, banco)
 */
public class NominaController {

    Conexion conexion = new Conexion();

    // ══════════════════════════════════════════════════════════════════════
    // PESTAÑA 0 — CREAR / EDITAR NÓMINA
    // ══════════════════════════════════════════════════════════════════════

    // Búsqueda de empleado
    @FXML private TextField        txtIdEmpleadoBuscar;
    @FXML private Label            lblInfoEmpleado;

    // Campos de ajuste por empleado
    @FXML private TextField        txtBonificacion;
    @FXML private TextField        txtComision;
    @FXML private TextField        txtMontoHorasExtras;
    @FXML private TextField        txtDescuento;
    @FXML private Label            lblSalarioBaseRef;
    @FXML private Label            lblNetoCalculado;

    // Encabezado de la nómina
    @FXML private DatePicker       dpFechaPagoNomina;
    @FXML private TextField        txtPeriodoDias;
    @FXML private ComboBox<String> cmbTipoPagoNomina;   // "Efectivo" / "Transferencia" / etc.
    @FXML private ComboBox<String> cmbCuentaNomina;
    @FXML private Label            lblTotalNomina;
    @FXML private Label            lblCantEmpleados;

    // Tabla de la nómina en construcción
    @FXML private TableView<NominaItem>           tablaNomina;
    @FXML private TableColumn<NominaItem, Number> colNomEmp;
    @FXML private TableColumn<NominaItem, String> colNomNombre;
    @FXML private TableColumn<NominaItem, String> colNomCargo;
    @FXML private TableColumn<NominaItem, Number> colNomBase;
    @FXML private TableColumn<NominaItem, Number> colNomBonif;
    @FXML private TableColumn<NominaItem, Number> colNomComis;
    @FXML private TableColumn<NominaItem, Number> colNomHoras;
    @FXML private TableColumn<NominaItem, Number> colNomDesc;
    @FXML private TableColumn<NominaItem, Number> colNomNeto;
    @FXML private TableColumn<NominaItem, String> colNomEstado;

    // ══════════════════════════════════════════════════════════════════════
    // PESTAÑA 1 — REGISTRAR PAGO
    // ══════════════════════════════════════════════════════════════════════
    @FXML private TextField        txtBuscarNominaPago;
    @FXML private ComboBox<String> cmbFiltroPago;
    @FXML private TableView<NominaItem>           tablaPendientes;
    @FXML private TableColumn<NominaItem, Number> colPagEmp;
    @FXML private TableColumn<NominaItem, String> colPagNombre;
    @FXML private TableColumn<NominaItem, String> colPagCargo;
    @FXML private TableColumn<NominaItem, String> colPagFecha;
    @FXML private TableColumn<NominaItem, Number> colPagNeto;
    @FXML private TableColumn<NominaItem, String> colPagTipo;
    @FXML private TableColumn<NominaItem, String> colPagEstado;

    // Formulario de pago
    @FXML private TextField        txtIdNominaPagar;
    @FXML private Label            lblInfoNominaPagar;
    @FXML private ComboBox<String> cmbMetodoPagoNom;
    @FXML private ComboBox<String> cmbCuentaPago;
    @FXML private DatePicker       dpFechaPago;
    @FXML private Label            lblMontoPendientePago;

    // ══════════════════════════════════════════════════════════════════════
    // PESTAÑA 2 — HISTORIAL
    // ══════════════════════════════════════════════════════════════════════
    @FXML private TextField        txtBuscarHistorial;
    @FXML private DatePicker       dpDesde;
    @FXML private DatePicker       dpHasta;
    @FXML private TableView<NominaItem>           tablaHistorial;
    @FXML private TableColumn<NominaItem, Number> colHistId;
    @FXML private TableColumn<NominaItem, Number> colHistEmp;
    @FXML private TableColumn<NominaItem, String> colHistNombre;
    @FXML private TableColumn<NominaItem, String> colHistCargo;
    @FXML private TableColumn<NominaItem, Number> colHistBase;
    @FXML private TableColumn<NominaItem, Number> colHistBonif;
    @FXML private TableColumn<NominaItem, Number> colHistComis;
    @FXML private TableColumn<NominaItem, Number> colHistHoras;
    @FXML private TableColumn<NominaItem, Number> colHistDesc;
    @FXML private TableColumn<NominaItem, Number> colHistNeto;
    @FXML private TableColumn<NominaItem, String> colHistFecha;
    @FXML private TableColumn<NominaItem, String> colHistTipo;
    @FXML private Label                           lblResumenHistorial;

    // ── Datos internos ────────────────────────────────────────────────────
    private final ObservableList<NominaItem> listaActual     = FXCollections.observableArrayList();
    private final ObservableList<NominaItem> listaPendientes = FXCollections.observableArrayList();
    private final ObservableList<NominaItem> listaHistorial  = FXCollections.observableArrayList();
    private final LinkedHashMap<String, Integer> mapaCuentas = new LinkedHashMap<>();

    // Empleado actualmente buscado (temporal antes de agregar)
    private int    idEmpleadoTemp   = -1;
    private String nombreEmpTemp    = "";
    private String cargoEmpTemp     = "";
    private double salarioBaseTemp  = 0;

    // ── Initialize ────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        cargarCuentas();
        configurarTablaCrear();
        configurarTablaPendientes();
        configurarTablaHistorial();
        configurarCombos();
        cargarNominasPendientes();
        cargarHistorial();

        if (dpFechaPagoNomina != null) dpFechaPagoNomina.setValue(java.time.LocalDate.now());
        if (dpFechaPago       != null) dpFechaPago.setValue(java.time.LocalDate.now());
        if (dpDesde           != null) dpDesde.setValue(java.time.LocalDate.now().withDayOfMonth(1));
        if (dpHasta           != null) dpHasta.setValue(java.time.LocalDate.now());
        actualizarTotalesCrear();
    }

    // ── Configurar combos ─────────────────────────────────────────────────
    private void configurarCombos() {
        String[] metodos = {"Efectivo", "Transferencia", "Tarjeta", "Cheque"};
        if (cmbTipoPagoNomina != null) cmbTipoPagoNomina.getItems().addAll(metodos);
        if (cmbMetodoPagoNom  != null) cmbMetodoPagoNom.getItems().addAll(metodos);
        if (cmbFiltroPago     != null) {
            cmbFiltroPago.getItems().addAll("Todos", "Pendiente", "Pagado");
            cmbFiltroPago.setValue("Pendiente");
            cmbFiltroPago.setOnAction(e -> filtrarPendientes());
        }
    }

    // ── Cargar cuentas bancarias ──────────────────────────────────────────
    private void cargarCuentas() {
        mapaCuentas.clear();
        String sql = "SELECT id_cuenta, nombre, banco FROM tbl_CUENTA ORDER BY banco, nombre";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String label = rs.getString("banco") + " - " + rs.getString("nombre");
                mapaCuentas.put(label, rs.getInt("id_cuenta"));
                if (cmbCuentaNomina != null) cmbCuentaNomina.getItems().add(label);
                if (cmbCuentaPago   != null) cmbCuentaPago.getItems().add(label);
            }
        } catch (SQLException e) {
            System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING, "Sin cuentas", e);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // PESTAÑA 0 — CONFIGURAR TABLA CREAR
    // ══════════════════════════════════════════════════════════════════════
    private void configurarTablaCrear() {
        if (tablaNomina == null) return;
        colNomEmp.setCellValueFactory(c -> c.getValue().idEmpleadoProperty());
        colNomNombre.setCellValueFactory(c -> c.getValue().nombreEmpleadoProperty());
        colNomCargo.setCellValueFactory(c -> c.getValue().cargoProperty());
        colNomBase.setCellValueFactory(c -> c.getValue().salarioBaseProperty());
        colNomBonif.setCellValueFactory(c -> c.getValue().bonificacionProperty());
        colNomComis.setCellValueFactory(c -> c.getValue().comisionVentaProperty());
        colNomHoras.setCellValueFactory(c -> c.getValue().horasExtrasProperty());
        colNomDesc.setCellValueFactory(c -> c.getValue().descuentoProperty());
        colNomNeto.setCellValueFactory(c -> c.getValue().salarioNetoProperty());
        colNomEstado.setCellValueFactory(c -> c.getValue().estadoProperty());

        aplicarColorEstado(colNomEstado);
        tablaNomina.setItems(listaActual);

        // Al seleccionar fila, precarga sus valores en los campos de ajuste
        tablaNomina.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) precargarAjuste(n);
        });
    }

    private void precargarAjuste(NominaItem item) {
        idEmpleadoTemp  = item.getIdEmpleado();
        nombreEmpTemp   = item.getNombreEmpleado();
        cargoEmpTemp    = item.getCargo();
        salarioBaseTemp = item.getSalarioBase();
        if (txtIdEmpleadoBuscar  != null) txtIdEmpleadoBuscar.setText(String.valueOf(item.getIdEmpleado()));
        if (lblInfoEmpleado      != null) lblInfoEmpleado.setText(item.getNombreEmpleado() + " | " + item.getCargo());
        if (lblSalarioBaseRef    != null) lblSalarioBaseRef.setText("Salario base: RD$ " + String.format("%.2f", item.getSalarioBase()));
        if (txtBonificacion      != null) txtBonificacion.setText(String.format("%.2f", item.getBonificacion()));
        if (txtComision          != null) txtComision.setText(String.format("%.2f", item.getComisionVenta()));
        if (txtMontoHorasExtras  != null) txtMontoHorasExtras.setText(String.format("%.2f", item.getHorasExtras()));
        if (txtDescuento         != null) txtDescuento.setText(String.format("%.2f", item.getDescuento()));
        recalcularNetoTemp();
    }

    // ══════════════════════════════════════════════════════════════════════
    // BUSCAR EMPLEADO POR ID
    // ══════════════════════════════════════════════════════════════════════
    @FXML
    public void onBuscarEmpleado(ActionEvent ignored) {
        if (txtIdEmpleadoBuscar == null || txtIdEmpleadoBuscar.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Ingresa el ID del empleado.",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE); return;
        }
        String sql =
                "SELECT e.id_empleado, " +
                        "       p.nombre + ' ' + p.apellido AS nombre_completo, " +
                        "       c.nombre AS cargo, c.salario_base " +
                        "FROM TBL_EMPLEADO e " +
                        "JOIN TBL_PERSONA p ON p.id_persona = e.id_persona " +
                        "JOIN TBL_CARGO   c ON c.id_cargo   = e.id_cargo " +
                        "WHERE e.id_empleado = ?";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(txtIdEmpleadoBuscar.getText().trim()));
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(null, "Empleado no encontrado.",
                        "No encontrado", JOptionPane.WARNING_MESSAGE);
                limpiarBusquedaEmpleado(); return;
            }
            idEmpleadoTemp  = rs.getInt("id_empleado");
            nombreEmpTemp   = rs.getString("nombre_completo");
            cargoEmpTemp    = rs.getString("cargo");
            salarioBaseTemp = rs.getDouble("salario_base");

            if (lblInfoEmpleado   != null)
                lblInfoEmpleado.setText("✔ " + nombreEmpTemp + " | " + cargoEmpTemp);
            if (lblSalarioBaseRef != null)
                lblSalarioBaseRef.setText("Salario base: RD$ " + String.format("%.2f", salarioBaseTemp));

            // Limpiar campos de ajuste para nuevo empleado
            if (txtBonificacion     != null) txtBonificacion.setText("0.00");
            if (txtComision         != null) txtComision.setText("0.00");
            if (txtMontoHorasExtras != null) txtMontoHorasExtras.setText("0.00");
            if (txtDescuento        != null) txtDescuento.setText("0.00");
            recalcularNetoTemp();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "El ID debe ser un número.", "ID inválido", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // AGREGAR EMPLEADO A LA LISTA
    // ══════════════════════════════════════════════════════════════════════
    @FXML
    public void onAgregarEmpleadoNomina(ActionEvent ignored) {
        if (idEmpleadoTemp == -1) {
            JOptionPane.showMessageDialog(null, "Primero busca un empleado.",
                    "Sin empleado", JOptionPane.WARNING_MESSAGE); return;
        }
        // Verificar duplicado
        for (NominaItem n : listaActual) {
            if (n.getIdEmpleado() == idEmpleadoTemp) {
                JOptionPane.showMessageDialog(null,
                        "El empleado " + nombreEmpTemp + " ya está en la lista.",
                        "Duplicado", JOptionPane.WARNING_MESSAGE); return;
            }
        }

        double bonif  = parseField(txtBonificacion,    0);
        double comis  = parseField(txtComision,         0);
        double horas  = parseField(txtMontoHorasExtras, 0);
        double desc   = parseField(txtDescuento,        0);
        double neto   = Math.max(0, salarioBaseTemp + bonif + comis + horas - desc);

        NominaItem item = new NominaItem(
                0, idEmpleadoTemp, nombreEmpTemp, cargoEmpTemp,
                salarioBaseTemp, bonif, comis, horas, desc,
                Math.round(neto * 100.0) / 100.0,
                "", "", "Pendiente", 0,
                parsePeriodo());
        listaActual.add(item);
        actualizarTotalesCrear();
        limpiarBusquedaEmpleado();
    }

    // ══════════════════════════════════════════════════════════════════════
    // ACTUALIZAR EMPLEADO YA EN LISTA (al seleccionar y modificar ajustes)
    // ══════════════════════════════════════════════════════════════════════
    @FXML
    public void onActualizarAjuste(ActionEvent ignored) {
        NominaItem sel = tablaNomina.getSelectionModel().getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(null, "Selecciona un empleado de la tabla para actualizar sus ajustes.",
                    "Sin selección", JOptionPane.WARNING_MESSAGE); return;
        }
        sel.setBonificacion(parseField(txtBonificacion, 0));
        sel.setComisionVenta(parseField(txtComision, 0));
        sel.setHorasExtras(parseField(txtMontoHorasExtras, 0));
        sel.setDescuento(parseField(txtDescuento, 0));
        sel.recalcular();
        tablaNomina.refresh();
        actualizarTotalesCrear();
    }

    // ══════════════════════════════════════════════════════════════════════
    // QUITAR EMPLEADO DE LA LISTA
    // ══════════════════════════════════════════════════════════════════════
    @FXML
    public void onQuitarEmpleadoNomina(ActionEvent ignored) {
        NominaItem sel = tablaNomina.getSelectionModel().getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(null, "Selecciona un empleado para quitar.",
                    "Sin selección", JOptionPane.WARNING_MESSAGE); return;
        }
        listaActual.remove(sel);
        actualizarTotalesCrear();
    }

    // ══════════════════════════════════════════════════════════════════════
    // GUARDAR NÓMINA EN BD
    // ══════════════════════════════════════════════════════════════════════
    @FXML
    public void onGuardarNomina(ActionEvent ignored) {
        if (listaActual.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Agrega al menos un empleado a la nómina.",
                    "Lista vacía", JOptionPane.WARNING_MESSAGE); return;
        }
        if (dpFechaPagoNomina == null || dpFechaPagoNomina.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona la fecha de pago.",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE); return;
        }
        if (txtPeriodoDias == null || txtPeriodoDias.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Ingresa el período en días.",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE); return;
        }
        if (cmbTipoPagoNomina == null || cmbTipoPagoNomina.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona el tipo de pago.",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE); return;
        }

        int    periodo    = parsePeriodo();
        String tipoPago   = cmbTipoPagoNomina.getValue();
        int    idCuenta   = resolverId_Cuenta(tipoPago);
        double montoTotal = listaActual.stream().mapToDouble(NominaItem::getSalarioNeto).sum();

        try (Connection con = conexion.establecerConexion()) {
            int guardados = 0;
            for (NominaItem item : listaActual) {
                // Insertar TBL_NOMINA por empleado
                PreparedStatement psN = con.prepareStatement(
                        "INSERT INTO TBL_NOMINA " +
                                "(fecha_pago, periodo, id_cuenta, monto_total_nomina, tipo_pago) " +
                                "VALUES (?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);
                psN.setDate(1,   java.sql.Date.valueOf(dpFechaPagoNomina.getValue()));
                psN.setInt(2,    periodo);
                psN.setInt(3,    idCuenta);
                psN.setDouble(4, item.getSalarioNeto());
                psN.setString(5, tipoPago);
                psN.executeUpdate();

                int idNomina = -1;
                ResultSet keys = psN.getGeneratedKeys();
                if (keys.next()) idNomina = keys.getInt(1);

                // Insertar TBL_DETALLE_NOMINA
                PreparedStatement psD = con.prepareStatement(
                        "INSERT INTO TBL_DETALLE_NOMINA " +
                                "(id_nomina, id_empleado, bonificacion, comision_venta, " +
                                " horas_extras, monto_horas_extras, descuento, salario_neto) " +
                                "VALUES (?,?,?,?,?,?,?,?)");
                psD.setInt(1,    idNomina);
                psD.setInt(2,    item.getIdEmpleado());
                psD.setDouble(3, item.getBonificacion());
                psD.setDouble(4, item.getComisionVenta());
                psD.setTime(5,   java.sql.Time.valueOf("00:00:00")); // horas_extras es TIME en BD
                psD.setDouble(6, item.getHorasExtras());             // monto_horas_extras
                psD.setDouble(7, item.getDescuento());
                psD.setDouble(8, item.getSalarioNeto());
                psD.executeUpdate();

                item.setIdNomina(idNomina);
                item.setIdCuenta(idCuenta);
                item.setPeriodo(periodo);
                guardados++;
            }

            JOptionPane.showMessageDialog(null,
                    "✔ Nómina guardada correctamente.\n" +
                            "Empleados registrados: " + guardados + "\n" +
                            "Monto total:           RD$ " + String.format("%.2f", montoTotal) + "\n" +
                            "Período:               " + periodo + " días\n" +
                            "Tipo de pago:          " + tipoPago,
                    "Nómina guardada", JOptionPane.INFORMATION_MESSAGE);

            cargarNominasPendientes();
            cargarHistorial();
            onLimpiarNomina(null);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al guardar nómina: " + e.getMessage(),
                    "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // PESTAÑA 1 — NÓMINAS PENDIENTES DE PAGO
    // ══════════════════════════════════════════════════════════════════════
    private void configurarTablaPendientes() {
        if (tablaPendientes == null) return;
        colPagEmp.setCellValueFactory(c -> c.getValue().idEmpleadoProperty());
        colPagNombre.setCellValueFactory(c -> c.getValue().nombreEmpleadoProperty());
        colPagCargo.setCellValueFactory(c -> c.getValue().cargoProperty());
        colPagFecha.setCellValueFactory(c -> c.getValue().fechaPagoProperty());
        colPagNeto.setCellValueFactory(c -> c.getValue().salarioNetoProperty());
        colPagTipo.setCellValueFactory(c -> c.getValue().tipoPagoProperty());
        colPagEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
        aplicarColorEstado(colPagEstado);
        tablaPendientes.setItems(listaPendientes);

        tablaPendientes.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) cargarEnFormularioPago(n);
        });
    }

    private void cargarNominasPendientes() {
        listaPendientes.clear();
        // "Pendiente" = nóminas guardadas cuya fecha_pago es >= hoy O que el tipo_pago aún no aparece
        // en la práctica: todas las nóminas recientes; el estado se infiere del campo tipo_pago
        // Para distinguir Pagado/Pendiente usamos un campo estado calculado:
        // - si monto_total_nomina > 0 y existe en TBL_DETALLE_NOMINA → Pendiente hasta que se marque
        // La BD no tiene campo estado en TBL_NOMINA según el diccionario, así que
        // usamos una convención: registros cuya fecha_pago >= hoy son Pendientes.
        String sql =
                "SELECT n.id_nomina, n.fecha_pago, n.periodo, n.monto_total_nomina, n.tipo_pago, " +
                        "       d.id_empleado, d.bonificacion, d.comision_venta, d.monto_horas_extras, " +
                        "       d.descuento, d.salario_neto, " +
                        "       p.nombre + ' ' + p.apellido AS nombre_empleado, " +
                        "       c.nombre AS cargo, c.salario_base, " +
                        "       CONVERT(VARCHAR(10), n.fecha_pago, 120) AS fecha_str " +
                        "FROM TBL_NOMINA n " +
                        "JOIN TBL_DETALLE_NOMINA d ON d.id_nomina   = n.id_nomina " +
                        "JOIN TBL_EMPLEADO e       ON e.id_empleado = d.id_empleado " +
                        "JOIN TBL_PERSONA p        ON p.id_persona  = e.id_persona " +
                        "JOIN TBL_CARGO c          ON c.id_cargo    = e.id_cargo " +
                        "ORDER BY n.fecha_pago DESC, p.nombre";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                listaPendientes.add(new NominaItem(
                        rs.getInt("id_nomina"),
                        rs.getInt("id_empleado"),
                        rs.getString("nombre_empleado"),
                        rs.getString("cargo"),
                        rs.getDouble("salario_base"),
                        rs.getDouble("bonificacion"),
                        rs.getDouble("comision_venta"),
                        rs.getDouble("monto_horas_extras"),
                        rs.getDouble("descuento"),
                        rs.getDouble("salario_neto"),
                        rs.getString("tipo_pago"),
                        rs.getString("fecha_str"),
                        "Pendiente",
                        0, rs.getInt("periodo")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar nóminas: " + e.getMessage());
        }
        filtrarPendientes();
    }

    private void filtrarPendientes() {
        String filtro   = (cmbFiltroPago   != null && cmbFiltroPago.getValue()   != null)
                ? cmbFiltroPago.getValue() : "Todos";
        String busqueda = (txtBuscarNominaPago != null)
                ? txtBuscarNominaPago.getText().trim().toLowerCase() : "";
        ObservableList<NominaItem> filtrada = FXCollections.observableArrayList();
        for (NominaItem n : listaPendientes) {
            boolean estad = "Todos".equals(filtro) || n.getEstado().equals(filtro);
            boolean texto = busqueda.isEmpty()
                    || n.getNombreEmpleado().toLowerCase().contains(busqueda)
                    || String.valueOf(n.getIdEmpleado()).contains(busqueda)
                    || String.valueOf(n.getIdNomina()).contains(busqueda);
            if (estad && texto) filtrada.add(n);
        }
        tablaPendientes.setItems(filtrada);
    }

    @FXML public void onBuscarNominaPago(ActionEvent ignored) { filtrarPendientes(); }
    @FXML public void onRefrescarPendientes(ActionEvent ignored) { cargarNominasPendientes(); }

    private void cargarEnFormularioPago(NominaItem n) {
        if (txtIdNominaPagar     != null) txtIdNominaPagar.setText(String.valueOf(n.getIdNomina()));
        if (lblInfoNominaPagar   != null)
            lblInfoNominaPagar.setText("Nómina #" + n.getIdNomina()
                    + " — " + n.getNombreEmpleado()
                    + " | " + n.getCargo());
        if (lblMontoPendientePago != null)
            lblMontoPendientePago.setText("Monto a pagar: RD$ " + String.format("%.2f", n.getSalarioNeto()));
        if (cmbMetodoPagoNom != null) cmbMetodoPagoNom.setValue(
                n.getTipoPago() != null && !n.getTipoPago().isBlank() ? n.getTipoPago() : null);
    }

    // ══════════════════════════════════════════════════════════════════════
    // REGISTRAR PAGO DE NÓMINA
    // ══════════════════════════════════════════════════════════════════════
    @FXML
    public void onRegistrarPagoNomina(ActionEvent ignored) {
        if (txtIdNominaPagar == null || txtIdNominaPagar.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Selecciona una nómina de la lista o ingresa el ID.",
                    "Sin selección", JOptionPane.WARNING_MESSAGE); return;
        }
        if (cmbMetodoPagoNom == null || cmbMetodoPagoNom.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona el método de pago.",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE); return;
        }
        if (dpFechaPago == null || dpFechaPago.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona la fecha del pago.",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE); return;
        }

        String metodo   = cmbMetodoPagoNom.getValue();
        int    idCuenta = resolverId_Cuenta(metodo);

        // Si es Transferencia/Tarjeta/Cheque y no seleccionaron cuenta → pedir
        if (!"Efectivo".equals(metodo)) {
            if (cmbCuentaPago == null || cmbCuentaPago.getValue() == null
                    || !mapaCuentas.containsKey(cmbCuentaPago.getValue())) {
                JOptionPane.showMessageDialog(null,
                        "Selecciona la cuenta bancaria para el pago.",
                        "Cuenta requerida", JOptionPane.WARNING_MESSAGE); return;
            }
            idCuenta = mapaCuentas.get(cmbCuentaPago.getValue());
        }

        int idNomina;
        try { idNomina = Integer.parseInt(txtIdNominaPagar.getText().trim()); }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID de nómina inválido.", "Error", JOptionPane.ERROR_MESSAGE); return;
        }

        try (Connection con = conexion.establecerConexion()) {
            // Leer monto desde BD
            PreparedStatement psGet = con.prepareStatement(
                    "SELECT monto_total_nomina FROM TBL_NOMINA WHERE id_nomina = ?");
            psGet.setInt(1, idNomina);
            ResultSet rs = psGet.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(null, "Nómina #" + idNomina + " no encontrada.",
                        "No encontrado", JOptionPane.ERROR_MESSAGE); return;
            }
            double monto = rs.getDouble("monto_total_nomina");

            // Actualizar fecha_pago y cuenta (marca como procesado)
            PreparedStatement psUpd = con.prepareStatement(
                    "UPDATE TBL_NOMINA SET fecha_pago = ?, tipo_pago = ?, id_cuenta = ? WHERE id_nomina = ?");
            psUpd.setDate(1,   java.sql.Date.valueOf(dpFechaPago.getValue()));
            psUpd.setString(2, metodo);
            psUpd.setInt(3,    idCuenta);
            psUpd.setInt(4,    idNomina);
            psUpd.executeUpdate();

            JOptionPane.showMessageDialog(null,
                    "✔ Pago de nómina #" + idNomina + " registrado.\n" +
                            "Monto:   RD$ " + String.format("%.2f", monto) + "\n" +
                            "Método:  " + metodo + "\n" +
                            "Fecha:   " + dpFechaPago.getValue(),
                    "Pago registrado", JOptionPane.INFORMATION_MESSAGE);

            // Marcar como pagado en la lista visual
            for (NominaItem n : listaPendientes) {
                if (n.getIdNomina() == idNomina) { n.setEstado("Pagado"); break; }
            }
            tablaPendientes.refresh();
            cargarHistorial();
            limpiarFormularioPago();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar pago: " + e.getMessage(),
                    "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // PESTAÑA 2 — HISTORIAL
    // ══════════════════════════════════════════════════════════════════════
    private void configurarTablaHistorial() {
        if (tablaHistorial == null) return;
        colHistId.setCellValueFactory(c -> c.getValue().idNominaProperty());
        colHistEmp.setCellValueFactory(c -> c.getValue().idEmpleadoProperty());
        colHistNombre.setCellValueFactory(c -> c.getValue().nombreEmpleadoProperty());
        colHistCargo.setCellValueFactory(c -> c.getValue().cargoProperty());
        colHistBase.setCellValueFactory(c -> c.getValue().salarioBaseProperty());
        colHistBonif.setCellValueFactory(c -> c.getValue().bonificacionProperty());
        colHistComis.setCellValueFactory(c -> c.getValue().comisionVentaProperty());
        colHistHoras.setCellValueFactory(c -> c.getValue().horasExtrasProperty());
        colHistDesc.setCellValueFactory(c -> c.getValue().descuentoProperty());
        colHistNeto.setCellValueFactory(c -> c.getValue().salarioNetoProperty());
        colHistFecha.setCellValueFactory(c -> c.getValue().fechaPagoProperty());
        colHistTipo.setCellValueFactory(c -> c.getValue().tipoPagoProperty());
        tablaHistorial.setItems(listaHistorial);
    }

    private void cargarHistorial() {
        listaHistorial.clear();
        java.time.LocalDate desde = (dpDesde != null && dpDesde.getValue() != null)
                ? dpDesde.getValue() : java.time.LocalDate.now().withDayOfMonth(1);
        java.time.LocalDate hasta = (dpHasta != null && dpHasta.getValue() != null)
                ? dpHasta.getValue() : java.time.LocalDate.now();

        String sql =
                "SELECT n.id_nomina, n.periodo, n.monto_total_nomina, n.tipo_pago, " +
                        "       CONVERT(VARCHAR(10), n.fecha_pago, 120) AS fecha_str, " +
                        "       d.id_empleado, d.bonificacion, d.comision_venta, " +
                        "       d.monto_horas_extras, d.descuento, d.salario_neto, " +
                        "       p.nombre + ' ' + p.apellido AS nombre_empleado, " +
                        "       c.nombre AS cargo, c.salario_base " +
                        "FROM TBL_NOMINA n " +
                        "JOIN TBL_DETALLE_NOMINA d ON d.id_nomina   = n.id_nomina " +
                        "JOIN TBL_EMPLEADO e       ON e.id_empleado = d.id_empleado " +
                        "JOIN TBL_PERSONA p        ON p.id_persona  = e.id_persona " +
                        "JOIN TBL_CARGO c          ON c.id_cargo    = e.id_cargo " +
                        "WHERE n.fecha_pago BETWEEN ? AND ? " +
                        "ORDER BY n.fecha_pago DESC, p.nombre";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(desde));
            ps.setDate(2, java.sql.Date.valueOf(hasta));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listaHistorial.add(new NominaItem(
                        rs.getInt("id_nomina"),
                        rs.getInt("id_empleado"),
                        rs.getString("nombre_empleado"),
                        rs.getString("cargo"),
                        rs.getDouble("salario_base"),
                        rs.getDouble("bonificacion"),
                        rs.getDouble("comision_venta"),
                        rs.getDouble("monto_horas_extras"),
                        rs.getDouble("descuento"),
                        rs.getDouble("salario_neto"),
                        rs.getString("tipo_pago"),
                        rs.getString("fecha_str"),
                        "Pagado",
                        0, rs.getInt("periodo")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar historial: " + e.getMessage());
        }
        actualizarResumenHistorial();
    }

    @FXML
    public void onFiltrarHistorial(ActionEvent ignored) {
        String busqueda = (txtBuscarHistorial != null)
                ? txtBuscarHistorial.getText().trim().toLowerCase() : "";
        if (busqueda.isEmpty()) { cargarHistorial(); return; }
        cargarHistorial();
        ObservableList<NominaItem> filtrada = FXCollections.observableArrayList();
        for (NominaItem n : listaHistorial) {
            if (n.getNombreEmpleado().toLowerCase().contains(busqueda)
                    || String.valueOf(n.getIdEmpleado()).contains(busqueda))
                filtrada.add(n);
        }
        tablaHistorial.setItems(filtrada);
        actualizarResumenHistorial();
    }

    @FXML public void onRefrescarHistorial(ActionEvent ignored) { cargarHistorial(); }

    private void actualizarResumenHistorial() {
        if (lblResumenHistorial == null) return;
        double total = listaHistorial.stream().mapToDouble(NominaItem::getSalarioNeto).sum();
        lblResumenHistorial.setText(
                listaHistorial.size() + " registros | Total pagado: RD$ " + String.format("%.2f", total));
    }

    // ══════════════════════════════════════════════════════════════════════
    // LIMPIAR
    // ══════════════════════════════════════════════════════════════════════
    @FXML
    public void onLimpiarNomina(ActionEvent ignored) {
        listaActual.clear();
        limpiarBusquedaEmpleado();
        if (dpFechaPagoNomina  != null) dpFechaPagoNomina.setValue(java.time.LocalDate.now());
        if (txtPeriodoDias     != null) txtPeriodoDias.clear();
        if (cmbTipoPagoNomina  != null) cmbTipoPagoNomina.setValue(null);
        if (cmbCuentaNomina    != null) cmbCuentaNomina.setValue(null);
        actualizarTotalesCrear();
    }

    private void limpiarFormularioPago() {
        if (txtIdNominaPagar      != null) txtIdNominaPagar.clear();
        if (lblInfoNominaPagar    != null) lblInfoNominaPagar.setText("");
        if (lblMontoPendientePago != null) lblMontoPendientePago.setText("");
        if (cmbMetodoPagoNom      != null) cmbMetodoPagoNom.setValue(null);
        if (cmbCuentaPago         != null) cmbCuentaPago.setValue(null);
        if (dpFechaPago           != null) dpFechaPago.setValue(java.time.LocalDate.now());
    }

    private void limpiarBusquedaEmpleado() {
        idEmpleadoTemp = -1; nombreEmpTemp = ""; cargoEmpTemp = ""; salarioBaseTemp = 0;
        if (txtIdEmpleadoBuscar  != null) txtIdEmpleadoBuscar.clear();
        if (lblInfoEmpleado      != null) lblInfoEmpleado.setText("");
        if (lblSalarioBaseRef    != null) lblSalarioBaseRef.setText("");
        if (lblNetoCalculado     != null) lblNetoCalculado.setText("");
        if (txtBonificacion      != null) txtBonificacion.clear();
        if (txtComision          != null) txtComision.clear();
        if (txtMontoHorasExtras  != null) txtMontoHorasExtras.clear();
        if (txtDescuento         != null) txtDescuento.clear();
    }

    // ══════════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════════
    private void actualizarTotalesCrear() {
        double total = listaActual.stream().mapToDouble(NominaItem::getSalarioNeto).sum();
        if (lblTotalNomina   != null)
            lblTotalNomina.setText("Total nómina: RD$ " + String.format("%.2f", total));
        if (lblCantEmpleados != null)
            lblCantEmpleados.setText(listaActual.size() + " empleado(s)");
    }

    /** Recalcula y muestra el neto temporal mientras el usuario ajusta los campos */
    private void recalcularNetoTemp() {
        double bonif = parseField(txtBonificacion,    0);
        double comis = parseField(txtComision,         0);
        double horas = parseField(txtMontoHorasExtras, 0);
        double desc  = parseField(txtDescuento,        0);
        double neto  = Math.max(0, salarioBaseTemp + bonif + comis + horas - desc);
        if (lblNetoCalculado != null)
            lblNetoCalculado.setText("Neto calculado: RD$ " + String.format("%.2f", neto));
    }

    @FXML public void onRecalcularNeto(ActionEvent ignored) { recalcularNetoTemp(); }

    private int parsePeriodo() {
        if (txtPeriodoDias == null || txtPeriodoDias.getText().isBlank()) return 30;
        try { return Integer.parseInt(txtPeriodoDias.getText().trim()); }
        catch (NumberFormatException e) { return 30; }
    }

    private double parseField(TextField field, double defVal) {
        if (field == null || field.getText().isBlank()) return defVal;
        try { return Double.parseDouble(field.getText().trim()); }
        catch (NumberFormatException e) { return defVal; }
    }

    /** Si el método es Efectivo → id_cuenta = 0 (sin cuenta). */
    private int resolverId_Cuenta(String metodo) {
        if ("Efectivo".equals(metodo)) return 0;
        if (cmbCuentaNomina != null && cmbCuentaNomina.getValue() != null
                && mapaCuentas.containsKey(cmbCuentaNomina.getValue()))
            return mapaCuentas.get(cmbCuentaNomina.getValue());
        return 0;
    }

    private <T> void aplicarColorEstado(TableColumn<T, String> col) {
        col.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("Pagado".equals(item)
                        ? "-fx-text-fill: #2E7D32; -fx-font-weight: bold;"
                        : "-fx-text-fill: #C62828; -fx-font-weight: bold;");
            }
        });
    }
}