package com.example.farmaventa.controlador;

import Usuarios.Permisos;
import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.CuentaPendiente;
import com.example.farmaventa.modelo.CuentaPendiente.TipoCuenta;
import com.example.farmaventa.modelo.EmailService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;

import javax.swing.JOptionPane;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class PagosController {

    Conexion conexion = new Conexion();

    @FXML private Button btnPagarCompra;
    @FXML private Button btnPagarVenta;
    @FXML private Button btnPagarSeguro;
    @FXML private Button btnImprimirReciboPago;
    @FXML private TextField txtIdPagoImprimir;

    @FXML private TabPane tabPanePagos;

    @FXML private TextField                            txtBuscarCompra;
    @FXML private ComboBox<String>                     cmbFiltroCompra;
    @FXML private TableView<CuentaPendiente>           tablaCompras;
    @FXML private TableColumn<CuentaPendiente, Number> colCmpId;
    @FXML private TableColumn<CuentaPendiente, String> colCmpProveedor;
    @FXML private TableColumn<CuentaPendiente, String> colCmpFecha;
    @FXML private TableColumn<CuentaPendiente, Number> colCmpTotal;
    @FXML private TableColumn<CuentaPendiente, Number> colCmpPagado;
    @FXML private TableColumn<CuentaPendiente, Number> colCmpPendiente;
    @FXML private TableColumn<CuentaPendiente, String> colCmpEstado;
    @FXML private Label                                lblResumenCompras;

    @FXML private TextField        txtIdCompraPago;
    @FXML private Label            lblInfoCompraPago;
    @FXML private ComboBox<String> cmbMetodoPagoCompra;
    @FXML private ComboBox<String> cmbCuentaCompra;
    @FXML private DatePicker       dpFechaPagoCompra;
    @FXML private TextField        txtMontoPagoCompra;
    @FXML private Label            lblSaldoCompra;

    @FXML private TableView<HistorialPagoItem>           tablaHistCompra;
    @FXML private TableColumn<HistorialPagoItem, Number> colHCmpId;
    @FXML private TableColumn<HistorialPagoItem, String> colHCmpFecha;
    @FXML private TableColumn<HistorialPagoItem, Number> colHCmpMonto;
    @FXML private TableColumn<HistorialPagoItem, String> colHCmpMetodo;
    @FXML private TableColumn<HistorialPagoItem, String> colHCmpCuenta;
    @FXML private TableColumn<HistorialPagoItem, String> colHCmpEstado;

    @FXML private TextField                            txtBuscarVenta;
    @FXML private ComboBox<String>                     cmbFiltroVenta;
    @FXML private TableView<CuentaPendiente>           tablaVentas;
    @FXML private TableColumn<CuentaPendiente, Number> colVntId;
    @FXML private TableColumn<CuentaPendiente, String> colVntCliente;
    @FXML private TableColumn<CuentaPendiente, String> colVntFecha;
    @FXML private TableColumn<CuentaPendiente, Number> colVntTotal;
    @FXML private TableColumn<CuentaPendiente, Number> colVntPagado;
    @FXML private TableColumn<CuentaPendiente, Number> colVntPendiente;
    @FXML private TableColumn<CuentaPendiente, String> colVntEstado;
    @FXML private Label                                lblResumenVentas;

    @FXML private TextField        txtIdVentaPago;
    @FXML private Label            lblInfoVentaPago;
    @FXML private ComboBox<String> cmbMetodoPagoVenta;
    @FXML private DatePicker       dpFechaPagoVenta;
    @FXML private TextField        txtMontoPagoVenta;
    @FXML private Label            lblSaldoVenta;

    @FXML private TableView<HistorialPagoItem>           tablaHistVenta;
    @FXML private TableColumn<HistorialPagoItem, Number> colHVntId;
    @FXML private TableColumn<HistorialPagoItem, String> colHVntFecha;
    @FXML private TableColumn<HistorialPagoItem, Number> colHVntMonto;
    @FXML private TableColumn<HistorialPagoItem, String> colHVntMetodo;
    @FXML private TableColumn<HistorialPagoItem, String> colHVntEstado;

    @FXML private TextField                            txtBuscarSeguro;
    @FXML private ComboBox<String>                     cmbFiltroSeguro;
    @FXML private TableView<CuentaPendiente>           tablaSeguro;
    @FXML private TableColumn<CuentaPendiente, Number> colSegId;
    @FXML private TableColumn<CuentaPendiente, String> colSegDescripcion;
    @FXML private TableColumn<CuentaPendiente, String> colSegTipo;
    @FXML private TableColumn<CuentaPendiente, String> colSegFecha;
    @FXML private TableColumn<CuentaPendiente, Number> colSegTotal;
    @FXML private TableColumn<CuentaPendiente, Number> colSegPagado;
    @FXML private TableColumn<CuentaPendiente, Number> colSegPendiente;
    @FXML private TableColumn<CuentaPendiente, String> colSegEstado;
    @FXML private Label                                lblResumenSeguro;

    @FXML private TextField        txtIdSeguroPago;
    @FXML private Label            lblInfoSeguroPago;
    @FXML private ComboBox<String> cmbTipoFacturaSeguro;
    @FXML private ComboBox<String> cmbMetodoPagoSeguro;
    @FXML private DatePicker       dpFechaPagoSeguro;
    @FXML private TextField        txtMontoPagoSeguro;
    @FXML private Label            lblSaldoSeguro;

    @FXML private TableView<HistorialPagoItem>           tablaHistSeguro;
    @FXML private TableColumn<HistorialPagoItem, Number> colHSegId;
    @FXML private TableColumn<HistorialPagoItem, String> colHSegFecha;
    @FXML private TableColumn<HistorialPagoItem, Number> colHSegMonto;
    @FXML private TableColumn<HistorialPagoItem, String> colHSegMetodo;
    @FXML private TableColumn<HistorialPagoItem, String> colHSegTipo;
    @FXML private TableColumn<HistorialPagoItem, String> colHSegEstado;

    private final ObservableList<CuentaPendiente>   listaCompras = FXCollections.observableArrayList();
    private final ObservableList<CuentaPendiente>   listaVentas  = FXCollections.observableArrayList();
    private final ObservableList<CuentaPendiente>   listaSeguro  = FXCollections.observableArrayList();
    private final ObservableList<HistorialPagoItem> histCompra   = FXCollections.observableArrayList();
    private final ObservableList<HistorialPagoItem> histVenta    = FXCollections.observableArrayList();
    private final ObservableList<HistorialPagoItem> histSeguro   = FXCollections.observableArrayList();

    private final LinkedHashMap<String, Integer> mapaCuentas = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        cargarCuentas();
        configurarFiltros();
        configurarTablas();
        cargarTodosLosDatos();

        ocultarLabel(lblInfoCompraPago);
        ocultarLabel(lblSaldoCompra);
        ocultarLabel(lblInfoVentaPago);
        ocultarLabel(lblSaldoVenta);
        ocultarLabel(lblInfoSeguroPago);
        ocultarLabel(lblSaldoSeguro);

        Permisos.aplicarBtn(btnPagarCompra, Permisos.Accion.REGISTRAR);
        Permisos.aplicarBtn(btnPagarVenta,  Permisos.Accion.REGISTRAR);
        Permisos.aplicarBtn(btnPagarSeguro, Permisos.Accion.REGISTRAR);
        Permisos.aplicarBtn(btnImprimirReciboPago, Permisos.Accion.REGISTRAR);
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

    private void configurarFiltros() {
        String[] estados = {"Todos", "Pendiente", "Parcial", "Pagado"};

        if (cmbFiltroCompra != null) {
            cmbFiltroCompra.getItems().addAll(estados);
            cmbFiltroCompra.setValue("Todos");
            cmbFiltroCompra.setOnAction(e -> filtrarTabla(tablaCompras, listaCompras,
                    cmbFiltroCompra.getValue(), txtBuscarCompra));
        }
        if (cmbFiltroVenta != null) {
            cmbFiltroVenta.getItems().addAll(estados);
            cmbFiltroVenta.setValue("Todos");
            cmbFiltroVenta.setOnAction(e -> filtrarTabla(tablaVentas, listaVentas,
                    cmbFiltroVenta.getValue(), txtBuscarVenta));
        }
        if (cmbFiltroSeguro != null) {
            cmbFiltroSeguro.getItems().addAll(estados);
            cmbFiltroSeguro.setValue("Todos");
            cmbFiltroSeguro.setOnAction(e -> filtrarTabla(tablaSeguro, listaSeguro,
                    cmbFiltroSeguro.getValue(), txtBuscarSeguro));
        }

        String[] metodos = {"Efectivo", "Transferencia", "Tarjeta", "Cheque"};
        if (cmbMetodoPagoCompra != null) cmbMetodoPagoCompra.getItems().addAll(metodos);
        if (cmbMetodoPagoVenta  != null) cmbMetodoPagoVenta.getItems().addAll(metodos);
        if (cmbMetodoPagoSeguro != null) cmbMetodoPagoSeguro.getItems().addAll(metodos);

        if (cmbTipoFacturaSeguro != null)
            cmbTipoFacturaSeguro.getItems().addAll("Aseguradora", "Cliente");

        if (dpFechaPagoCompra != null) dpFechaPagoCompra.setValue(java.time.LocalDate.now());
        if (dpFechaPagoVenta  != null) dpFechaPagoVenta.setValue(java.time.LocalDate.now());
        if (dpFechaPagoSeguro != null) dpFechaPagoSeguro.setValue(java.time.LocalDate.now());
    }

    private void configurarTablas() {
        if (tablaCompras != null) {
            colCmpId.setCellValueFactory(c -> c.getValue().idRefProperty());
            colCmpProveedor.setCellValueFactory(c -> c.getValue().descripcionProperty());
            colCmpFecha.setCellValueFactory(c -> c.getValue().fechaProperty());
            colCmpTotal.setCellValueFactory(c -> c.getValue().montoTotalProperty());
            colCmpPagado.setCellValueFactory(c -> c.getValue().montoPagadoProperty());
            colCmpPendiente.setCellValueFactory(c -> c.getValue().montoPendienteProperty());
            colCmpEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
            aplicarColorEstado(colCmpEstado);
            tablaCompras.setItems(listaCompras);
            tablaCompras.getSelectionModel().selectedItemProperty().addListener(
                    (obs, o, n) -> { if (n != null) cargarEnFormularioCompra(n); });
        }

        if (tablaVentas != null) {
            colVntId.setCellValueFactory(c -> c.getValue().idRefProperty());
            colVntCliente.setCellValueFactory(c -> c.getValue().descripcionProperty());
            colVntFecha.setCellValueFactory(c -> c.getValue().fechaProperty());
            colVntTotal.setCellValueFactory(c -> c.getValue().montoTotalProperty());
            colVntPagado.setCellValueFactory(c -> c.getValue().montoPagadoProperty());
            colVntPendiente.setCellValueFactory(c -> c.getValue().montoPendienteProperty());
            colVntEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
            aplicarColorEstado(colVntEstado);
            tablaVentas.setItems(listaVentas);
            tablaVentas.getSelectionModel().selectedItemProperty().addListener(
                    (obs, o, n) -> { if (n != null) cargarEnFormularioVenta(n); });
        }

        if (tablaSeguro != null) {
            colSegId.setCellValueFactory(c -> c.getValue().idRefProperty());
            colSegDescripcion.setCellValueFactory(c -> c.getValue().descripcionProperty());
            colSegTipo.setCellValueFactory(c -> c.getValue().tipoProperty());
            colSegFecha.setCellValueFactory(c -> c.getValue().fechaProperty());
            colSegTotal.setCellValueFactory(c -> c.getValue().montoTotalProperty());
            colSegPagado.setCellValueFactory(c -> c.getValue().montoPagadoProperty());
            colSegPendiente.setCellValueFactory(c -> c.getValue().montoPendienteProperty());
            colSegEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
            aplicarColorEstado(colSegEstado);
            tablaSeguro.setItems(listaSeguro);
            tablaSeguro.getSelectionModel().selectedItemProperty().addListener(
                    (obs, o, n) -> { if (n != null) cargarEnFormularioSeguro(n); });
        }

        configurarHistorialCompra();
        configurarHistorialVenta();
        configurarHistorialSeguro();
    }

    private void configurarHistorialCompra() {
        if (tablaHistCompra == null) return;
        colHCmpId.setCellValueFactory(c -> c.getValue().idPagoProperty());
        colHCmpFecha.setCellValueFactory(c -> c.getValue().fechaProperty());
        colHCmpMonto.setCellValueFactory(c -> c.getValue().montoProperty());
        colHCmpMetodo.setCellValueFactory(c -> c.getValue().metodoPagoProperty());
        colHCmpCuenta.setCellValueFactory(c -> c.getValue().cuentaProperty());
        colHCmpEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
        tablaHistCompra.setItems(histCompra);
    }

    private void configurarHistorialVenta() {
        if (tablaHistVenta == null) return;
        colHVntId.setCellValueFactory(c -> c.getValue().idPagoProperty());
        colHVntFecha.setCellValueFactory(c -> c.getValue().fechaProperty());
        colHVntMonto.setCellValueFactory(c -> c.getValue().montoProperty());
        colHVntMetodo.setCellValueFactory(c -> c.getValue().metodoPagoProperty());
        colHVntEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
        tablaHistVenta.setItems(histVenta);
    }

    private void configurarHistorialSeguro() {
        if (tablaHistSeguro == null) return;
        colHSegId.setCellValueFactory(c -> c.getValue().idPagoProperty());
        colHSegFecha.setCellValueFactory(c -> c.getValue().fechaProperty());
        colHSegMonto.setCellValueFactory(c -> c.getValue().montoProperty());
        colHSegMetodo.setCellValueFactory(c -> c.getValue().metodoPagoProperty());
        colHSegTipo.setCellValueFactory(c -> c.getValue().tipoProperty());
        colHSegEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
        tablaHistSeguro.setItems(histSeguro);
    }

    private void cargarCuentas() {
        mapaCuentas.clear();
        String sql = "SELECT id_cuenta, nombre, banco FROM tbl_CUENTA ORDER BY banco, nombre";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String label = rs.getString("banco") + " - " + rs.getString("nombre");
                mapaCuentas.put(label, rs.getInt("id_cuenta"));
                if (cmbCuentaCompra != null) cmbCuentaCompra.getItems().add(label);
            }
        } catch (SQLException e) {
            System.getLogger(getClass().getName())
                    .log(System.Logger.Level.WARNING, "No se pudieron cargar cuentas", e);
        }
    }

    private void cargarTodosLosDatos() {
        cargarComprasPendientes();
        cargarVentasPendientes();
        cargarSegurosPendientes();
    }

    private void cargarComprasPendientes() {
        listaCompras.clear();

        String sqlSync =
                "UPDATE tbl_COMPRA " +
                        "SET monto_pendiente = " +
                        "    CASE " +
                        "        WHEN monto_total - ISNULL((" +
                        "            SELECT SUM(pg.monto_pago) " +
                        "            FROM TBL_PAGO_COMPRA pc " +
                        "            JOIN TBL_PAGO pg ON pg.id_pago = pc.id_pago " +
                        "            WHERE pc.id_compra = tbl_COMPRA.id_compra " +
                        "              AND pg.estado_pago = 1" +
                        "        ), 0) < 0 THEN 0 " +
                        "        ELSE monto_total - ISNULL((" +
                        "            SELECT SUM(pg.monto_pago) " +
                        "            FROM TBL_PAGO_COMPRA pc " +
                        "            JOIN TBL_PAGO pg ON pg.id_pago = pc.id_pago " +
                        "            WHERE pc.id_compra = tbl_COMPRA.id_compra " +
                        "              AND pg.estado_pago = 1" +
                        "        ), 0) " +
                        "    END";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sqlSync)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            System.getLogger(getClass().getName())
                    .log(System.Logger.Level.WARNING, "No se pudo sincronizar monto_pendiente compras", e);
        }

        String sql =
                "SELECT c.id_compra, pr.nombre AS proveedor, " +
                        "CONVERT(VARCHAR(10), c.fecha_transaccion, 120) AS fecha, " +
                        "c.monto_total, c.monto_pendiente, c.tipo_compra " +
                        "FROM tbl_COMPRA c " +
                        "JOIN tbl_PEDIDO_C pc  ON pc.id_pedido_c  = c.id_pedido_c " +
                        "JOIN tbl_PROVEEDOR pr ON pr.id_proveedor = pc.id_proveedor " +
                        "ORDER BY c.monto_pendiente DESC, c.fecha_transaccion DESC";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                listaCompras.add(new CuentaPendiente(
                        rs.getInt("id_compra"),
                        rs.getString("proveedor"),
                        rs.getString("fecha"),
                        rs.getDouble("monto_total"),
                        rs.getDouble("monto_pendiente"),
                        rs.getString("tipo_compra"),
                        TipoCuenta.COMPRA));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar compras: " + e.getMessage());
        }
        actualizarResumen(listaCompras, lblResumenCompras, "pagar");
    }

    private void cargarVentasPendientes() {
        listaVentas.clear();

        String sqlSync =
                "UPDATE TBL_VENTA " +
                        "SET monto_pendiente = " +
                        "    CASE " +
                        "        WHEN monto_total - ISNULL((" +
                        "            SELECT SUM(pg.monto_pago) " +
                        "            FROM TBL_PAGO_VENTA pv " +
                        "            JOIN TBL_PAGO pg ON pg.id_pago = pv.id_pago " +
                        "            WHERE pv.id_venta = TBL_VENTA.id_venta " +
                        "              AND pg.estado_pago = 1" +
                        "        ), 0) < 0 THEN 0 " +
                        "        ELSE monto_total - ISNULL((" +
                        "            SELECT SUM(pg.monto_pago) " +
                        "            FROM TBL_PAGO_VENTA pv " +
                        "            JOIN TBL_PAGO pg ON pg.id_pago = pv.id_pago " +
                        "            WHERE pv.id_venta = TBL_VENTA.id_venta " +
                        "              AND pg.estado_pago = 1" +
                        "        ), 0) " +
                        "    END";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sqlSync)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            System.getLogger(getClass().getName())
                    .log(System.Logger.Level.WARNING, "No se pudo sincronizar monto_pendiente ventas", e);
        }

        String sql =
                "SELECT v.id_venta, p.nombre + ' ' + p.apellido AS cliente, " +
                        "CONVERT(VARCHAR(10), v.fecha_transaccion, 120) AS fecha, " +
                        "v.monto_total, v.monto_pendiente, v.tipo_venta " +
                        "FROM TBL_VENTA v " +
                        "JOIN TBL_CLIENTE cl ON cl.id_cliente = v.id_cliente " +
                        "JOIN TBL_PERSONA p  ON p.id_persona  = cl.id_persona " +
                        "ORDER BY v.monto_pendiente DESC, v.fecha_transaccion DESC";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                listaVentas.add(new CuentaPendiente(
                        rs.getInt("id_venta"),
                        rs.getString("cliente"),
                        rs.getString("fecha"),
                        rs.getDouble("monto_total"),
                        rs.getDouble("monto_pendiente"),
                        rs.getString("tipo_venta"),
                        TipoCuenta.VENTA));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar ventas: " + e.getMessage());
        }
        actualizarResumen(listaVentas, lblResumenVentas, "cobrar (clientes)");
    }

    private void cargarSegurosPendientes() {
        listaSeguro.clear();

        String sql =
                "SELECT " +
                        "  vs.id_ventaseguro, " +
                        "  p.nombre + ' ' + p.apellido  AS cliente, " +
                        "  a.nombre                      AS aseguradora, " +
                        "  CONVERT(VARCHAR(10), vs.fecha_transaccion, 120) AS fecha, " +
                        "  vs.monto_total, " +
                        "  vs.monto_pendiente_cliente             AS monto_cliente, " +
                        "  vs.condicion, " +
                        "  ISNULL((" +
                        "      SELECT SUM(vps.precio_unitario * vps.cantidad * vps.porcentaje_cobertura / 100.0)" +
                        "      FROM   TBL_VENTA_PRODUCTO_SEGURO vps" +
                        "      WHERE  vps.id_ventaseguro = vs.id_ventaseguro" +
                        "  ), 0) AS monto_aseg, " +
                        "  ISNULL((" +
                        "      SELECT SUM(pg.monto_pago)" +
                        "      FROM   TBL_PAGO_ASEGURADORA_SEGURO pas" +
                        "      JOIN   TBL_PAGO pg ON pg.id_pago = pas.id_pago" +
                        "      WHERE  pas.id_ventaseguro = vs.id_ventaseguro" +
                        "  ), 0) AS pagado_aseg, " +
                        "  ISNULL((" +
                        "      SELECT SUM(pg.monto_pago)" +
                        "      FROM   TBL_PAGO_CLIENTE_SEGURO pcs" +
                        "      JOIN   TBL_PAGO pg ON pg.id_pago = pcs.id_pago" +
                        "      WHERE  pcs.id_ventaseguro = vs.id_ventaseguro" +
                        "  ), 0) AS pagado_cli " +
                        "FROM TBL_VENTA_SEGURO vs " +
                        "JOIN TBL_CLIENTE      cl ON cl.id_cliente   = vs.id_cliente " +
                        "JOIN TBL_PERSONA      p  ON p.id_persona    = cl.id_persona " +
                        "JOIN TBL_SEGURO_MEDICO sm ON sm.id_seguro   = cl.id_seguro " +
                        "JOIN TBL_ASEGURADORA  a  ON a.id_aseguradora = sm.id_aseguradora " +
                        "ORDER BY vs.fecha_transaccion DESC";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int    idVS         = rs.getInt("id_ventaseguro");
                String fecha        = rs.getString("fecha");
                String condicion    = rs.getString("condicion");

                double montoAseg     = rs.getDouble("monto_aseg");
                double pagadoAseg    = rs.getDouble("pagado_aseg");
                double pendienteAseg = Math.max(0, montoAseg - pagadoAseg);

                double montoCli      = rs.getDouble("monto_cliente");
                double pagadoCli     = rs.getDouble("pagado_cli");
                double pendienteCli  = Math.max(0, montoCli - pagadoCli);

                listaSeguro.add(new CuentaPendiente(
                        idVS,
                        rs.getString("aseguradora") + (condicion != null && !condicion.isBlank()
                                ? " | " + condicion : ""),
                        fecha,
                        montoAseg,
                        pendienteAseg,
                        "Aseguradora",
                        TipoCuenta.SEGURO_ASEGURADORA));

                listaSeguro.add(new CuentaPendiente(
                        idVS,
                        rs.getString("cliente") + (condicion != null && !condicion.isBlank()
                                ? " | " + condicion : ""),
                        fecha,
                        montoCli,
                        pendienteCli,
                        "Cliente",
                        TipoCuenta.SEGURO_CLIENTE));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar seguros: " + e.getMessage());
        }
        actualizarResumen(listaSeguro, lblResumenSeguro, "cobrar (seguros)");
    }

    private void cargarEnFormularioCompra(CuentaPendiente c) {
        if (txtIdCompraPago != null) txtIdCompraPago.setText(String.valueOf(c.getIdRef()));

        mostrarLabelContenido(lblInfoCompraPago,
                "Compra #" + c.getIdRef() + " — " + c.getDescripcion()
                        + " | Pendiente: RD$ " + String.format("%.2f", c.getMontoPendiente()));
        if (lblInfoCompraPago != null)
            lblInfoCompraPago.setStyle(c.getMontoPendiente() <= 0
                    ? "-fx-text-fill: #2E7D32; -fx-font-size: 11px;"
                    : "-fx-text-fill: #C62828; -fx-font-size: 11px;");

        mostrarLabelContenido(lblSaldoCompra,
                "Saldo pendiente: RD$ " + String.format("%.2f", c.getMontoPendiente()));

        if (txtMontoPagoCompra != null)
            txtMontoPagoCompra.setText(String.format("%.2f", c.getMontoPendiente()));
        cargarHistorialCompra(c.getIdRef());
    }

    private void cargarEnFormularioVenta(CuentaPendiente c) {
        if (txtIdVentaPago != null) txtIdVentaPago.setText(String.valueOf(c.getIdRef()));

        mostrarLabelContenido(lblInfoVentaPago,
                "Venta #" + c.getIdRef() + " — " + c.getDescripcion()
                        + " | Pendiente: RD$ " + String.format("%.2f", c.getMontoPendiente()));
        if (lblInfoVentaPago != null)
            lblInfoVentaPago.setStyle(c.getMontoPendiente() <= 0
                    ? "-fx-text-fill: #2E7D32; -fx-font-size: 11px;"
                    : "-fx-text-fill: #C62828; -fx-font-size: 11px;");

        mostrarLabelContenido(lblSaldoVenta,
                "Saldo pendiente: RD$ " + String.format("%.2f", c.getMontoPendiente()));

        if (txtMontoPagoVenta != null)
            txtMontoPagoVenta.setText(String.format("%.2f", c.getMontoPendiente()));
        cargarHistorialVenta(c.getIdRef());
    }

    private void cargarEnFormularioSeguro(CuentaPendiente c) {
        if (txtIdSeguroPago      != null) txtIdSeguroPago.setText(String.valueOf(c.getIdRef()));
        if (cmbTipoFacturaSeguro != null) cmbTipoFacturaSeguro.setValue(c.getTipo());

        mostrarLabelContenido(lblInfoSeguroPago,
                "VentaSeguro #" + c.getIdRef()
                        + " [" + c.getTipo() + "] — " + c.getDescripcion()
                        + " | Pendiente: RD$ " + String.format("%.2f", c.getMontoPendiente()));
        if (lblInfoSeguroPago != null)
            lblInfoSeguroPago.setStyle(c.getMontoPendiente() <= 0
                    ? "-fx-text-fill: #2E7D32; -fx-font-size: 11px;"
                    : "-fx-text-fill: #C62828; -fx-font-size: 11px;");

        mostrarLabelContenido(lblSaldoSeguro,
                "Saldo pendiente: RD$ " + String.format("%.2f", c.getMontoPendiente()));

        if (txtMontoPagoSeguro != null)
            txtMontoPagoSeguro.setText(String.format("%.2f", c.getMontoPendiente()));
        cargarHistorialSeguro(c.getIdRef());
    }

    private void cargarHistorialCompra(int idCompra) {
        histCompra.clear();
        String sql =
                "SELECT pc.id_pagocompra, " +
                        "CONVERT(VARCHAR(10), pg.fecha_pago, 120) AS fecha, " +
                        "pg.monto_pago, pg.metodo_pago, pg.estado_pago, " +
                        "cu.banco + ' - ' + cu.nombre AS cuenta " +
                        "FROM TBL_PAGO_COMPRA pc " +
                        "JOIN TBL_PAGO    pg ON pg.id_pago   = pc.id_pago " +
                        "JOIN tbl_CUENTA  cu ON cu.id_cuenta = pc.id_cuenta " +
                        "WHERE pc.id_compra = ? ORDER BY pg.fecha_pago DESC";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCompra);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                histCompra.add(new HistorialPagoItem(
                        rs.getInt("id_pagocompra"),
                        rs.getString("fecha"),
                        rs.getDouble("monto_pago"),
                        rs.getString("metodo_pago"),
                        rs.getBoolean("estado_pago") ? "Aplicado" : "Anulado",
                        rs.getString("cuenta"),
                        ""));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar historial: " + e.getMessage());
        }
    }

    private void cargarHistorialVenta(int idVenta) {
        histVenta.clear();
        String sql =
                "SELECT pv.id_pagoventa, " +
                        "CONVERT(VARCHAR(10), pg.fecha_pago, 120) AS fecha, " +
                        "pg.monto_pago, pg.metodo_pago, pg.estado_pago " +
                        "FROM TBL_PAGO_VENTA pv " +
                        "JOIN TBL_PAGO pg ON pg.id_pago = pv.id_pago " +
                        "WHERE pv.id_venta = ? ORDER BY pg.fecha_pago DESC";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                histVenta.add(new HistorialPagoItem(
                        rs.getInt("id_pagoventa"),
                        rs.getString("fecha"),
                        rs.getDouble("monto_pago"),
                        rs.getString("metodo_pago"),
                        rs.getBoolean("estado_pago") ? "Aplicado" : "Anulado",
                        "", ""));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar historial: " + e.getMessage());
        }
    }

    private void cargarHistorialSeguro(int idVentaSeguro) {
        histSeguro.clear();
        String sql =
                "SELECT pas.id_pagoventaseguro, " +
                        "       CONVERT(VARCHAR(10), pg.fecha_pago, 120) AS fecha, " +
                        "       pg.monto_pago, pg.metodo_pago, pg.estado_pago, " +
                        "       'Aseguradora' AS tipo_pago " +
                        "FROM TBL_PAGO_ASEGURADORA_SEGURO pas " +
                        "JOIN TBL_PAGO pg ON pg.id_pago = pas.id_pago " +
                        "WHERE pas.id_ventaseguro = ? " +
                        "UNION ALL " +
                        "SELECT pcs.id_pagoventaseguro, " +
                        "       CONVERT(VARCHAR(10), pg.fecha_pago, 120) AS fecha, " +
                        "       pg.monto_pago, pg.metodo_pago, pg.estado_pago, " +
                        "       'Cliente' AS tipo_pago " +
                        "FROM TBL_PAGO_CLIENTE_SEGURO pcs " +
                        "JOIN TBL_PAGO pg ON pg.id_pago = pcs.id_pago " +
                        "WHERE pcs.id_ventaseguro = ? " +
                        "ORDER BY fecha DESC";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVentaSeguro);
            ps.setInt(2, idVentaSeguro);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                histSeguro.add(new HistorialPagoItem(
                        rs.getInt("id_pagoventaseguro"),
                        rs.getString("fecha"),
                        rs.getDouble("monto_pago"),
                        rs.getString("metodo_pago"),
                        rs.getBoolean("estado_pago") ? "Aplicado" : "Anulado",
                        "",
                        rs.getString("tipo_pago")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar historial: " + e.getMessage());
        }
    }

    @FXML
    public void onRegistrarPagoCompra(ActionEvent ignored) {
        if (txtIdCompraPago == null || txtIdCompraPago.getText().isBlank()) {
            JOptionPane.showMessageDialog(null,
                    "Selecciona una compra de la lista o ingresa el ID.",
                    "Sin selección", JOptionPane.WARNING_MESSAGE); return;
        }
        if (!validarFormularioPago(txtMontoPagoCompra, cmbMetodoPagoCompra, cmbCuentaCompra)) return;

        int    idCompra  = Integer.parseInt(txtIdCompraPago.getText().trim());
        double montoPago = Double.parseDouble(txtMontoPagoCompra.getText().trim());
        int    idCuenta  = mapaCuentas.get(cmbCuentaCompra.getValue());

        try (Connection con = conexion.establecerConexion()) {
            double[] saldo = leerSaldoCompra(con, idCompra);
            if (saldo == null) return;
            double montoTotal = saldo[0], montoPendiente = saldo[1];
            if (!validarMontoPago(montoPago, montoPendiente, montoTotal, txtMontoPagoCompra)) return;

            double nuevoPendiente = Math.max(0.0, Math.round((montoPendiente - montoPago) * 100.0) / 100.0);

            PreparedStatement psPago = con.prepareStatement(
                    "INSERT INTO TBL_PAGO (tipo_pago, fecha_pago, monto_pago, metodo_pago, estado_pago) " +
                            "VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            psPago.setString(1, "Compra");
            psPago.setDate(2, Date.valueOf(dpFechaPagoCompra.getValue()));
            psPago.setDouble(3, montoPago);
            psPago.setString(4, cmbMetodoPagoCompra.getValue());
            psPago.setBoolean(5, true);
            psPago.executeUpdate();

            int idPago = -1;
            try (ResultSet keys = psPago.getGeneratedKeys()) { if (keys.next()) idPago = keys.getInt(1); }

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO TBL_PAGO_COMPRA (id_compra, id_cuenta, id_pago) VALUES (?,?,?)");
            ps.setInt(1, idCompra);
            ps.setInt(2, idCuenta);
            ps.setInt(3, idPago);
            ps.executeUpdate();

            PreparedStatement psUpd = con.prepareStatement(
                    "UPDATE tbl_COMPRA SET monto_pendiente = ? WHERE id_compra = ?");
            psUpd.setDouble(1, nuevoPendiente);
            psUpd.setInt(2, idCompra);
            psUpd.executeUpdate();

            JOptionPane.showMessageDialog(null,
                    "Pago #" + idPago + " registrado.\n" +
                            "Monto pagado:    RD$ " + String.format("%.2f", montoPago) + "\n" +
                            "Nuevo pendiente: RD$ " + String.format("%.2f", nuevoPendiente),
                    "Pago registrado", JOptionPane.INFORMATION_MESSAGE);

            cargarComprasPendientes();
            cargarHistorialCompra(idCompra);
            actualizarInfoLuegoDePago(nuevoPendiente, lblInfoCompraPago, lblSaldoCompra, txtMontoPagoCompra);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar pago: " + e.getMessage(),
                    "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    @FXML
    public void onRegistrarPagoVenta(ActionEvent ignored) {
        if (txtIdVentaPago == null || txtIdVentaPago.getText().isBlank()) {
            JOptionPane.showMessageDialog(null,
                    "Selecciona una venta de la lista o ingresa el ID.",
                    "Sin selección", JOptionPane.WARNING_MESSAGE); return;
        }
        if (!validarFormularioPagoSinCuenta(txtMontoPagoVenta, cmbMetodoPagoVenta)) return;

        int    idVenta   = Integer.parseInt(txtIdVentaPago.getText().trim());
        double montoPago = Double.parseDouble(txtMontoPagoVenta.getText().trim());

        try (Connection con = conexion.establecerConexion()) {
            double[] saldo = leerSaldoVenta(con, idVenta);
            if (saldo == null) return;
            double montoTotal = saldo[0], montoPendiente = saldo[1];
            if (!validarMontoPago(montoPago, montoPendiente, montoTotal, txtMontoPagoVenta)) return;

            double nuevoPendiente = Math.max(0.0, Math.round((montoPendiente - montoPago) * 100.0) / 100.0);

            PreparedStatement psPago = con.prepareStatement(
                    "INSERT INTO TBL_PAGO (tipo_pago, fecha_pago, monto_pago, metodo_pago, estado_pago) " +
                            "VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            psPago.setString(1, "Venta");
            psPago.setDate(2, Date.valueOf(dpFechaPagoVenta.getValue()));
            psPago.setDouble(3, montoPago);
            psPago.setString(4, cmbMetodoPagoVenta.getValue());
            psPago.setBoolean(5, true);
            psPago.executeUpdate();

            int idPago = -1;
            try (ResultSet keys = psPago.getGeneratedKeys()) { if (keys.next()) idPago = keys.getInt(1); }

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO TBL_PAGO_VENTA (id_venta, id_pago) VALUES (?,?)");
            ps.setInt(1, idVenta);
            ps.setInt(2, idPago);
            ps.executeUpdate();

            PreparedStatement psUpd = con.prepareStatement(
                    "UPDATE TBL_VENTA SET monto_pendiente = ? WHERE id_venta = ?");
            psUpd.setDouble(1, nuevoPendiente);
            psUpd.setInt(2, idVenta);
            psUpd.executeUpdate();

            JOptionPane.showMessageDialog(null,
                    "Pago #" + idPago + " registrado.\n" +
                            "Monto pagado:    RD$ " + String.format("%.2f", montoPago) + "\n" +
                            "Nuevo pendiente: RD$ " + String.format("%.2f", nuevoPendiente),
                    "Pago registrado", JOptionPane.INFORMATION_MESSAGE);

            cargarVentasPendientes();
            cargarHistorialVenta(idVenta);
            actualizarInfoLuegoDePago(nuevoPendiente, lblInfoVentaPago, lblSaldoVenta, txtMontoPagoVenta);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar pago: " + e.getMessage(),
                    "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    @FXML
    public void onRegistrarPagoSeguro(ActionEvent ignored) {
        if (txtIdSeguroPago == null || txtIdSeguroPago.getText().isBlank()) {
            JOptionPane.showMessageDialog(null,
                    "Selecciona una cuenta de la lista o ingresa el ID.",
                    "Sin selección", JOptionPane.WARNING_MESSAGE); return;
        }
        if (cmbTipoFacturaSeguro == null || cmbTipoFacturaSeguro.getValue() == null) {
            JOptionPane.showMessageDialog(null,
                    "Selecciona si es pago de Aseguradora o Cliente.",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE); return;
        }
        if (!validarFormularioPagoSinCuenta(txtMontoPagoSeguro, cmbMetodoPagoSeguro)) return;

        int    idVS      = Integer.parseInt(txtIdSeguroPago.getText().trim());
        double montoPago = Double.parseDouble(txtMontoPagoSeguro.getText().trim());
        String tipoFact  = cmbTipoFacturaSeguro.getValue();

        try (Connection con = conexion.establecerConexion()) {

            String sqlSaldo =
                    "SELECT vs.monto_pendiente_cliente AS monto_cli, " +
                            "  ISNULL((" +
                            "      SELECT SUM(vps.precio_unitario * vps.cantidad * vps.porcentaje_cobertura / 100.0)" +
                            "      FROM TBL_VENTA_PRODUCTO_SEGURO vps" +
                            "      WHERE vps.id_ventaseguro = vs.id_ventaseguro" +
                            "  ), 0) AS monto_aseg, " +
                            "  ISNULL((" +
                            "      SELECT SUM(pg.monto_pago)" +
                            "      FROM TBL_PAGO_ASEGURADORA_SEGURO pas" +
                            "      JOIN TBL_PAGO pg ON pg.id_pago = pas.id_pago" +
                            "      WHERE pas.id_ventaseguro = vs.id_ventaseguro" +
                            "  ), 0) AS pagado_aseg, " +
                            "  ISNULL((" +
                            "      SELECT SUM(pg.monto_pago)" +
                            "      FROM TBL_PAGO_CLIENTE_SEGURO pcs" +
                            "      JOIN TBL_PAGO pg ON pg.id_pago = pcs.id_pago" +
                            "      WHERE pcs.id_ventaseguro = vs.id_ventaseguro" +
                            "  ), 0) AS pagado_cli " +
                            "FROM TBL_VENTA_SEGURO vs WHERE vs.id_ventaseguro = ?";

            PreparedStatement psCheck = con.prepareStatement(sqlSaldo);
            psCheck.setInt(1, idVS);
            ResultSet rsCheck = psCheck.executeQuery();

            if (!rsCheck.next()) {
                JOptionPane.showMessageDialog(null,
                        "Venta con seguro #" + idVS + " no encontrada.",
                        "No encontrado", JOptionPane.ERROR_MESSAGE); return;
            }

            double montoAseg     = rsCheck.getDouble("monto_aseg");
            double montoCli      = rsCheck.getDouble("monto_cli");
            double pagadoAseg    = rsCheck.getDouble("pagado_aseg");
            double pagadoCli     = rsCheck.getDouble("pagado_cli");
            double pendienteAseg = Math.max(0, montoAseg - pagadoAseg);
            double pendienteCli  = Math.max(0, montoCli  - pagadoCli);

            double pendienteTarget = tipoFact.equals("Aseguradora") ? pendienteAseg : pendienteCli;
            double totalTarget     = tipoFact.equals("Aseguradora") ? montoAseg     : montoCli;

            if (pendienteTarget <= 0) {
                JOptionPane.showMessageDialog(null,
                        "La factura de " + tipoFact + " ya está completamente pagada.\n" +
                                "Total: RD$ " + String.format("%.2f", totalTarget),
                        "Sin deuda pendiente", JOptionPane.INFORMATION_MESSAGE); return;
            }
            if (montoPago > pendienteTarget + 0.001) {
                JOptionPane.showMessageDialog(null,
                        "⚠ El monto supera el saldo pendiente de " + tipoFact + ".\n\n" +
                                "Monto ingresado:  RD$ " + String.format("%.2f", montoPago) + "\n" +
                                "Máximo permitido: RD$ " + String.format("%.2f", pendienteTarget),
                        "Monto excedido", JOptionPane.WARNING_MESSAGE);
                if (txtMontoPagoSeguro != null) {
                    txtMontoPagoSeguro.setText(String.format("%.2f", pendienteTarget));
                    txtMontoPagoSeguro.requestFocus();
                    txtMontoPagoSeguro.selectAll();
                }
                return;
            }

            PreparedStatement psPago = con.prepareStatement(
                    "INSERT INTO TBL_PAGO (tipo_pago, fecha_pago, monto_pago, metodo_pago, estado_pago) " +
                            "VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            psPago.setString(1, tipoFact);
            psPago.setDate(2, Date.valueOf(dpFechaPagoSeguro.getValue()));
            psPago.setDouble(3, montoPago);
            psPago.setString(4, cmbMetodoPagoSeguro.getValue());
            psPago.setBoolean(5, true);
            psPago.executeUpdate();

            int idPago = -1;
            try (ResultSet keys = psPago.getGeneratedKeys()) {
                if (keys.next()) idPago = keys.getInt(1);
            }
            if (idPago == -1) {
                JOptionPane.showMessageDialog(null,
                        "No se pudo obtener el ID del pago generado.",
                        "Error interno", JOptionPane.ERROR_MESSAGE); return;
            }

            String tablaPuente = tipoFact.equals("Aseguradora")
                    ? "TBL_PAGO_ASEGURADORA_SEGURO"
                    : "TBL_PAGO_CLIENTE_SEGURO";

            PreparedStatement psPuente = con.prepareStatement(
                    "INSERT INTO " + tablaPuente + " (id_ventaseguro, id_pago) VALUES (?, ?)");
            psPuente.setInt(1, idVS);
            psPuente.setInt(2, idPago);
            psPuente.executeUpdate();

            if (tipoFact.equals("Cliente")) {
                double nuevoPendienteCli = Math.max(0.0, Math.round((pendienteCli - montoPago) * 100.0) / 100.0);
                PreparedStatement psUpd = con.prepareStatement(
                        "UPDATE TBL_VENTA_SEGURO SET monto_pendiente = ? WHERE id_ventaseguro = ?");
                psUpd.setDouble(1, nuevoPendienteCli);
                psUpd.setInt(2, idVS);
                psUpd.executeUpdate();
            }

            double nuevoPendiente = Math.max(0.0, Math.round((pendienteTarget - montoPago) * 100.0) / 100.0);

            JOptionPane.showMessageDialog(null,
                    "Pago #" + idPago + " registrado [" + tipoFact + "].\n" +
                            "Monto pagado:    RD$ " + String.format("%.2f", montoPago) + "\n" +
                            "Nuevo pendiente " + tipoFact + ": RD$ " + String.format("%.2f", nuevoPendiente),
                    "Pago registrado", JOptionPane.INFORMATION_MESSAGE);

            cargarSegurosPendientes();
            cargarHistorialSeguro(idVS);

            mostrarLabelContenido(lblSaldoSeguro,
                    "Saldo pendiente " + tipoFact + ": RD$ " + String.format("%.2f", nuevoPendiente));
            if (txtMontoPagoSeguro != null) txtMontoPagoSeguro.clear();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar pago: " + e.getMessage(),
                    "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    @FXML public void onBuscarEnCompras(ActionEvent ignored) {
        filtrarTabla(tablaCompras, listaCompras,
                cmbFiltroCompra != null ? cmbFiltroCompra.getValue() : "Todos", txtBuscarCompra);
    }
    @FXML public void onBuscarEnVentas(ActionEvent ignored) {
        filtrarTabla(tablaVentas, listaVentas,
                cmbFiltroVenta != null ? cmbFiltroVenta.getValue() : "Todos", txtBuscarVenta);
    }
    @FXML public void onBuscarEnSeguros(ActionEvent ignored) {
        filtrarTabla(tablaSeguro, listaSeguro,
                cmbFiltroSeguro != null ? cmbFiltroSeguro.getValue() : "Todos", txtBuscarSeguro);
    }

    private void filtrarTabla(TableView<CuentaPendiente> tabla,
                              ObservableList<CuentaPendiente> listaCompleta,
                              String estado, TextField txtBuscar) {
        String busqueda = (txtBuscar != null) ? txtBuscar.getText().trim().toLowerCase() : "";
        boolean esSoloNumero = !busqueda.isEmpty() && busqueda.matches("\\d+");

        ObservableList<CuentaPendiente> filtrada = FXCollections.observableArrayList();
        for (CuentaPendiente c : listaCompleta) {
            boolean coincideEstado = "Todos".equals(estado) || c.getEstado().equals(estado);
            boolean coincideTexto;
            if (busqueda.isEmpty()) {
                coincideTexto = true;
            } else if (esSoloNumero) {
                coincideTexto = String.valueOf(c.getIdRef()).equals(busqueda);
            } else {
                coincideTexto = c.getDescripcion().toLowerCase().contains(busqueda);
            }
            if (coincideEstado && coincideTexto) filtrada.add(c);
        }
        tabla.setItems(filtrada);
    }

    @FXML public void onRefrescarCompras(ActionEvent ignored)  { cargarComprasPendientes(); }
    @FXML public void onRefrescarVentas(ActionEvent ignored)   { cargarVentasPendientes(); }
    @FXML public void onRefrescarSeguros(ActionEvent ignored)  { cargarSegurosPendientes(); }

    @FXML public void onLimpiarPagoCompra(ActionEvent ignored) {
        limpiarFormulario(txtIdCompraPago, txtMontoPagoCompra, cmbMetodoPagoCompra,
                cmbCuentaCompra, dpFechaPagoCompra, lblInfoCompraPago, lblSaldoCompra);
        histCompra.clear();
    }
    @FXML public void onLimpiarPagoVenta(ActionEvent ignored) {
        limpiarFormulario(txtIdVentaPago, txtMontoPagoVenta, cmbMetodoPagoVenta,
                null, dpFechaPagoVenta, lblInfoVentaPago, lblSaldoVenta);
        histVenta.clear();
    }
    @FXML public void onLimpiarPagoSeguro(ActionEvent ignored) {
        limpiarFormulario(txtIdSeguroPago, txtMontoPagoSeguro, cmbMetodoPagoSeguro,
                null, dpFechaPagoSeguro, lblInfoSeguroPago, lblSaldoSeguro);
        if (cmbTipoFacturaSeguro != null) cmbTipoFacturaSeguro.setValue(null);
        histSeguro.clear();
    }

    private double[] leerSaldoCompra(Connection con, int idCompra) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
                "SELECT monto_total, monto_pendiente FROM tbl_COMPRA WHERE id_compra = ?");
        ps.setInt(1, idCompra);
        ResultSet rs = ps.executeQuery();
        if (!rs.next()) {
            JOptionPane.showMessageDialog(null, "Compra #" + idCompra + " no encontrada.",
                    "No encontrado", JOptionPane.ERROR_MESSAGE); return null;
        }
        return new double[]{rs.getDouble("monto_total"), rs.getDouble("monto_pendiente")};
    }

    private double[] leerSaldoVenta(Connection con, int idVenta) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
                "SELECT monto_total, monto_pendiente FROM TBL_VENTA WHERE id_venta = ?");
        ps.setInt(1, idVenta);
        ResultSet rs = ps.executeQuery();
        if (!rs.next()) {
            JOptionPane.showMessageDialog(null, "Venta #" + idVenta + " no encontrada.",
                    "No encontrado", JOptionPane.ERROR_MESSAGE); return null;
        }
        return new double[]{rs.getDouble("monto_total"), rs.getDouble("monto_pendiente")};
    }

    private boolean validarMontoPago(double montoPago, double montoPendiente,
                                     double montoTotal, TextField txtMonto) {
        if (montoPendiente <= 0) {
            JOptionPane.showMessageDialog(null,
                    "Esta cuenta ya está completamente pagada.\nTotal: RD$ "
                            + String.format("%.2f", montoTotal),
                    "Sin deuda pendiente", JOptionPane.INFORMATION_MESSAGE); return false;
        }
        if (montoPago <= 0) {
            JOptionPane.showMessageDialog(null, "El monto debe ser mayor a cero.",
                    "Valor inválido", JOptionPane.WARNING_MESSAGE); return false;
        }
        if (montoPago > montoPendiente + 0.001) {
            JOptionPane.showMessageDialog(null,
                    "⚠ El monto supera el saldo pendiente.\n\n" +
                            "Monto ingresado:  RD$ " + String.format("%.2f", montoPago) + "\n" +
                            "Máximo permitido: RD$ " + String.format("%.2f", montoPendiente),
                    "Monto excedido", JOptionPane.WARNING_MESSAGE);
            txtMonto.setText(String.format("%.2f", montoPendiente));
            txtMonto.requestFocus(); txtMonto.selectAll(); return false;
        }
        return true;
    }

    private boolean validarFormularioPago(TextField txtMonto,
                                          ComboBox<String> cmbMetodo,
                                          ComboBox<String> cmbCuenta) {
        if (!validarFormularioPagoSinCuenta(txtMonto, cmbMetodo)) return false;
        if (cmbCuenta == null || cmbCuenta.getValue() == null
                || !mapaCuentas.containsKey(cmbCuenta.getValue())) {
            JOptionPane.showMessageDialog(null, "Selecciona una cuenta bancaria válida.",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE); return false;
        }
        return true;
    }

    private boolean validarFormularioPagoSinCuenta(TextField txtMonto, ComboBox<String> cmbMetodo) {
        if (txtMonto == null || txtMonto.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Ingresa el monto del pago.",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE); return false;
        }
        try { Double.parseDouble(txtMonto.getText().trim()); }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "El monto debe ser un número válido.",
                    "Valor inválido", JOptionPane.ERROR_MESSAGE); return false;
        }
        if (cmbMetodo == null || cmbMetodo.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona el método de pago.",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE); return false;
        }
        return true;
    }


    private void actualizarInfoLuegoDePago(double nuevoPendiente,
                                           Label lblInfo, Label lblSaldo,
                                           TextField txtMonto) {
        if (lblInfo != null)
            lblInfo.setStyle(nuevoPendiente <= 0
                    ? "-fx-text-fill: #2E7D32; -fx-font-size: 11px;"
                    : "-fx-text-fill: #C62828; -fx-font-size: 11px;");
        mostrarLabelContenido(lblSaldo,
                "Saldo pendiente: RD$ " + String.format("%.2f", nuevoPendiente));
        if (txtMonto != null) txtMonto.clear();
    }

    private void limpiarFormulario(TextField txtId, TextField txtMonto,
                                   ComboBox<String> cmbMetodo, ComboBox<String> cmbCuenta,
                                   DatePicker dp, Label lblInfo, Label lblSaldo) {
        if (txtId     != null) txtId.clear();
        if (txtMonto  != null) txtMonto.clear();
        if (cmbMetodo != null) cmbMetodo.setValue(null);
        if (cmbCuenta != null) cmbCuenta.setValue(null);
        if (dp        != null) dp.setValue(java.time.LocalDate.now());
        ocultarLabel(lblInfo);
        ocultarLabel(lblSaldo);
    }

    private void actualizarResumen(ObservableList<CuentaPendiente> lista, Label lbl, String tipo) {
        if (lbl == null) return;
        double totalPendiente = lista.stream().mapToDouble(CuentaPendiente::getMontoPendiente).sum();
        long   cantPendientes = lista.stream().filter(c -> c.getMontoPendiente() > 0).count();
        lbl.setText(cantPendientes + " cuentas por " + tipo
                + " | Total pendiente: RD$ " + String.format("%.2f", totalPendiente));
        lbl.setStyle(totalPendiente > 0
                ? "-fx-text-fill: #C62828; -fx-font-size: 11px;"
                : "-fx-text-fill: #2E7D32; -fx-font-size: 11px;");
    }

    private <T> void aplicarColorEstado(TableColumn<T, String> col) {
        col.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item) {
                    case "Pagado"    -> setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                    case "Parcial"   -> setStyle("-fx-text-fill: #E65100; -fx-font-weight: bold;");
                    case "Pendiente" -> setStyle("-fx-text-fill: #C62828; -fx-font-weight: bold;");
                    default          -> setStyle("");
                }
            }
        });
    }

    @FXML
    public void onImprimirReciboPago(ActionEvent ignored) {
        if (txtIdPagoImprimir == null || txtIdPagoImprimir.getText().isBlank()) {
            JOptionPane.showMessageDialog(null,
                    "Ingresa el ID del pago para imprimir.",
                    "ID requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        generarReporteReciboPago();
    }

    private void generarReporteReciboPago() {
        try {
            int idPago = Integer.parseInt(txtIdPagoImprimir.getText().trim());
            InputStream reporte = getClass().getResourceAsStream("/reports/ReciboPago.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(reporte);
            Map<String, Object> params = new HashMap<>();
            params.put("ID_PAGO", idPago);
            params.put("LOGO_STREAM", getClass().getResourceAsStream("/reports/logusfarmablanco.png"));
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport, params, conexion.establecerConexion());
            JasperViewer.viewReport(jasperPrint, false);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "El ID del pago debe ser un número.",
                    "ID inválido", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error al generar el reporte: " + e.getMessage());
            System.getLogger(getClass().getName())
                    .log(System.Logger.Level.ERROR, e.getMessage(), e);
        }
    }

    @FXML
    private void onEnviarReciboPago() {
        try {
            int idPago = Integer.parseInt(txtIdPagoImprimir.getText().trim());
            InputStream reporte = getClass().getResourceAsStream("/reports/ReciboPago.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(reporte);
            Map<String, Object> params = new HashMap<>();
            params.put("ID_PAGO", idPago);
            params.put("LOGO_STREAM", getClass().getResourceAsStream("/reports/logusfarmablanco.png"));
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport, params, conexion.establecerConexion());

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Enviar Recibo por Correo");
            dialog.setHeaderText("Ingrese el correo del destinatario:");
            dialog.setContentText("Correo:");
            String destinatario = dialog.showAndWait().orElse(null);
            if (destinatario == null || destinatario.isBlank()) return;

            byte[] pdf = JasperExportManager.exportReportToPdf(jasperPrint);

            EmailService emailService = new EmailService(
                    "smtp.gmail.com", "587",
                    "2230006@ipisa.edu.do", "dfjy zlqx nsve idyf");
            emailService.enviarConReporte(destinatario, "Recibo de Pago #" + idPago,
                    "Adjunto el recibo de pago.",
                    "ReciboPago_" + idPago + ".pdf",
                    new java.io.ByteArrayInputStream(pdf));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "El ID del pago debe ser un número.",
                    "ID inválido", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            JOptionPane.showMessageDialog(null,
                    "Error al enviar el recibo:\n" + e.getMessage()
                            + "\n\nDetalle:\n" + sw.toString().substring(0, Math.min(1000, sw.toString().length())),
                    "Error de envío", JOptionPane.ERROR_MESSAGE);
            System.getLogger(getClass().getName())
                    .log(System.Logger.Level.ERROR, e.getMessage(), e);
        }
    }

    public static class HistorialPagoItem {
        private final javafx.beans.property.SimpleIntegerProperty idPago     = new javafx.beans.property.SimpleIntegerProperty();
        private final javafx.beans.property.SimpleStringProperty  fecha      = new javafx.beans.property.SimpleStringProperty();
        private final javafx.beans.property.SimpleDoubleProperty  monto      = new javafx.beans.property.SimpleDoubleProperty();
        private final javafx.beans.property.SimpleStringProperty  metodoPago = new javafx.beans.property.SimpleStringProperty();
        private final javafx.beans.property.SimpleStringProperty  estado     = new javafx.beans.property.SimpleStringProperty();
        private final javafx.beans.property.SimpleStringProperty  cuenta     = new javafx.beans.property.SimpleStringProperty();
        private final javafx.beans.property.SimpleStringProperty  tipo       = new javafx.beans.property.SimpleStringProperty();

        public HistorialPagoItem(int idPago, String fecha, double monto,
                                 String metodo, String estado, String cuenta, String tipo) {
            this.idPago.set(idPago);
            this.fecha.set(fecha   != null ? fecha   : "");
            this.monto.set(monto);
            this.metodoPago.set(metodo  != null ? metodo  : "");
            this.estado.set(estado != null ? estado : "");
            this.cuenta.set(cuenta != null ? cuenta : "");
            this.tipo.set(tipo   != null ? tipo   : "");
        }

        public javafx.beans.property.SimpleIntegerProperty idPagoProperty()     { return idPago; }
        public javafx.beans.property.SimpleStringProperty  fechaProperty()      { return fecha; }
        public javafx.beans.property.SimpleDoubleProperty  montoProperty()      { return monto; }
        public javafx.beans.property.SimpleStringProperty  metodoPagoProperty() { return metodoPago; }
        public javafx.beans.property.SimpleStringProperty  estadoProperty()     { return estado; }
        public javafx.beans.property.SimpleStringProperty  cuentaProperty()     { return cuenta; }
        public javafx.beans.property.SimpleStringProperty  tipoProperty()       { return tipo; }

        public int    getIdPago() { return idPago.get(); }
        public double getMonto()  { return monto.get(); }
        public String getEstado() { return estado.get(); }
    }
}
