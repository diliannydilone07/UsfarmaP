package com.example.farmaventa.controlador;

import Usuarios.Permisos;
import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.NominaItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;

import javax.swing.JOptionPane;
import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class NominaController {

    Conexion conexion = new Conexion();




    @FXML private Button btnGuardarNomina;
    @FXML private Button btnQuitarEmpleadoNomina;
    @FXML private Button btnRegistrarPagoNomina;
    @FXML private Button btnImprimirReciboNomina;

    @FXML private TextField        txtIdEmpleadoBuscar;
    @FXML private Label            lblInfoEmpleado;
    @FXML private TextField        txtBonificacion;
    @FXML private TextField        txtComision;
    @FXML private TextField        txtMontoHorasExtras;
    @FXML private TextField        txtDescuento;
    @FXML private Label            lblSalarioBaseRef;
    @FXML private Label            lblNetoCalculado;

    @FXML private DatePicker       dpFechaPagoNomina;
    @FXML private TextField        txtPeriodoDias;
    @FXML private ComboBox<String> cmbTipoPagoNomina;
    @FXML private ComboBox<String> cmbCuentaNomina;
    @FXML private Label            lblTotalNomina;
    @FXML private Label            lblCantEmpleados;

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

    @FXML private TextField        txtIdNominaPagar;
    @FXML private Label            lblInfoNominaPagar;
    @FXML private Label            lblMontoPendientePago;
    @FXML private ComboBox<String> cmbMetodoPagoNom;
    @FXML private ComboBox<String> cmbCuentaPago;
    @FXML private DatePicker       dpFechaPago;



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

    private final ObservableList<NominaItem> listaActual     = FXCollections.observableArrayList();
    private final ObservableList<NominaItem> listaPendientes = FXCollections.observableArrayList();
    private final ObservableList<NominaItem> listaHistorial  = FXCollections.observableArrayList();
    private final LinkedHashMap<String, Integer> mapaCuentas = new LinkedHashMap<>();
    private int idCuentaDefault = 1;

    private NominaItem itemSeleccionado = null;

    private final javafx.beans.value.ChangeListener<NominaItem> seleccionPendientesListener =
            (obs, oldVal, newVal) -> {
                itemSeleccionado = newVal;
                if (newVal != null) cargarEnFormularioPago(newVal);
            };

    private int    idEmpleadoTemp  = -1;
    private String nombreEmpTemp   = "";
    private String cargoEmpTemp    = "";
    private double salarioBaseTemp = 0;

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

        Permisos.aplicarBtn(btnGuardarNomina,        Permisos.Accion.REGISTRAR);
        Permisos.aplicarBtn(btnQuitarEmpleadoNomina, Permisos.Accion.ELIMINAR);
        Permisos.aplicarBtn(btnRegistrarPagoNomina,  Permisos.Accion.REGISTRAR);
        Permisos.aplicarBtn(btnImprimirReciboNomina, Permisos.Accion.REGISTRAR);
    }

    private void cargarCuentas() {
        mapaCuentas.clear();
        String sql = "SELECT id_cuenta, nombre, banco FROM TBL_CUENTA ORDER BY banco, nombre";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean primero = true;
            while (rs.next()) {
                String label = rs.getString("banco") + " - " + rs.getString("nombre");
                int    id    = rs.getInt("id_cuenta");
                mapaCuentas.put(label, id);
                if (primero) { idCuentaDefault = id; primero = false; }
                if (cmbCuentaNomina != null) cmbCuentaNomina.getItems().add(label);
                if (cmbCuentaPago   != null) cmbCuentaPago.getItems().add(label);
            }
        } catch (SQLException e) {
            System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING, "Sin cuentas", e);
        }
    }

    private void configurarCombos() {
        String[] metodos = {"Efectivo", "Transferencia", "Tarjeta", "Cheque"};
        if (cmbTipoPagoNomina != null) cmbTipoPagoNomina.getItems().addAll(metodos);
        if (cmbMetodoPagoNom  != null) cmbMetodoPagoNom.getItems().addAll(metodos);
        if (cmbFiltroPago != null) {
            cmbFiltroPago.getItems().addAll("Todos", "Pendiente", "Pagado");
            cmbFiltroPago.setValue("Todos");
            cmbFiltroPago.setOnAction(e -> filtrarPendientes());
        }
    }



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
        tablaNomina.getSelectionModel().selectedItemProperty().addListener(
                (obs, o, n) -> { if (n != null) precargarAjuste(n); });
    }

    private void precargarAjuste(NominaItem item) {
        idEmpleadoTemp  = item.getIdEmpleado();
        nombreEmpTemp   = item.getNombreEmpleado();
        cargoEmpTemp    = item.getCargo();
        salarioBaseTemp = item.getSalarioBase();
        if (txtIdEmpleadoBuscar != null) txtIdEmpleadoBuscar.setText(String.valueOf(item.getIdEmpleado()));
        if (lblInfoEmpleado     != null) lblInfoEmpleado.setText("✔ " + item.getNombreEmpleado() + " | " + item.getCargo());
        mostrarLabelContenido(lblSalarioBaseRef, "Salario base: RD$ " + String.format("%.2f", item.getSalarioBase()));
        if (txtBonificacion     != null) txtBonificacion.setText(String.format("%.2f", item.getBonificacion()));
        if (txtComision         != null) txtComision.setText(String.format("%.2f", item.getComisionVenta()));
        if (txtMontoHorasExtras != null) txtMontoHorasExtras.setText(String.format("%.2f", item.getHorasExtras()));
        if (txtDescuento        != null) txtDescuento.setText(String.format("%.2f", item.getDescuento()));
        recalcularNetoTemp();
    }



    @FXML
    public void onBuscarEmpleado(ActionEvent ignored) {
        if (txtIdEmpleadoBuscar == null || txtIdEmpleadoBuscar.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Ingresa el ID del empleado.",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE); return;
        }
        String sql =
                "SELECT e.id_empleado, p.nombre + ' ' + p.apellido AS nombre_completo, " +
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

            if (lblInfoEmpleado != null) lblInfoEmpleado.setText("✔ " + nombreEmpTemp + " | " + cargoEmpTemp);
            mostrarLabelContenido(lblSalarioBaseRef, "Salario base: RD$ " + String.format("%.2f", salarioBaseTemp));

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



    @FXML
    public void onAgregarEmpleadoNomina(ActionEvent ignored) {
        if (idEmpleadoTemp == -1) {
            JOptionPane.showMessageDialog(null, "Primero busca un empleado.",
                    "Sin empleado", JOptionPane.WARNING_MESSAGE); return;
        }
        for (NominaItem n : listaActual) {
            if (n.getIdEmpleado() == idEmpleadoTemp) {
                JOptionPane.showMessageDialog(null, "El empleado " + nombreEmpTemp + " ya está en la lista.",
                        "Duplicado", JOptionPane.WARNING_MESSAGE); return;
            }
        }
        double bonif = parseField(txtBonificacion,    0);
        double comis = parseField(txtComision,         0);
        double horas = parseField(txtMontoHorasExtras, 0);
        double desc  = parseField(txtDescuento,        0);
        double neto  = Math.max(0, salarioBaseTemp + bonif + comis + horas - desc);

        listaActual.add(new NominaItem(
                0, idEmpleadoTemp, nombreEmpTemp, cargoEmpTemp,
                salarioBaseTemp, bonif, comis, horas, desc,
                Math.round(neto * 100.0) / 100.0,
                "", "", "Pendiente", 0, parsePeriodo()));
        actualizarTotalesCrear();
        limpiarBusquedaEmpleado();
    }

    @FXML
    public void onActualizarAjuste(ActionEvent ignored) {
        NominaItem sel = tablaNomina.getSelectionModel().getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(null, "Selecciona un empleado de la tabla.",
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



    @FXML
    public void onGuardarNomina(ActionEvent ignored) {
        if (listaActual.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Agrega al menos un empleado.",
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

        int idCuenta = resolverIdCuenta(cmbCuentaNomina, cmbTipoPagoNomina.getValue());
        if (idCuenta <= 0) {
            JOptionPane.showMessageDialog(null,
                    "Selecciona una cuenta bancaria (requerida por la base de datos).",
                    "Cuenta requerida", JOptionPane.WARNING_MESSAGE); return;
        }

        int    periodo  = parsePeriodo();
        String tipoPago = cmbTipoPagoNomina.getValue();
        double total    = listaActual.stream().mapToDouble(NominaItem::getSalarioNeto).sum();

        try (Connection con = conexion.establecerConexion()) {
            int guardados = 0;
            for (NominaItem item : listaActual) {
                PreparedStatement psN = con.prepareStatement(
                        "INSERT INTO TBL_NOMINA " +
                                "(fecha_pago, periodo, id_cuenta, monto_total_nomina, tipo_pago, estado) " +
                                "VALUES (?, ?, ?, ?, ?, 'Pendiente')", Statement.RETURN_GENERATED_KEYS);
                psN.setDate(1,   java.sql.Date.valueOf(dpFechaPagoNomina.getValue()));
                psN.setInt(2,    periodo);
                psN.setInt(3,    idCuenta);
                psN.setDouble(4, item.getSalarioNeto());
                psN.setString(5, tipoPago);
                psN.executeUpdate();

                int idNomina = -1;
                try (ResultSet keys = psN.getGeneratedKeys()) {
                    if (keys.next()) idNomina = keys.getInt(1);
                }

                PreparedStatement psD = con.prepareStatement(
                        "INSERT INTO TBL_DETALLE_NOMINA " +
                                "(id_nomina, id_empleado, bonificacion, comision_venta, " +
                                " horas_extras, monto_horas_extras, descuento, salario_neto, estado) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'Pendiente')");
                psD.setInt(1,    idNomina);
                psD.setInt(2,    item.getIdEmpleado());
                psD.setDouble(3, item.getBonificacion());
                psD.setDouble(4, item.getComisionVenta());
                psD.setTime(5,   java.sql.Time.valueOf("00:00:00"));
                psD.setDouble(6, item.getHorasExtras());
                psD.setDouble(7, item.getDescuento());
                psD.setDouble(8, item.getSalarioNeto());
                psD.executeUpdate();

                item.setIdNomina(idNomina);
                item.setPeriodo(periodo);
                guardados++;
            }

            JOptionPane.showMessageDialog(null,
                    "✔ Nómina guardada.\nEmpleados: " + guardados +
                            "\nTotal: RD$ " + String.format("%.2f", total) +
                            "\nPeríodo: " + periodo + " días\nMétodo: " + tipoPago,
                    "Nómina guardada", JOptionPane.INFORMATION_MESSAGE);

            cargarNominasPendientes();
            cargarHistorial();
            onLimpiarNomina(null);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al guardar: " + e.getMessage(),
                    "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }




    private void configurarTablaPendientes() {
        if (tablaPendientes == null) return;
        colPagEmp.setCellValueFactory(c -> c.getValue().idNominaProperty());
        colPagNombre.setCellValueFactory(c -> c.getValue().nombreEmpleadoProperty());
        colPagCargo.setCellValueFactory(c -> c.getValue().cargoProperty());
        colPagFecha.setCellValueFactory(c -> c.getValue().fechaPagoProperty());
        colPagNeto.setCellValueFactory(c -> c.getValue().salarioNetoProperty());
        colPagTipo.setCellValueFactory(c -> c.getValue().tipoPagoProperty());
        colPagEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
        aplicarColorEstado(colPagEstado);
        tablaPendientes.setItems(listaPendientes);
        tablaPendientes.getSelectionModel().selectedItemProperty().addListener(seleccionPendientesListener);
    }

    private void cargarNominasPendientes() {
        listaPendientes.clear();

        String sql =
                "SELECT n.id_nomina, n.fecha_pago, n.periodo, n.monto_total_nomina, n.tipo_pago, " +
                        "       d.id_empleado, d.bonificacion, d.comision_venta, d.monto_horas_extras, " +
                        "       d.descuento, d.salario_neto, d.estado AS estado_detalle, " +
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

                String estadoDetalle = rs.getString("estado_detalle");
                if (estadoDetalle == null || estadoDetalle.isBlank()) estadoDetalle = "Pendiente";

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
                        estadoDetalle, 0, rs.getInt("periodo")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar nóminas: " + e.getMessage());
        }

        String busqueda = (txtBuscarNominaPago != null) ? txtBuscarNominaPago.getText().trim() : "";
        if (busqueda.isEmpty()) {
            tablaPendientes.setItems(listaPendientes);
            tablaPendientes.getSelectionModel().selectedItemProperty()
                    .removeListener(seleccionPendientesListener);
            tablaPendientes.getSelectionModel().selectedItemProperty()
                    .addListener(seleccionPendientesListener);
        } else {
            filtrarPendientes();
        }
    }

    private void filtrarPendientes() {
        String filtro   = (cmbFiltroPago != null && cmbFiltroPago.getValue() != null)
                ? cmbFiltroPago.getValue() : "Todos";
        String busqueda = (txtBuscarNominaPago != null)
                ? txtBuscarNominaPago.getText().trim().toLowerCase() : "";
        boolean esSoloNumero = !busqueda.isEmpty() && busqueda.matches("\\d+");

        ObservableList<NominaItem> filtrada = FXCollections.observableArrayList();
        for (NominaItem n : listaPendientes) {
            boolean coincideEstado = "Todos".equals(filtro) || n.getEstado().equals(filtro);
            boolean coincideTexto;
            if (busqueda.isEmpty()) {
                coincideTexto = true;
            } else if (esSoloNumero) {
                coincideTexto = String.valueOf(n.getIdNomina()).equals(busqueda);
            } else {
                coincideTexto = n.getNombreEmpleado().toLowerCase().contains(busqueda);
            }
            if (coincideEstado && coincideTexto) filtrada.add(n);
        }

        tablaPendientes.setItems(filtrada);
        tablaPendientes.getSelectionModel().selectedItemProperty()
                .removeListener(seleccionPendientesListener);
        tablaPendientes.getSelectionModel().selectedItemProperty()
                .addListener(seleccionPendientesListener);
    }

    @FXML
    public void onBuscarNominaPago(ActionEvent ignored) {
        if (txtBuscarNominaPago != null && txtBuscarNominaPago.getText().isBlank()) {
            tablaPendientes.setItems(listaPendientes);
            tablaPendientes.getSelectionModel().selectedItemProperty()
                    .removeListener(seleccionPendientesListener);
            tablaPendientes.getSelectionModel().selectedItemProperty()
                    .addListener(seleccionPendientesListener);
        } else {
            filtrarPendientes();
        }
    }

    @FXML
    public void onRefrescarPendientes(ActionEvent ignored) {
        if (txtBuscarNominaPago != null) txtBuscarNominaPago.clear();
        cargarNominasPendientes();
    }

    private void cargarEnFormularioPago(NominaItem n) {
        if (txtIdNominaPagar != null) txtIdNominaPagar.setText(String.valueOf(n.getIdNomina()));
        mostrarLabelContenido(lblInfoNominaPagar,
                "Nómina #" + n.getIdNomina() + " — " + n.getNombreEmpleado() + " | " + n.getCargo());
        mostrarLabelContenido(lblMontoPendientePago,
                "Monto a pagar: RD$ " + String.format("%.2f", n.getSalarioNeto()));
        if (cmbMetodoPagoNom != null && n.getTipoPago() != null && !n.getTipoPago().isBlank())
            cmbMetodoPagoNom.setValue(n.getTipoPago());
    }





    @FXML
    public void onRegistrarPagoNomina(ActionEvent ignored) {

        if (itemSeleccionado == null) {
            JOptionPane.showMessageDialog(null,
                    "Selecciona un empleado de la tabla para pagar.",
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

        int    idNomina    = itemSeleccionado.getIdNomina();
        int    idEmpleado  = itemSeleccionado.getIdEmpleado();
        String nombre      = itemSeleccionado.getNombreEmpleado();
        double monto       = itemSeleccionado.getSalarioNeto();
        String estadoActual = itemSeleccionado.getEstado();
        String metodo      = cmbMetodoPagoNom.getValue();
        int    idCuenta    = resolverIdCuenta(cmbCuentaPago, metodo);

        if (!"Efectivo".equals(metodo) && idCuenta <= 0) {
            JOptionPane.showMessageDialog(null,
                    "Selecciona la cuenta bancaria para " + metodo + ".",
                    "Cuenta requerida", JOptionPane.WARNING_MESSAGE); return;
        }
        if (idCuenta <= 0) idCuenta = idCuentaDefault;

        if ("Pagado".equals(estadoActual)) {
            int resp = JOptionPane.showConfirmDialog(null,
                    nombre + " ya tiene su nómina #" + idNomina + " marcada como Pagada.\n" +
                            "¿Deseas registrar el pago de todas formas?",
                    "Ya pagado", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (resp != JOptionPane.YES_OPTION) return;
        }

        try (Connection con = conexion.establecerConexion()) {

            PreparedStatement psUpd = con.prepareStatement(
                    "UPDATE TBL_DETALLE_NOMINA " +
                            "SET estado = 'Pagado' " +
                            "WHERE id_nomina = ? AND id_empleado = ?");
            psUpd.setInt(1, idNomina);
            psUpd.setInt(2, idEmpleado);
            int filas = psUpd.executeUpdate();

            if (filas == 0) {
                JOptionPane.showMessageDialog(null,
                        "No se encontró el detalle de nómina para actualizar.",
                        "Error", JOptionPane.ERROR_MESSAGE); return;
            }


            PreparedStatement psCheck = con.prepareStatement(
                    "SELECT COUNT(*) AS total, " +
                            "       SUM(CASE WHEN estado = 'Pagado' THEN 1 ELSE 0 END) AS pagados " +
                            "FROM TBL_DETALLE_NOMINA WHERE id_nomina = ?");
            psCheck.setInt(1, idNomina);
            ResultSet rsCheck = psCheck.executeQuery();
            if (rsCheck.next()) {
                int total   = rsCheck.getInt("total");
                int pagados = rsCheck.getInt("pagados");
                if (total > 0 && total == pagados) {

                    PreparedStatement psNom = con.prepareStatement(
                            "UPDATE TBL_NOMINA SET estado = 'Pagado', " +
                                    "fecha_pago = ?, tipo_pago = ?, id_cuenta = ? " +
                                    "WHERE id_nomina = ?");
                    psNom.setDate(1,   java.sql.Date.valueOf(dpFechaPago.getValue()));
                    psNom.setString(2, metodo);
                    psNom.setInt(3,    idCuenta);
                    psNom.setInt(4,    idNomina);
                    psNom.executeUpdate();
                }
            }

            JOptionPane.showMessageDialog(null,
                    "✔ Pago registrado.\n" +
                            "Empleado: " + nombre + "\n" +
                            "Nómina #" + idNomina + "\n" +
                            "Monto:  RD$ " + String.format("%.2f", monto) + "\n" +
                            "Método: " + metodo + "\nFecha: " + dpFechaPago.getValue(),
                    "Pago registrado", JOptionPane.INFORMATION_MESSAGE);

            itemSeleccionado.setEstado("Pagado");

            for (NominaItem item : listaPendientes) {
                if (item.getIdNomina() == idNomina && item.getIdEmpleado() == idEmpleado) {
                    item.setEstado("Pagado");
                    break;
                }
            }
            tablaPendientes.refresh();
            cargarHistorial();
            limpiarFormularioPago();
            itemSeleccionado = null;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar pago: " + e.getMessage(),
                    "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }



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
                        "       d.estado AS estado_detalle, " +
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
                String estadoDetalle = rs.getString("estado_detalle");
                if (estadoDetalle == null || estadoDetalle.isBlank()) estadoDetalle = "Pendiente";
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
                        estadoDetalle, 0, rs.getInt("periodo")));
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
        boolean esSoloNumero = !busqueda.isEmpty() && busqueda.matches("\\d+");

        cargarHistorial();
        if (!busqueda.isEmpty()) {
            ObservableList<NominaItem> filtrada = FXCollections.observableArrayList();
            for (NominaItem n : listaHistorial) {
                boolean coincide;
                if (esSoloNumero) {
                    coincide = String.valueOf(n.getIdEmpleado()).equals(busqueda)
                            || String.valueOf(n.getIdNomina()).equals(busqueda);
                } else {
                    coincide = n.getNombreEmpleado().toLowerCase().contains(busqueda);
                }
                if (coincide) filtrada.add(n);
            }
            tablaHistorial.setItems(filtrada);
        }
        actualizarResumenHistorial();
    }

    @FXML public void onRefrescarHistorial(ActionEvent ignored) { cargarHistorial(); }

    private void actualizarResumenHistorial() {
        if (lblResumenHistorial == null) return;
        double total = listaHistorial.stream().mapToDouble(NominaItem::getSalarioNeto).sum();
        lblResumenHistorial.setText(
                listaHistorial.size() + " registros | Total pagado: RD$ " + String.format("%.2f", total));
    }



    @FXML
    public void onImprimirReciboNomina(ActionEvent ignored) {
        if (itemSeleccionado == null) {
            JOptionPane.showMessageDialog(null,
                    "Selecciona un empleado de la tabla de nóminas pendientes.",
                    "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        generarReporteReciboNomina();
    }

    private void generarReporteReciboNomina() {
        try {
            InputStream reporte = getClass().getResourceAsStream("/reports/ReciboNomina.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(reporte);
            Map<String, Object> params = new HashMap<>();
            params.put("ID_NOMINA", itemSeleccionado.getIdNomina());
            params.put("ID_EMPLEADO", itemSeleccionado.getIdEmpleado());
            params.put("LOGO_STREAM", getClass().getResourceAsStream("/reports/logusfarmablanco.png"));
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport, params, conexion.establecerConexion());
            JasperViewer.viewReport(jasperPrint, false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error al generar el reporte: " + e.getMessage());
            System.getLogger(getClass().getName())
                    .log(System.Logger.Level.ERROR, e.getMessage(), e);
        }
    }



    @FXML
    public void onLimpiarNomina(ActionEvent ignored) {
        listaActual.clear();
        limpiarBusquedaEmpleado();
        if (dpFechaPagoNomina != null) dpFechaPagoNomina.setValue(java.time.LocalDate.now());
        if (txtPeriodoDias    != null) txtPeriodoDias.clear();
        if (cmbTipoPagoNomina != null) cmbTipoPagoNomina.setValue(null);
        if (cmbCuentaNomina   != null) cmbCuentaNomina.setValue(null);
        actualizarTotalesCrear();
    }

    private void limpiarFormularioPago() {
        if (txtIdNominaPagar != null) txtIdNominaPagar.clear();
        ocultarLabel(lblInfoNominaPagar);
        ocultarLabel(lblMontoPendientePago);
        if (cmbMetodoPagoNom != null) cmbMetodoPagoNom.setValue(null);
        if (cmbCuentaPago    != null) cmbCuentaPago.setValue(null);
        if (dpFechaPago      != null) dpFechaPago.setValue(java.time.LocalDate.now());
    }

    private void limpiarBusquedaEmpleado() {
        idEmpleadoTemp = -1; nombreEmpTemp = ""; cargoEmpTemp = ""; salarioBaseTemp = 0;
        if (txtIdEmpleadoBuscar != null) txtIdEmpleadoBuscar.clear();
        if (lblInfoEmpleado     != null) lblInfoEmpleado.setText("");
        ocultarLabel(lblSalarioBaseRef);
        ocultarLabel(lblNetoCalculado);
        if (txtBonificacion     != null) txtBonificacion.clear();
        if (txtComision         != null) txtComision.clear();
        if (txtMontoHorasExtras != null) txtMontoHorasExtras.clear();
        if (txtDescuento        != null) txtDescuento.clear();
    }



    private void mostrarLabelContenido(Label lbl, String texto) {
        if (lbl == null) return;
        lbl.setText(texto);
        lbl.setManaged(true);
        lbl.setVisible(true);
    }

    private void ocultarLabel(Label lbl) {
        if (lbl == null) return;
        lbl.setText("");
        lbl.setManaged(false);
        lbl.setVisible(false);
    }

    private void actualizarTotalesCrear() {
        double total = listaActual.stream().mapToDouble(NominaItem::getSalarioNeto).sum();
        if (lblTotalNomina   != null) lblTotalNomina.setText("Total nómina: RD$ " + String.format("%.2f", total));
        if (lblCantEmpleados != null) lblCantEmpleados.setText(listaActual.size() + " empleado(s)");
    }

    private void recalcularNetoTemp() {
        double bonif = parseField(txtBonificacion,    0);
        double comis = parseField(txtComision,         0);
        double horas = parseField(txtMontoHorasExtras, 0);
        double desc  = parseField(txtDescuento,        0);
        double neto  = Math.max(0, salarioBaseTemp + bonif + comis + horas - desc);
        mostrarLabelContenido(lblNetoCalculado, "Neto calculado: RD$ " + String.format("%.2f", neto));
    }

    @FXML public void onRecalcularNeto(ActionEvent ignored) { recalcularNetoTemp(); }

    private int resolverIdCuenta(ComboBox<String> combo, String metodo) {
        if (combo != null && combo.getValue() != null
                && mapaCuentas.containsKey(combo.getValue()))
            return mapaCuentas.get(combo.getValue());
        return "Efectivo".equals(metodo) ? idCuentaDefault : 0;
    }

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