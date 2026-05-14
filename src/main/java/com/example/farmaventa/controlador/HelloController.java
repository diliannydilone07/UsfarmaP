package com.example.farmaventa.controlador;

import Usuarios.Permisos;
import Usuarios.SesionUsuario;
import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.EmailService;
import com.example.farmaventa.modelo.Venta;
import com.example.farmaventa.modelo.VentaSeguroItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;

import javax.swing.JOptionPane;
import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class HelloController {

    Conexion conexion = new Conexion();

    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnRegistrarVenta;
    @FXML private Button btnRegistrarVentaSeguro;
    @FXML private Button btnEditarSeguro;
    @FXML private Button btnQuitarProdSeguro;
    @FXML private Button btnEditarVenta;
    @FXML private Button btnQuitarProducto;
    @FXML private Button btnRegistrarPagoSeguro;
    @FXML private Button btnImprimirFactura;
    @FXML private Button btnImprimirFacturaSeguro;

    @FXML private TabPane tabPane;
    @FXML private Label   lblTipoActivo;



    @FXML private TextField        txtIdVenta;
    @FXML private Label            lblInfoVenta;
    @FXML private TextField        txtIdCliente;
    @FXML private Label            lblNombreCliente;
    @FXML private Label            lblSeguroCliente;
    @FXML private TextField        txtIdEmpleado;
    @FXML private ComboBox<String> cmbTipoVenta;
    @FXML private DatePicker       dpFechaVenta;
    @FXML private TextField        txtCondicion;
    @FXML private TextField        txtMontoPagado;
    @FXML private Label            lblMontoTotal;
    @FXML private Label            lblMontoPendiente;
    @FXML private Label            lblCantProductos;
    @FXML private TextField        txtIdProducto;
    @FXML private TextField        txtNombreProducto;
    @FXML private TextField        txtCantidadProducto;
    @FXML private TextField        txtPrecioProducto;
    @FXML private TextField        txtBuscarProducto;

    @FXML private CheckBox         chkComprobante;
    @FXML private ComboBox<String> cmbTipoComprobante;
    @FXML private Label            lblNcfGenerado;
    @FXML private VBox             panelComprobante;

    private int idComprobanteSeleccionado = -1;

    @FXML private ComboBox<String> cmbMetodoPagoVenta;
    @FXML private Button           btnRegistrarPagoVenta;

    private double pendienteActualVenta = 0;

    @FXML private TableView<Venta>           tablaVentaProducto;
    @FXML private TableColumn<Venta, Number> colVentaId;
    @FXML private TableColumn<Venta, String> colVentaCliente;
    @FXML private TableColumn<Venta, String> colProductoNombre;
    @FXML private TableColumn<Venta, Number> colProductoCantidad;
    @FXML private TableColumn<Venta, Number> colProductoPrecio;
    @FXML private TableColumn<Venta, Number> colProductoSubtotal;

    private ObservableList<Venta> listaTemporal = FXCollections.observableArrayList();
    private int idVentaSeleccionada = -1;



    @FXML private TextField  txtIdVentaSeguro;
    @FXML private Label      lblInfoVentaSeguro;
    @FXML private TextField  txtIdClienteSeguro;
    @FXML private Label      lblNombreClienteSeguro;
    @FXML private VBox       cardSeguroCliente;
    @FXML private Label      lblNombreSeguro;
    @FXML private Label      lblAseguradoraNombre;
    @FXML private Label      lblCoberturaSeguro;
    @FXML private Label      lblSinSeguro;
    @FXML private TextField  txtIdEmpleadoSeguro;
    @FXML private TextField  txtNumAutorizacion;
    @FXML private DatePicker dpFechaVentaSeguro;
    @FXML private TextField  txtCondicionSeguro;
    @FXML private Label      lblTotalSeguro;
    @FXML private Label      lblMontoAseguradora;
    @FXML private Label      lblMontoClienteSeguro;
    @FXML private TextField  txtIdProdSeg;
    @FXML private TextField  txtNombreProdSeg;
    @FXML private TextField  txtCantProdSeg;
    @FXML private TextField  txtPrecioProdSeg;
    @FXML private TextField  txtPctCobertura;

    @FXML private TextField        txtMontoAsegPagado;
    @FXML private ComboBox<String> cmbMetodoPagoAseg;
    @FXML private TextField        txtMontoCliPagado;
    @FXML private ComboBox<String> cmbMetodoPagoCli;
    @FXML private Label            lblPendienteAseg;
    @FXML private Label            lblPendienteCli;

    private double pendienteAsegActual = 0;
    private double pendienteCliActual  = 0;

    @FXML private CheckBox         chkComprobanteSeguro;
    @FXML private ComboBox<String> cmbTipoComprobanteSeguro;
    @FXML private Label            lblNcfGeneradoSeguro;
    @FXML private VBox             panelComprobanteSeguro;

    @FXML private TableView<VentaSeguroItem>           tablaSeguro;
    @FXML private TableColumn<VentaSeguroItem, String> colSegProd;
    @FXML private TableColumn<VentaSeguroItem, Number> colSegCant;
    @FXML private TableColumn<VentaSeguroItem, Number> colSegPrecio;
    @FXML private TableColumn<VentaSeguroItem, Number> colSegSub;
    @FXML private TableColumn<VentaSeguroItem, Number> colSegPctCob;
    @FXML private TableColumn<VentaSeguroItem, Number> colSegPctCli;
    @FXML private TableColumn<VentaSeguroItem, Number> colSegMtoAseg;
    @FXML private TableColumn<VentaSeguroItem, Number> colSegMtoCli;

    private ObservableList<VentaSeguroItem> listaSeguro = FXCollections.observableArrayList();
    private int    idVentaSeguroSeleccionada = -1;
    private int    idSeguroCliente           = -1;
    private double coberturaBaseCliente      = 0;

    @FXML
    public void initialize() {

        cmbTipoVenta.getItems().addAll("Contado", "Credito");
        dpFechaVenta.setValue(java.time.LocalDate.now());
        lblMontoTotal.setText("RD$ 0.00");
        lblMontoPendiente.setText("RD$ 0.00");
        lblCantProductos.setText("0 productos");

        colVentaId.setCellValueFactory(c -> c.getValue().idProperty());
        colVentaCliente.setCellValueFactory(c -> c.getValue().clienteProperty());
        colProductoNombre.setCellValueFactory(c -> c.getValue().productoProperty());
        colProductoCantidad.setCellValueFactory(c -> c.getValue().cantidadProperty());
        colProductoPrecio.setCellValueFactory(c -> c.getValue().totalProperty());
        colProductoSubtotal.setCellValueFactory(c -> c.getValue().subtotalProperty());
        tablaVentaProducto.setItems(listaTemporal);

        txtIdCliente.focusedProperty().addListener((obs, o, n) -> {
            if (!n && !txtIdCliente.getText().isBlank()) buscarInfoCliente();
        });

        dpFechaVentaSeguro.setValue(java.time.LocalDate.now());

        colSegProd.setCellValueFactory(c -> c.getValue().productoProperty());
        colSegCant.setCellValueFactory(c -> c.getValue().cantidadProperty());
        colSegPrecio.setCellValueFactory(c -> c.getValue().precioUnitarioProperty());
        colSegSub.setCellValueFactory(c -> c.getValue().subtotalProperty());
        colSegPctCob.setCellValueFactory(c -> c.getValue().porcentajeCobertProperty());
        colSegPctCli.setCellValueFactory(c -> c.getValue().porcentajeCliProperty());
        colSegMtoAseg.setCellValueFactory(c -> c.getValue().montoAseguradoraProperty());
        colSegMtoCli.setCellValueFactory(c -> c.getValue().montoClienteProperty());

        formatMoneda(colSegPrecio);
        formatMoneda(colSegSub);
        formatMoneda(colSegMtoAseg);
        formatMoneda(colSegMtoCli);
        formatPct(colSegPctCob);
        formatPct(colSegPctCli);

        tablaSeguro.setItems(listaSeguro);

        txtIdClienteSeguro.focusedProperty().addListener((obs, o, n) -> {
            if (!n && !txtIdClienteSeguro.getText().isBlank()) buscarInfoClienteSeguro();
        });

        if (cmbMetodoPagoAseg != null)
            cmbMetodoPagoAseg.getItems().addAll("Transferencia", "Cheque");
        if (cmbMetodoPagoCli != null)
            cmbMetodoPagoCli.getItems().addAll("Efectivo", "Tarjeta", "Transferencia");
        if (cmbMetodoPagoVenta != null)
            cmbMetodoPagoVenta.getItems().addAll("Efectivo", "Tarjeta", "Transferencia", "Cheque");

        if (lblPendienteAseg != null) lblPendienteAseg.setText("RD$ 0.00");
        if (lblPendienteCli  != null) lblPendienteCli.setText("RD$ 0.00");

        if (tabPane != null) {
            tabPane.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
                if (n == null || lblTipoActivo == null) return;
                int idx = tabPane.getSelectionModel().getSelectedIndex();
                lblTipoActivo.setText(idx == 1 ? "Venta con Seguro" : "Venta Normal");
            });
        }

        if (chkComprobante != null) {
            chkComprobante.selectedProperty().addListener((obs, oldVal, selected) -> {
                if (panelComprobante != null) {
                    panelComprobante.setVisible(selected);
                    panelComprobante.setManaged(selected);
                }
                if (!selected) {
                    if (cmbTipoComprobante != null) cmbTipoComprobante.setValue(null);
                    if (lblNcfGenerado     != null) lblNcfGenerado.setText("");
                    idComprobanteSeleccionado = -1;
                }
            });
            cargarTiposComprobanteEn(cmbTipoComprobante);
        }

        if (chkComprobanteSeguro != null) {
            chkComprobanteSeguro.selectedProperty().addListener((obs, oldVal, selected) -> {
                if (panelComprobanteSeguro != null) {
                    panelComprobanteSeguro.setVisible(selected);
                    panelComprobanteSeguro.setManaged(selected);
                }
                if (!selected) {
                    if (cmbTipoComprobanteSeguro != null) cmbTipoComprobanteSeguro.setValue(null);
                    if (lblNcfGeneradoSeguro     != null) lblNcfGeneradoSeguro.setText("");
                }
            });
            cargarTiposComprobanteEn(cmbTipoComprobanteSeguro);
        }

        Permisos.aplicarBtn(btnEditar,               Permisos.Accion.EDITAR);
        Permisos.aplicarBtn(btnEliminar,             Permisos.Accion.ELIMINAR);
        Permisos.aplicarBtn(btnRegistrarVenta,       Permisos.Accion.REGISTRAR);
        Permisos.aplicarBtn(btnRegistrarVentaSeguro, Permisos.Accion.REGISTRAR);
        Permisos.aplicarBtn(btnEditarSeguro,         Permisos.Accion.EDITAR);
        Permisos.aplicarBtn(btnQuitarProdSeguro,     Permisos.Accion.ELIMINAR);
        Permisos.aplicarBtn(btnEditarVenta,          Permisos.Accion.EDITAR);
        Permisos.aplicarBtn(btnQuitarProducto,       Permisos.Accion.ELIMINAR);
        Permisos.aplicarBtn(btnRegistrarPagoSeguro,  Permisos.Accion.REGISTRAR);
        Permisos.aplicarBtn(btnRegistrarPagoVenta,  Permisos.Accion.REGISTRAR);

        if (!Permisos.puedeHacer(Permisos.Accion.EDITAR)) {
            if (txtIdVenta       != null) { txtIdVenta.setDisable(true);       txtIdVenta.setPromptText("Sin permiso"); }
            if (txtIdVentaSeguro != null) { txtIdVentaSeguro.setDisable(true); txtIdVentaSeguro.setPromptText("Sin permiso"); }
        }
    }




    private void cargarTiposComprobanteEn(ComboBox<String> cmb) {
        if (cmb == null) return;
        String sql = "SELECT MIN(id_comprobante) AS id_comprobante, tipo, serie " +
                "FROM TBL_COMPROBANTE_FISCAL WHERE estado = 1 " +
                "GROUP BY tipo, serie ORDER BY tipo";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            cmb.getItems().clear();
            while (rs.next()) {
                int tipo = rs.getInt("tipo");
                String label = switch (tipo) {
                    case 1  -> "B01 - Crédito Fiscal";
                    case 2  -> "B02 - Consumidor Final";
                    case 3  -> "B03 - Nota de Débito";
                    case 4  -> "B04 - Nota de Crédito";
                    case 11 -> "B11 - Proveedores Informales";
                    case 13 -> "B13 - Gastos Menores";
                    case 14 -> "B14 - Gubernamental";
                    case 15 -> "B15 - Exportaciones";
                    default -> "B" + String.format("%02d", tipo) + " - Tipo " + tipo;
                };
                cmb.getItems().add(label);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error cargando comprobantes: " + e.getMessage());
        }
    }

    private int asignarComprobante(Connection con,
                                   CheckBox chk,
                                   ComboBox<String> cmb,
                                   Label lblNcf) throws SQLException {
        if (chk == null || !chk.isSelected()) return -1;
        if (cmb == null || cmb.getValue() == null) return -1;

        String seleccion = cmb.getValue();
        int tipo;
        try {
            tipo = Integer.parseInt(seleccion.substring(1, 3));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "No se pudo determinar el tipo de comprobante.");
            return -1;
        }

        int    idComprobante = -1;
        String serie         = "B";
        String sqlBase = "SELECT id_comprobante, serie FROM TBL_COMPROBANTE_FISCAL " +
                "WHERE tipo = ? AND estado = 1";
        try (PreparedStatement ps = con.prepareStatement(sqlBase)) {
            ps.setInt(1, tipo);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(null,
                        "No hay comprobante activo para el tipo seleccionado.");
                return -1;
            }
            idComprobante = rs.getInt("id_comprobante");
            serie         = rs.getString("serie").trim();
        }

        int proximaSecuencia = 1;
        String sqlMax = "SELECT ISNULL(MAX(secuencia), 0) + 1 AS proxima " +
                "FROM TBL_SECUENCIA_DE_COMPROBANTE WHERE id_comprobante = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlMax)) {
            ps.setInt(1, idComprobante);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) proximaSecuencia = rs.getInt("proxima");
        }

        String usuario = "Sistema";
        try { usuario = SesionUsuario.getInstance().getUsuarioActual().getNombreCompleto(); }
        catch (Exception ignored) {}

        String sqlInsert = "INSERT INTO TBL_SECUENCIA_DE_COMPROBANTE " +
                "(secuencia, fecha, estado, quien_creo, id_comprobante) VALUES (?,?,0,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
            ps.setInt(1,    proximaSecuencia);
            ps.setDate(2,   Date.valueOf(java.time.LocalDate.now()));
            ps.setString(3, usuario);
            ps.setInt(4,    idComprobante);
            ps.executeUpdate();
        }

        String ncf = serie + String.format("%02d", tipo) + String.format("%08d", proximaSecuencia);
        if (lblNcf != null) {
            lblNcf.setText("NCF: " + ncf);
            lblNcf.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1B5E20;" +
                    "-fx-background-color: #E8F5E9; -fx-background-radius: 5; -fx-padding: 4 8 4 8;");
        }

        idComprobanteSeleccionado = idComprobante;
        return idComprobante;
    }

    private int asignarComprobanteFiscal(Connection con) throws SQLException {
        return asignarComprobante(con, chkComprobante, cmbTipoComprobante, lblNcfGenerado);
    }

    private int asignarComprobanteFiscalSeguro(Connection con) throws SQLException {
        return asignarComprobante(con, chkComprobanteSeguro, cmbTipoComprobanteSeguro, lblNcfGeneradoSeguro);
    }




    private void buscarInfoCliente() {
        String sql = "SELECT p.nombre + ' ' + p.apellido AS nombre_completo, " +
                "       ISNULL(sm.nombre_seguro, 'Sin seguro') AS nombre_seguro, c.id_seguro " +
                "FROM TBL_CLIENTE c " +
                "JOIN TBL_PERSONA p ON p.id_persona = c.id_persona " +
                "LEFT JOIN TBL_SEGURO_MEDICO sm ON sm.id_seguro = c.id_seguro " +
                "WHERE c.id_cliente = ?";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(txtIdCliente.getText().trim()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (lblNombreCliente != null) lblNombreCliente.setText(rs.getString("nombre_completo"));
                if (lblSeguroCliente != null) {
                    int idSeg = rs.getInt("id_seguro");
                    if (idSeg > 0) {
                        lblSeguroCliente.setText("🏥 " + rs.getString("nombre_seguro"));
                        lblSeguroCliente.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 11px; -fx-font-weight: bold;");
                    } else {
                        lblSeguroCliente.setText("Sin seguro medico");
                        lblSeguroCliente.setStyle("-fx-text-fill: #9E9E9E; -fx-font-size: 11px;");
                    }
                }
            } else {
                if (lblNombreCliente != null) lblNombreCliente.setText("Cliente no encontrado");
                if (lblSeguroCliente != null) { lblSeguroCliente.setText(""); lblSeguroCliente.setStyle(""); }
            }
        } catch (Exception ignored) {}
    }

    @FXML
    public void onBuscarVenta(ActionEvent event) {
        if (txtIdVenta.getText().isBlank()) return;
        String sql = "SELECT v.id_venta, v.id_cliente, v.id_empleado, v.tipo_venta, v.condicion, " +
                "CONVERT(VARCHAR(10), v.fecha_transaccion, 120) AS fecha, " +
                "v.monto_total, v.monto_pendiente, p.nombre + ' ' + p.apellido AS cliente " +
                "FROM TBL_VENTA v " +
                "JOIN TBL_CLIENTE cl ON cl.id_cliente = v.id_cliente " +
                "JOIN TBL_PERSONA p  ON p.id_persona  = cl.id_persona " +
                "WHERE v.id_venta = ?";
        try {
            int idV = Integer.parseInt(txtIdVenta.getText().trim());
            try (Connection con = conexion.establecerConexion();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idV);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    lblInfoVenta.setText("Venta no encontrada.");
                    lblInfoVenta.setStyle("-fx-text-fill: #C62828; -fx-font-size: 11px;");
                    return;
                }
                idVentaSeleccionada = rs.getInt("id_venta");
                txtIdCliente.setText(String.valueOf(rs.getInt("id_cliente")));
                txtIdEmpleado.setText(String.valueOf(rs.getInt("id_empleado")));
                cmbTipoVenta.setValue(rs.getString("tipo_venta"));
                txtCondicion.setText(rs.getString("condicion") != null ? rs.getString("condicion") : "");
                try { dpFechaVenta.setValue(java.time.LocalDate.parse(rs.getString("fecha"))); } catch (Exception ignored) {}
                double mt = rs.getDouble("monto_total"), mp = rs.getDouble("monto_pendiente");
                pendienteActualVenta = mp;
                lblInfoVenta.setText("Venta #" + idV + " - " + rs.getString("cliente") +
                        " | Total: RD$ " + String.format("%.2f", mt) +
                        " | Pendiente: RD$ " + String.format("%.2f", mp));
                lblInfoVenta.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 11px;");
                if (mt - mp > 0) txtMontoPagado.setText(String.format("%.2f", mt - mp));
                cargarProductosDeVenta(con, idV);
                buscarInfoCliente();
            }
        } catch (NumberFormatException e) {
            lblInfoVenta.setText("ID invalido.");
            lblInfoVenta.setStyle("-fx-text-fill: #C62828; -fx-font-size: 11px;");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private void cargarProductosDeVenta(Connection con, int idVenta) throws SQLException {
        listaTemporal.clear();
        PreparedStatement ps = con.prepareStatement(
                "SELECT vp.id_producto, pr.nombre, vp.cantidad, vp.precio_unitario " +
                        "FROM TBL_VENTA_PRODUCTO vp " +
                        "JOIN TBL_PRODUCTO pr ON pr.id_producto = vp.id_producto " +
                        "WHERE vp.id_venta = ?");
        ps.setInt(1, idVenta);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            double precio = rs.getDouble("precio_unitario");
            int    cant   = rs.getInt("cantidad");
            listaTemporal.add(new Venta(rs.getInt("id_producto"), "", precio, cant, precio * cant,
                    rs.getString("nombre"), "", "", "", 0, 0, ""));
        }
        tablaVentaProducto.setItems(listaTemporal);
        actualizarTotalesNormal();
    }

    @FXML
    public void onBuscarProductoId(ActionEvent event) {
        if (txtIdProducto.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Ingresa el ID del producto.");
            return;
        }
        buscarProducto(txtIdProducto, txtNombreProducto, txtPrecioProducto);
    }

    @FXML
    public void onAgregarProducto(ActionEvent event) {
        if (txtIdProducto.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Ingresa el ID del producto.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (txtNombreProducto.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Busca el producto primero (boton Buscar).", "Producto no buscado", JOptionPane.WARNING_MESSAGE);
            txtIdProducto.requestFocus();
            return;
        }
        if (txtPrecioProducto.getUserData() == null) {
            JOptionPane.showMessageDialog(null,
                    "Debes buscar el producto antes de agregarlo.\nEso garantiza que el precio sea el del sistema.",
                    "Producto no validado", JOptionPane.WARNING_MESSAGE);
            txtIdProducto.requestFocus();
            return;
        }
        if (txtCantidadProducto.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "La cantidad es obligatoria.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int    idProd    = Integer.parseInt(txtIdProducto.getText().trim());
            int    cant      = Integer.parseInt(txtCantidadProducto.getText().trim());
            double precio    = Double.parseDouble(txtPrecioProducto.getText().trim());
            double precioMax = (double) txtPrecioProducto.getUserData();

            if (precio > precioMax + 0.001) {
                JOptionPane.showMessageDialog(null,
                        "El precio ingresado supera el precio registrado en el sistema.\n" +
                                "Precio maximo: RD$ " + String.format("%.2f", precioMax),
                        "Precio excedido", JOptionPane.WARNING_MESSAGE);
                txtPrecioProducto.setText(String.format("%.2f", precioMax));
                txtPrecioProducto.requestFocus();
                txtPrecioProducto.selectAll();
                return;
            }

            listaTemporal.add(new Venta(idProd, "", precio, cant, precio * cant,
                    txtNombreProducto.getText().trim(), "", "", "", 0, 0, ""));
            tablaVentaProducto.setItems(listaTemporal);
            actualizarTotalesNormal();

            txtIdProducto.clear();
            txtNombreProducto.clear();
            txtCantidadProducto.clear();
            if (txtPrecioProducto != null) {
                txtPrecioProducto.clear();
                txtPrecioProducto.setUserData(null);
                txtPrecioProducto.setEditable(true);
                txtPrecioProducto.setStyle("-fx-background-radius: 6; -fx-border-color: #E0E0E0; -fx-border-radius: 6;");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Cantidad y precio deben ser numeros.");
        }
    }

    @FXML
    public void fnBuscarProducto(ActionEvent event) {
        String b = txtBuscarProducto.getText().trim().toLowerCase();
        if (b.isEmpty()) { tablaVentaProducto.setItems(listaTemporal); return; }
        ObservableList<Venta> f = FXCollections.observableArrayList();
        for (Venta v : listaTemporal)
            if (v.getProducto().toLowerCase().contains(b)) f.add(v);
        tablaVentaProducto.setItems(f);
    }

    @FXML
    public void onQuitarProducto(ActionEvent event) {
        Venta sel = tablaVentaProducto.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona un producto de la tabla."); return; }
        listaTemporal.remove(sel);
        actualizarTotalesNormal();
    }

    @FXML
    public void onAbrirCatalogo(ActionEvent event) {
        try {
            Node nodo = (Node) event.getSource();
            StackPane contentArea = (StackPane) nodo.getScene().lookup("#contentArea");
            if (contentArea == null) { JOptionPane.showMessageDialog(null, "No se pudo encontrar el area de contenido."); return; }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/farmaventa/SelectorProducto.fxml"));
            Node selectorVista = loader.load();
            SelectorProductoController ctrl = loader.getController();
            ctrl.init(this, contentArea, "Venta | " + listaTemporal.size() + " productos");
            contentArea.getChildren().setAll(selectorVista);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al abrir catalogo: " + e.getMessage());
        }
    }

    public void recibirProductoDelCatalogo(int idProducto, String nombre, int cantidad, double precio) {
        listaTemporal.add(new Venta(idProducto, "", precio, cantidad, precio * cantidad, nombre, "", "", "", 0, 0, ""));
        actualizarTotalesNormal();
    }

    @FXML
    public void onRegistrarVenta(ActionEvent event) {
        if (txtIdCliente.getText().isBlank()) { JOptionPane.showMessageDialog(null, "El ID de Cliente es obligatorio."); return; }
        if (cmbTipoVenta.getValue() == null)  { JOptionPane.showMessageDialog(null, "Selecciona el tipo de venta."); return; }
        if (listaTemporal.isEmpty())          { JOptionPane.showMessageDialog(null, "Agrega al menos un producto."); return; }
        if (chkComprobante != null && chkComprobante.isSelected()
                && (cmbTipoComprobante == null || cmbTipoComprobante.getValue() == null)) {
            JOptionPane.showMessageDialog(null,
                    "Selecciona el tipo de comprobante fiscal o desmarca la opcion.",
                    "Comprobante requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double montoTotal = calcTotalNormal();
        double montoPagado = parsePagado(txtMontoPagado);
        if (montoPagado > montoTotal + 0.001) {
            JOptionPane.showMessageDialog(null,
                    "El monto pagado (RD$ " + String.format("%.2f", montoPagado) +
                            ") supera el total de la venta (RD$ " + String.format("%.2f", montoTotal) + ").",
                    "Monto excedido", JOptionPane.WARNING_MESSAGE);
            txtMontoPagado.requestFocus(); return;
        }
        double pendiente  = Math.max(0, montoTotal - montoPagado);

        try (Connection con = conexion.establecerConexion()) {
            con.setAutoCommit(false);
            try {
                int idComp = asignarComprobanteFiscal(con);

                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO TBL_VENTA (id_empleado, tipo_venta, fecha_transaccion, " +
                                "monto_total, monto_pendiente, condicion, id_cliente, id_comprobante) " +
                                "VALUES (?,?,?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, txtIdEmpleado.getText().isBlank() ? 1 : Integer.parseInt(txtIdEmpleado.getText().trim()));
                ps.setString(2, cmbTipoVenta.getValue());
                ps.setDate(3,   Date.valueOf(dpFechaVenta.getValue()));
                ps.setDouble(4, montoTotal);
                ps.setDouble(5, pendiente);
                ps.setString(6, txtCondicion.getText().trim());
                ps.setInt(7,    Integer.parseInt(txtIdCliente.getText().trim()));
                if (idComp == -1) ps.setNull(8, java.sql.Types.INTEGER);
                else              ps.setInt(8, idComp);
                ps.executeUpdate();

                int idVenta = -1;
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) idVenta = keys.getInt(1);

                for (Venta v : listaTemporal) {
                    PreparedStatement psD = con.prepareStatement(
                            "INSERT INTO TBL_VENTA_PRODUCTO " +
                                    "(id_venta, id_producto, cantidad, precio_unitario, fecha_venta, id_presentacion) " +
                                    "VALUES (?,?,?,?,?,1)");
                    psD.setInt(1, idVenta);
                    psD.setInt(2, v.getIdVenta());
                    psD.setInt(3, v.getCantidad());
                    psD.setDouble(4, v.getTotal());
                    psD.setDate(5, Date.valueOf(dpFechaVenta.getValue()));
                    psD.executeUpdate();
                }

                if (montoPagado > 0) {
                    String metodo = (cmbMetodoPagoVenta != null && cmbMetodoPagoVenta.getValue() != null)
                            ? cmbMetodoPagoVenta.getValue() : "Efectivo";
                    int idPago = insertarPago(con, "Venta",
                            Date.valueOf(dpFechaVenta.getValue()), montoPagado, metodo);
                    try (PreparedStatement psPuente = con.prepareStatement(
                            "INSERT INTO TBL_PAGO_VENTA (id_venta, id_pago) VALUES (?,?)")) {
                        psPuente.setInt(1, idVenta);
                        psPuente.setInt(2, idPago);
                        psPuente.executeUpdate();
                    }
                }

                con.commit();

                final int idVentaCreada = idVenta;
                String msgNcf = (lblNcfGenerado != null && !lblNcfGenerado.getText().isBlank())
                        ? "\n" + lblNcfGenerado.getText()
                        : "\nSin comprobante fiscal";

                int respuesta = JOptionPane.showConfirmDialog(null,
                        "✅ Venta #" + idVentaCreada + " registrada correctamente." + msgNcf +
                                "\n\n¿Deseas imprimir la factura ahora?",
                        "Venta registrada",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);

                onLimpiarVenta(null);

                if (respuesta == JOptionPane.YES_OPTION) {
                    generarReporteVentaNormal(idVentaCreada);
                }

            } catch (Exception ex) {
                con.rollback();
                JOptionPane.showMessageDialog(null, "Error al registrar venta: " + ex.getMessage());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error de conexion: " + e.getMessage());
        }
    }

    @FXML
    public void onRegistrarPagoVenta(ActionEvent event) {
        if (idVentaSeleccionada == -1) {
            JOptionPane.showMessageDialog(null, "Primero busca una venta por ID.");
            return;
        }
        double montoPago = parsePagado(txtMontoPagado);
        if (montoPago <= 0) {
            JOptionPane.showMessageDialog(null, "Ingresa un monto de pago valido.");
            txtMontoPagado.requestFocus(); return;
        }
        if (montoPago > pendienteActualVenta + 0.001) {
            JOptionPane.showMessageDialog(null,
                    "El pago supera el pendiente.\nPendiente actual: RD$ " +
                            String.format("%.2f", pendienteActualVenta),
                    "Monto excedido", JOptionPane.WARNING_MESSAGE);
            txtMontoPagado.requestFocus(); return;
        }
        if (pendienteActualVenta <= 0) {
            JOptionPane.showMessageDialog(null, "La venta ya esta completamente pagada.");
            return;
        }
        String metodo = (cmbMetodoPagoVenta != null && cmbMetodoPagoVenta.getValue() != null)
                ? cmbMetodoPagoVenta.getValue() : "Efectivo";

        try (Connection con = conexion.establecerConexion()) {
            con.setAutoCommit(false);
            try {
                int idPago = insertarPago(con, "Venta",
                        Date.valueOf(java.time.LocalDate.now()), montoPago, metodo);
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO TBL_PAGO_VENTA (id_venta, id_pago) VALUES (?,?)")) {
                    ps.setInt(1, idVentaSeleccionada);
                    ps.setInt(2, idPago);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE TBL_VENTA SET monto_pendiente = monto_pendiente - ? WHERE id_venta = ?")) {
                    ps.setDouble(1, montoPago);
                    ps.setInt(2, idVentaSeleccionada);
                    ps.executeUpdate();
                }
                con.commit();

                pendienteActualVenta = Math.max(0, pendienteActualVenta - montoPago);
                lblMontoPendiente.setText("RD$ " + String.format("%.2f", pendienteActualVenta));
                txtMontoPagado.clear();
                if (cmbMetodoPagoVenta != null) cmbMetodoPagoVenta.setValue(null);

                JOptionPane.showMessageDialog(null,
                        "✅ Pago registrado.\n\nPendiente actual: RD$ " +
                                String.format("%.2f", pendienteActualVenta));

            } catch (Exception ex) {
                con.rollback();
                JOptionPane.showMessageDialog(null, "Error al registrar pago: " + ex.getMessage());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error de conexion: " + e.getMessage());
        }
    }

    @FXML
    public void onEditarVenta(ActionEvent event) {
        if (idVentaSeleccionada == -1) { JOptionPane.showMessageDialog(null, "Primero busca una venta por ID."); return; }
        if (txtIdCliente.getText().isBlank() || cmbTipoVenta.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Cliente y tipo de venta son obligatorios.");
            return;
        }
        double montoTotal = calcTotalNormal();
        double pendiente  = Math.max(0, montoTotal - parsePagado(txtMontoPagado));
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE TBL_VENTA SET id_empleado=?,tipo_venta=?,fecha_transaccion=?," +
                             "monto_total=?,monto_pendiente=?,condicion=?,id_cliente=? WHERE id_venta=?")) {
            ps.setInt(1, txtIdEmpleado.getText().isBlank() ? 1 : Integer.parseInt(txtIdEmpleado.getText().trim()));
            ps.setString(2, cmbTipoVenta.getValue());
            ps.setDate(3, Date.valueOf(dpFechaVenta.getValue()));
            ps.setDouble(4, montoTotal);
            ps.setDouble(5, pendiente);
            ps.setString(6, txtCondicion.getText().trim());
            ps.setInt(7, Integer.parseInt(txtIdCliente.getText().trim()));
            ps.setInt(8, idVentaSeleccionada);
            ps.executeUpdate();

            con.prepareStatement("DELETE FROM TBL_VENTA_PRODUCTO WHERE id_venta=" + idVentaSeleccionada).executeUpdate();
            for (Venta v : listaTemporal) {
                PreparedStatement psD = con.prepareStatement(
                        "INSERT INTO TBL_VENTA_PRODUCTO " +
                                "(id_venta, id_producto, cantidad, precio_unitario, fecha_venta, id_presentacion) " +
                                "VALUES (?,?,?,?,?,1)");
                psD.setInt(1, idVentaSeleccionada);
                psD.setInt(2, v.getIdVenta());
                psD.setInt(3, v.getCantidad());
                psD.setDouble(4, v.getTotal());
                psD.setDate(5, Date.valueOf(dpFechaVenta.getValue()));
                psD.executeUpdate();
            }
            JOptionPane.showMessageDialog(null, "Venta #" + idVentaSeleccionada + " actualizada.");
            onLimpiarVenta(null);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al editar venta: " + e.getMessage());
        }
    }

    @FXML
    public void onLimpiarVenta(ActionEvent event) {
        txtIdVenta.clear();
        txtIdCliente.clear();
        txtIdEmpleado.clear();
        txtCondicion.clear();
        txtMontoPagado.clear();
        if (txtIdProducto       != null) txtIdProducto.clear();
        if (txtNombreProducto   != null) txtNombreProducto.clear();
        if (txtCantidadProducto != null) txtCantidadProducto.clear();
        if (txtPrecioProducto   != null) {
            txtPrecioProducto.clear();
            txtPrecioProducto.setUserData(null);
            txtPrecioProducto.setEditable(true);
            txtPrecioProducto.setStyle("-fx-background-radius: 6; -fx-border-color: #E0E0E0; -fx-border-radius: 6;");
        }
        if (txtBuscarProducto != null) txtBuscarProducto.clear();
        if (lblInfoVenta      != null) lblInfoVenta.setText("");
        if (lblNombreCliente  != null) lblNombreCliente.setText("");
        if (lblSeguroCliente  != null) lblSeguroCliente.setText("");
        if (cmbMetodoPagoVenta != null) cmbMetodoPagoVenta.setValue(null);
        pendienteActualVenta = 0;
        cmbTipoVenta.setValue(null);
        dpFechaVenta.setValue(java.time.LocalDate.now());
        listaTemporal.clear();
        idVentaSeleccionada = -1;
        lblMontoTotal.setText("RD$ 0.00");
        lblMontoPendiente.setText("RD$ 0.00");
        lblCantProductos.setText("0 productos");

        if (chkComprobante     != null) chkComprobante.setSelected(false);
        if (cmbTipoComprobante != null) cmbTipoComprobante.setValue(null);
        if (lblNcfGenerado     != null) lblNcfGenerado.setText("");
        if (panelComprobante   != null) { panelComprobante.setVisible(false); panelComprobante.setManaged(false); }
        idComprobanteSeleccionado = -1;
    }

    @FXML
    public void Limpiar() { onLimpiarVenta(null); }

    private void actualizarTotalesNormal() {
        double total  = calcTotalNormal();
        double pagado = parsePagado(txtMontoPagado);
        lblMontoTotal.setText("RD$ " + String.format("%.2f", total));
        lblMontoPendiente.setText("RD$ " + String.format("%.2f", Math.max(0, total - pagado)));
        lblCantProductos.setText(listaTemporal.size() + " productos");
    }

    private double calcTotalNormal() {
        double t = 0;
        for (Venta v : listaTemporal) t += v.getSubtotal();
        return t;
    }





    private JasperPrint generarReporteVentaNormal(int idVenta) throws Exception {
        Connection connection = null;
        try {
            InputStream reportStream = getClass()
                    .getResourceAsStream("/reports/Factura.jrxml");

            if (reportStream == null) {
                throw new Exception("No se encontró el archivo del reporte Factura.jrxml");
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ID_VENTA", idVenta);
            parameters.put("LOGO_STREAM", getClass().getResourceAsStream("/reports/logusfarmablanco.png"));

            connection = conexion.establecerConexion();

            return JasperFillManager.fillReport(jasperReport, parameters, connection);
        } finally {
            if (connection != null) {
                try { connection.close(); } catch (Exception ignored) {}
            }
        }
    }


    @FXML
    public void onImprimirVenta(ActionEvent event) {
        if (idVentaSeleccionada == -1) {
            if (listaTemporal.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "No hay una venta seleccionada.\n" +
                                "Primero registra o busca una venta.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null,
                        "Hay productos en la tabla pero la venta no está guardada.\n\n" +
                                "Para imprimir:\n" +
                                "1. Registra la venta con 'Registrar Venta'\n" +
                                "2. O busca una venta existente por ID\n" +
                                "3. Luego presiona 'Imprimir Factura'",
                        "Venta no registrada", JOptionPane.WARNING_MESSAGE);
            }
            return;
        }
        try {
            JasperPrint jasperPrint = generarReporteVentaNormal(idVentaSeleccionada);
            JasperViewer viewer = new JasperViewer(jasperPrint, false);
            viewer.setTitle("Factura #" + String.format("%06d", idVentaSeleccionada) + " — UsFarma");
            viewer.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error al generar el reporte:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @FXML
    public void onEnviarFactura(ActionEvent event) {
        if (idVentaSeleccionada == -1) {
            JOptionPane.showMessageDialog(null,
                    "No hay una venta seleccionada.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            JasperPrint jasperPrint = generarReporteVentaNormal(idVentaSeleccionada);
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Enviar Factura por Correo");
            dialog.setHeaderText("Ingrese el correo del destinatario:");
            dialog.setContentText("Correo:");
            String destinatario = dialog.showAndWait().orElse(null);
            if (destinatario == null || destinatario.isBlank()) return;

            byte[] pdf = JasperExportManager.exportReportToPdf(jasperPrint);
            EmailService emailService = new EmailService(
                    "smtp.gmail.com", "587",
                    "2230006@ipisa.edu.do", "dfjy zlqx nsve idyf");
            emailService.enviarConReporte(destinatario,
                    "Factura #" + String.format("%06d", idVentaSeleccionada),
                    "Adjunto la factura de venta.",
                    "Factura_" + idVentaSeleccionada + ".pdf",
                    new java.io.ByteArrayInputStream(pdf));
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error al enviar la factura:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }




    private void buscarInfoClienteSeguro() {
        String sql = "SELECT p.nombre + ' ' + p.apellido AS nombre_completo, " +
                "       c.id_seguro, sm.nombre_seguro, sm.cobertura, a.nombre AS aseguradora " +
                "FROM TBL_CLIENTE c " +
                "JOIN TBL_PERSONA p ON p.id_persona = c.id_persona " +
                "LEFT JOIN TBL_SEGURO_MEDICO sm ON sm.id_seguro = c.id_seguro " +
                "LEFT JOIN TBL_ASEGURADORA a ON a.id_aseguradora = sm.id_aseguradora " +
                "WHERE c.id_cliente = ?";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(txtIdClienteSeguro.getText().trim()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                lblNombreClienteSeguro.setText(rs.getString("nombre_completo"));
                idSeguroCliente = rs.getInt("id_seguro");
                if (idSeguroCliente > 0) {
                    coberturaBaseCliente = rs.getDouble("cobertura");
                    lblNombreSeguro.setText("Seguro: " + rs.getString("nombre_seguro"));
                    lblAseguradoraNombre.setText(rs.getString("aseguradora"));
                    lblCoberturaSeguro.setText(String.format("%.0f%%", coberturaBaseCliente));
                    cardSeguroCliente.setVisible(true); cardSeguroCliente.setManaged(true);
                    lblSinSeguro.setVisible(false); lblSinSeguro.setManaged(false);
                    txtPctCobertura.setText(String.format("%.0f", coberturaBaseCliente));
                } else {
                    idSeguroCliente = -1; coberturaBaseCliente = 0;
                    cardSeguroCliente.setVisible(false); cardSeguroCliente.setManaged(false);
                    lblSinSeguro.setText("Este cliente no tiene seguro medico registrado.");
                    lblSinSeguro.setVisible(true); lblSinSeguro.setManaged(true);
                }
            } else {
                lblNombreClienteSeguro.setText("Cliente no encontrado");
                idSeguroCliente = -1;
            }
        } catch (Exception ignored) {}
    }

    @FXML
    public void onBuscarVentaSeguro(ActionEvent event) {
        if (txtIdVentaSeguro.getText().isBlank()) return;
        String sql = "SELECT vs.*, p.nombre + ' ' + p.apellido AS cliente, " +
                "sm.nombre_seguro, sm.cobertura, a.nombre AS aseguradora " +
                "FROM TBL_VENTA_SEGURO vs " +
                "JOIN TBL_CLIENTE cl ON cl.id_cliente = vs.id_cliente " +
                "JOIN TBL_PERSONA p  ON p.id_persona  = cl.id_persona " +
                "JOIN TBL_SEGURO_MEDICO sm ON sm.id_seguro = cl.id_seguro " +
                "JOIN TBL_ASEGURADORA a ON a.id_aseguradora = sm.id_aseguradora " +
                "WHERE vs.id_ventaseguro = ?";
        try {
            int idVS = Integer.parseInt(txtIdVentaSeguro.getText().trim());
            try (Connection con = conexion.establecerConexion();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idVS);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) { lblInfoVentaSeguro.setText("Venta no encontrada."); return; }
                idVentaSeguroSeleccionada = idVS;
                txtIdClienteSeguro.setText(String.valueOf(rs.getInt("id_cliente")));
                txtIdEmpleadoSeguro.setText(String.valueOf(rs.getInt("id_empleado")));
                txtNumAutorizacion.setText(rs.getString("numero_autorizacion"));
                txtCondicionSeguro.setText(rs.getString("condicion") != null ? rs.getString("condicion") : "");
                try {
                    String f = rs.getString("fecha_transaccion");
                    if (f != null) dpFechaVentaSeguro.setValue(java.time.LocalDate.parse(f.substring(0, 10)));
                } catch (Exception ignored) {}
                lblInfoVentaSeguro.setText("VentaSeguro #" + idVS + " - " + rs.getString("cliente") +
                        " | " + rs.getString("nombre_seguro"));
                lblInfoVentaSeguro.setStyle("-fx-text-fill: #1B5E20; -fx-font-size: 11px;");

                pendienteAsegActual = rs.getDouble("monto_pendiente_a");
                pendienteCliActual  = rs.getDouble("monto_pendiente_cliente");
                actualizarLabelsPendiente();

                buscarInfoClienteSeguro();
                cargarProductosVentaSeguro(con, idVS);
            }
        } catch (NumberFormatException e) {
            lblInfoVentaSeguro.setText("ID invalido.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private void actualizarLabelsPendiente() {
        if (lblPendienteAseg != null) {
            lblPendienteAseg.setText("Pendiente aseguradora: RD$ " + String.format("%.2f", pendienteAsegActual));
            lblPendienteAseg.setStyle(pendienteAsegActual <= 0
                    ? "-fx-text-fill: #2E7D32; -fx-font-size: 11px; -fx-font-weight: bold;"
                    : "-fx-text-fill: #C62828; -fx-font-size: 11px; -fx-font-weight: bold;");
        }
        if (lblPendienteCli != null) {
            lblPendienteCli.setText("Pendiente cliente: RD$ " + String.format("%.2f", pendienteCliActual));
            lblPendienteCli.setStyle(pendienteCliActual <= 0
                    ? "-fx-text-fill: #2E7D32; -fx-font-size: 11px; -fx-font-weight: bold;"
                    : "-fx-text-fill: #C62828; -fx-font-size: 11px; -fx-font-weight: bold;");
        }
    }

    private void cargarProductosVentaSeguro(Connection con, int idVS) throws SQLException {
        listaSeguro.clear();
        PreparedStatement ps = con.prepareStatement(
                "SELECT vps.id_producto, pr.nombre, vps.cantidad, vps.precio_unitario, " +
                        "vps.porcentaje_cobertura " +
                        "FROM TBL_VENTA_PRODUCTO_SEGURO vps " +
                        "JOIN TBL_PRODUCTO pr ON pr.id_producto = vps.id_producto " +
                        "WHERE vps.id_ventaseguro = ?");
        ps.setInt(1, idVS);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            listaSeguro.add(new VentaSeguroItem(
                    rs.getInt("id_producto"), rs.getString("nombre"),
                    rs.getInt("cantidad"), rs.getDouble("precio_unitario"),
                    rs.getDouble("porcentaje_cobertura")));
        }
        tablaSeguro.setItems(listaSeguro);
        actualizarResumenSeguro();
    }

    @FXML
    public void onBuscarProdSeguro(ActionEvent event) {
        if (txtIdProdSeg.getText().isBlank()) { JOptionPane.showMessageDialog(null, "Ingresa el ID del producto."); return; }
        buscarProducto(txtIdProdSeg, txtNombreProdSeg, txtPrecioProdSeg);
    }

    @FXML
    public void onAgregarProdSeguro(ActionEvent event) {
        if (idSeguroCliente == -1) { JOptionPane.showMessageDialog(null, "El cliente no tiene seguro medico registrado."); return; }
        if (txtIdProdSeg.getText().isBlank()) { JOptionPane.showMessageDialog(null, "Ingresa el ID del producto.", "Campo requerido", JOptionPane.WARNING_MESSAGE); return; }
        if (txtNombreProdSeg.getText().isBlank()) { JOptionPane.showMessageDialog(null, "Busca el producto primero.", "Producto no buscado", JOptionPane.WARNING_MESSAGE); txtIdProdSeg.requestFocus(); return; }
        if (txtPrecioProdSeg.getUserData() == null) { JOptionPane.showMessageDialog(null, "Debes buscar el producto antes de agregarlo.", "Producto no validado", JOptionPane.WARNING_MESSAGE); txtIdProdSeg.requestFocus(); return; }
        if (txtCantProdSeg.getText().isBlank() || txtPrecioProdSeg.getText().isBlank()) { JOptionPane.showMessageDialog(null, "Completa todos los campos del producto."); return; }
        if (txtPctCobertura.getText().isBlank()) { JOptionPane.showMessageDialog(null, "Ingresa el porcentaje de cobertura."); return; }

        try {
            int    idProd    = Integer.parseInt(txtIdProdSeg.getText().trim());
            int    cant      = Integer.parseInt(txtCantProdSeg.getText().trim());
            double precio    = Double.parseDouble(txtPrecioProdSeg.getText().trim());
            double precioMax = (double) txtPrecioProdSeg.getUserData();

            if (precio > precioMax + 0.001) {
                JOptionPane.showMessageDialog(null,
                        "El precio supera el precio del sistema.\nMaximo: RD$ " + String.format("%.2f", precioMax),
                        "Precio excedido", JOptionPane.WARNING_MESSAGE);
                txtPrecioProdSeg.setText(String.format("%.2f", precioMax));
                txtPrecioProdSeg.requestFocus(); txtPrecioProdSeg.selectAll();
                return;
            }

            double pct = Math.min(100, Math.max(0, Double.parseDouble(txtPctCobertura.getText().replace("%", "").trim())));
            listaSeguro.add(new VentaSeguroItem(idProd, txtNombreProdSeg.getText().trim(), cant, precio, pct));
            tablaSeguro.setItems(listaSeguro);
            actualizarResumenSeguro();

            txtIdProdSeg.clear(); txtNombreProdSeg.clear(); txtCantProdSeg.clear();
            txtPrecioProdSeg.clear(); txtPrecioProdSeg.setUserData(null);
            txtPrecioProdSeg.setEditable(true);
            txtPrecioProdSeg.setStyle("-fx-background-radius: 6; -fx-border-color: #E0E0E0; -fx-border-radius: 6;");
            txtPctCobertura.setText(String.format("%.0f", coberturaBaseCliente));

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Cantidad, precio y cobertura deben ser numeros.");
        }
    }

    @FXML
    public void onQuitarProdSeguro(ActionEvent event) {
        VentaSeguroItem sel = tablaSeguro.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona un producto."); return; }
        listaSeguro.remove(sel);
        actualizarResumenSeguro();
    }

    @FXML
    public void onRegistrarVentaSeguro(ActionEvent event) {
        if (txtIdClienteSeguro.getText().isBlank())  { JOptionPane.showMessageDialog(null, "ID de cliente obligatorio."); return; }
        if (idSeguroCliente == -1)                   { JOptionPane.showMessageDialog(null, "El cliente no tiene seguro medico."); return; }
        if (txtNumAutorizacion.getText().isBlank())  { JOptionPane.showMessageDialog(null, "El numero de autorizacion es obligatorio."); return; }
        if (listaSeguro.isEmpty())                   { JOptionPane.showMessageDialog(null, "Agrega al menos un producto asegurado."); return; }
        if (chkComprobanteSeguro != null && chkComprobanteSeguro.isSelected()
                && (cmbTipoComprobanteSeguro == null || cmbTipoComprobanteSeguro.getValue() == null)) {
            JOptionPane.showMessageDialog(null, "Selecciona el tipo de comprobante fiscal o desmarca la opcion.",
                    "Comprobante requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double totalVenta = 0, totalAseg = 0, totalCli = 0;
        for (VentaSeguroItem i : listaSeguro) {
            totalVenta += i.getSubtotal();
            totalAseg  += i.getMontoAseguradora();
            totalCli   += i.getMontoCliente();
        }

        double pagoAsegAhora = parsePagado(txtMontoAsegPagado);
        double pagoCliAhora  = parsePagado(txtMontoCliPagado);

        if (pagoAsegAhora > totalAseg + 0.001) {
            JOptionPane.showMessageDialog(null,
                    "El pago de la aseguradora supera su parte (RD$ " + String.format("%.2f", totalAseg) + ").",
                    "Monto excedido", JOptionPane.WARNING_MESSAGE);
            txtMontoAsegPagado.requestFocus(); return;
        }
        if (pagoCliAhora > totalCli + 0.001) {
            JOptionPane.showMessageDialog(null,
                    "El pago del cliente supera su parte (RD$ " + String.format("%.2f", totalCli) + ").",
                    "Monto excedido", JOptionPane.WARNING_MESSAGE);
            txtMontoCliPagado.requestFocus(); return;
        }

        double pendienteAsegFinal = totalAseg - pagoAsegAhora;
        double pendienteCliFinal  = totalCli  - pagoCliAhora;

        try (Connection con = conexion.establecerConexion()) {
            con.setAutoCommit(false);
            try {
                int idComp = asignarComprobanteFiscalSeguro(con);

                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO TBL_VENTA_SEGURO " +
                                "(numero_autorizacion, monto_aprobado, monto_total, " +
                                " monto_pendiente_a, monto_pendiente_cliente, " +
                                " id_empleado, id_cliente, fecha_transaccion, condicion, id_comprobante) " +
                                "VALUES (?,?,?,?,?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, txtNumAutorizacion.getText().trim());
                ps.setDouble(2, totalAseg);
                ps.setDouble(3, totalVenta);
                ps.setDouble(4, pendienteAsegFinal);
                ps.setDouble(5, pendienteCliFinal);
                ps.setInt(6, txtIdEmpleadoSeguro.getText().isBlank() ? 1 : Integer.parseInt(txtIdEmpleadoSeguro.getText().trim()));
                ps.setInt(7, Integer.parseInt(txtIdClienteSeguro.getText().trim()));
                ps.setDate(8, Date.valueOf(dpFechaVentaSeguro.getValue()));
                ps.setString(9, txtCondicionSeguro.getText().trim());
                if (idComp == -1) ps.setNull(10, java.sql.Types.INTEGER);
                else              ps.setInt(10, idComp);
                ps.executeUpdate();

                int idVS = -1;
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) idVS = keys.getInt(1);

                for (VentaSeguroItem item : listaSeguro) {
                    PreparedStatement psD = con.prepareStatement(
                            "INSERT INTO TBL_VENTA_PRODUCTO_SEGURO " +
                                    "(id_ventaseguro, id_producto, cantidad, precio_unitario, " +
                                    " fecha_venta, id_presentacion, porcentaje_cobertura, porcentaje_cliente) " +
                                    "VALUES (?,?,?,?,?,?,?,?)");
                    psD.setInt(1, idVS);
                    psD.setInt(2, item.getIdProducto());
                    psD.setInt(3, item.getCantidad());
                    psD.setDouble(4, item.getPrecioUnitario());
                    psD.setDate(5, Date.valueOf(dpFechaVentaSeguro.getValue()));
                    psD.setInt(6, item.getIdPresentacion());
                    psD.setDouble(7, item.getPorcentajeCobert());
                    psD.setDouble(8, item.getPorcentajeCli());
                    psD.executeUpdate();
                }

                if (pagoAsegAhora > 0) {
                    String metodoAseg = (cmbMetodoPagoAseg != null && cmbMetodoPagoAseg.getValue() != null)
                            ? cmbMetodoPagoAseg.getValue() : "Efectivo";
                    int idPagoAseg = insertarPago(con, "Aseguradora",
                            Date.valueOf(dpFechaVentaSeguro.getValue()), pagoAsegAhora, metodoAseg);
                    insertarPuenteAseguradora(con, idVS, idPagoAseg);
                }

                if (pagoCliAhora > 0) {
                    String metodoCli = (cmbMetodoPagoCli != null && cmbMetodoPagoCli.getValue() != null)
                            ? cmbMetodoPagoCli.getValue() : "Efectivo";
                    int idPagoCli = insertarPago(con, "Cliente",
                            Date.valueOf(dpFechaVentaSeguro.getValue()), pagoCliAhora, metodoCli);
                    insertarPuenteCliente(con, idVS, idPagoCli);
                }

                con.commit();

                String msgNcf = (lblNcfGeneradoSeguro != null && !lblNcfGeneradoSeguro.getText().isBlank())
                        ? "\n" + lblNcfGeneradoSeguro.getText()
                        : "\nSin comprobante fiscal";
                JOptionPane.showMessageDialog(null,
                        "✅ Venta con Seguro #" + idVS + " registrada.\n\n" +
                                "Autorizacion:       " + txtNumAutorizacion.getText().trim() + "\n\n" +
                                "Parte aseguradora:  RD$ " + String.format("%.2f", totalAseg) +
                                "  |  Pagado: RD$ " + String.format("%.2f", pagoAsegAhora) +
                                "  |  Pendiente: RD$ " + String.format("%.2f", pendienteAsegFinal) + "\n" +
                                "Parte cliente:      RD$ " + String.format("%.2f", totalCli) +
                                "  |  Pagado: RD$ " + String.format("%.2f", pagoCliAhora) +
                                "  |  Pendiente: RD$ " + String.format("%.2f", pendienteCliFinal) +
                                msgNcf);
                onLimpiarSeguro(null);

            } catch (Exception ex) {
                con.rollback();
                JOptionPane.showMessageDialog(null, "Error al registrar: " + ex.getMessage());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error de conexion: " + e.getMessage());
        }
    }

    @FXML
    public void onRegistrarPagoSeguro(ActionEvent event) {
        if (idVentaSeguroSeleccionada == -1) {
            JOptionPane.showMessageDialog(null, "Primero busca una venta con seguro por ID.");
            return;
        }

        double pagoAseg = parsePagado(txtMontoAsegPagado);
        double pagoCli  = parsePagado(txtMontoCliPagado);

        if (pagoAseg == 0 && pagoCli == 0) {
            JOptionPane.showMessageDialog(null, "Ingresa al menos un monto de pago.");
            return;
        }

        if (pagoAseg > pendienteAsegActual + 0.001) {
            JOptionPane.showMessageDialog(null,
                    "El pago de la aseguradora supera el pendiente.\n" +
                            "Pendiente: RD$ " + String.format("%.2f", pendienteAsegActual),
                    "Monto excedido", JOptionPane.WARNING_MESSAGE);
            txtMontoAsegPagado.requestFocus(); return;
        }
        if (pagoCli > pendienteCliActual + 0.001) {
            JOptionPane.showMessageDialog(null,
                    "El pago del cliente supera el pendiente.\n" +
                            "Pendiente: RD$ " + String.format("%.2f", pendienteCliActual),
                    "Monto excedido", JOptionPane.WARNING_MESSAGE);
            txtMontoCliPagado.requestFocus(); return;
        }

        try (Connection con = conexion.establecerConexion()) {
            con.setAutoCommit(false);
            try {
                if (pagoAseg > 0) {
                    String metodo = (cmbMetodoPagoAseg != null && cmbMetodoPagoAseg.getValue() != null)
                            ? cmbMetodoPagoAseg.getValue() : "Efectivo";
                    int idPago = insertarPago(con, "Aseguradora",
                            Date.valueOf(java.time.LocalDate.now()), pagoAseg, metodo);
                    insertarPuenteAseguradora(con, idVentaSeguroSeleccionada, idPago);
                    actualizarPendienteEnBD(con, true, pagoAseg);
                    pendienteAsegActual = Math.max(0, pendienteAsegActual - pagoAseg);
                }
                if (pagoCli > 0) {
                    String metodo = (cmbMetodoPagoCli != null && cmbMetodoPagoCli.getValue() != null)
                            ? cmbMetodoPagoCli.getValue() : "Efectivo";
                    int idPago = insertarPago(con, "Cliente",
                            Date.valueOf(java.time.LocalDate.now()), pagoCli, metodo);
                    insertarPuenteCliente(con, idVentaSeguroSeleccionada, idPago);
                    actualizarPendienteEnBD(con, false, pagoCli);
                    pendienteCliActual = Math.max(0, pendienteCliActual - pagoCli);
                }
                con.commit();

                actualizarLabelsPendiente();
                if (txtMontoAsegPagado != null) txtMontoAsegPagado.clear();
                if (txtMontoCliPagado  != null) txtMontoCliPagado.clear();
                if (cmbMetodoPagoAseg  != null) cmbMetodoPagoAseg.setValue(null);
                if (cmbMetodoPagoCli   != null) cmbMetodoPagoCli.setValue(null);

                JOptionPane.showMessageDialog(null,
                        "✅ Pago registrado.\n\n" +
                                "Pendiente aseguradora: RD$ " + String.format("%.2f", pendienteAsegActual) + "\n" +
                                "Pendiente cliente:     RD$ " + String.format("%.2f", pendienteCliActual));

            } catch (Exception ex) {
                con.rollback();
                JOptionPane.showMessageDialog(null, "Error al registrar pago: " + ex.getMessage());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error de conexion: " + e.getMessage());
        }
    }


    private int insertarPago(Connection con, String tipoPago,
                             java.sql.Date fechaPago, double monto,
                             String metodoPago) throws SQLException {
        String sql = "INSERT INTO TBL_PAGO (tipo_pago, fecha_pago, monto_pago, metodo_pago, estado_pago) " +
                "VALUES (?, ?, ?, ?, 1)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tipoPago);
            ps.setDate(2, fechaPago);
            ps.setDouble(3, monto);
            ps.setString(4, metodoPago);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            throw new SQLException("No se obtuvo el ID del pago.");
        }
    }

    private void insertarPuenteAseguradora(Connection con, int idVS, int idPago) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO TBL_PAGO_ASEGURADORA_SEGURO (id_ventaseguro, id_pago) VALUES (?,?)")) {
            ps.setInt(1, idVS); ps.setInt(2, idPago); ps.executeUpdate();
        }
    }

    private void insertarPuenteCliente(Connection con, int idVS, int idPago) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO TBL_PAGO_CLIENTE_SEGURO (id_ventaseguro, id_pago) VALUES (?,?)")) {
            ps.setInt(1, idVS); ps.setInt(2, idPago); ps.executeUpdate();
        }
    }

    private void actualizarPendienteEnBD(Connection con, boolean esAseg, double monto) throws SQLException {
        String campo = esAseg ? "monto_pendiente_a" : "monto_pendiente_cliente";
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE TBL_VENTA_SEGURO SET " + campo + " = " + campo + " - ? WHERE id_ventaseguro = ?")) {
            ps.setDouble(1, monto); ps.setInt(2, idVentaSeguroSeleccionada); ps.executeUpdate();
        }
    }

    @FXML
    public void onEditarVentaSeguro(ActionEvent event) {
        if (idVentaSeguroSeleccionada == -1) { JOptionPane.showMessageDialog(null, "Primero busca una venta con seguro."); return; }
        double totalVenta = 0, totalAseg = 0, totalCli = 0;
        for (VentaSeguroItem i : listaSeguro) {
            totalVenta += i.getSubtotal(); totalAseg += i.getMontoAseguradora(); totalCli += i.getMontoCliente();
        }
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE TBL_VENTA_SEGURO SET numero_autorizacion=?,monto_aprobado=?,monto_total=?," +
                             "monto_pendiente_a=?,monto_pendiente_cliente=?," +
                             "id_empleado=?,id_cliente=?,fecha_transaccion=?,condicion=? " +
                             "WHERE id_ventaseguro=?")) {
            ps.setString(1, txtNumAutorizacion.getText().trim());
            ps.setDouble(2, totalAseg);
            ps.setDouble(3, totalVenta);
            ps.setDouble(4, pendienteAsegActual);
            ps.setDouble(5, pendienteCliActual);
            ps.setInt(6, txtIdEmpleadoSeguro.getText().isBlank() ? 1 : Integer.parseInt(txtIdEmpleadoSeguro.getText().trim()));
            ps.setInt(7, Integer.parseInt(txtIdClienteSeguro.getText().trim()));
            ps.setDate(8, Date.valueOf(dpFechaVentaSeguro.getValue()));
            ps.setString(9, txtCondicionSeguro.getText().trim());
            ps.setInt(10, idVentaSeguroSeleccionada);
            ps.executeUpdate();

            con.prepareStatement("DELETE FROM TBL_VENTA_PRODUCTO_SEGURO WHERE id_ventaseguro=" + idVentaSeguroSeleccionada).executeUpdate();

            for (VentaSeguroItem item : listaSeguro) {
                PreparedStatement psD = con.prepareStatement(
                        "INSERT INTO TBL_VENTA_PRODUCTO_SEGURO " +
                                "(id_ventaseguro, id_producto, cantidad, precio_unitario, " +
                                " fecha_venta, id_presentacion, porcentaje_cobertura, porcentaje_cliente) " +
                                "VALUES (?,?,?,?,?,?,?,?)");
                psD.setInt(1, idVentaSeguroSeleccionada); psD.setInt(2, item.getIdProducto());
                psD.setInt(3, item.getCantidad()); psD.setDouble(4, item.getPrecioUnitario());
                psD.setDate(5, Date.valueOf(dpFechaVentaSeguro.getValue()));
                psD.setInt(6, item.getIdPresentacion());
                psD.setDouble(7, item.getPorcentajeCobert()); psD.setDouble(8, item.getPorcentajeCli());
                psD.executeUpdate();
            }
            JOptionPane.showMessageDialog(null, "Venta con Seguro #" + idVentaSeguroSeleccionada + " actualizada.");
            onLimpiarSeguro(null);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    @FXML
    public void onLimpiarSeguro(ActionEvent event) {
        txtIdVentaSeguro.clear(); txtIdClienteSeguro.clear(); txtIdEmpleadoSeguro.clear();
        txtNumAutorizacion.clear(); txtCondicionSeguro.clear();
        txtIdProdSeg.clear(); txtNombreProdSeg.clear(); txtCantProdSeg.clear();
        if (txtPrecioProdSeg != null) {
            txtPrecioProdSeg.clear(); txtPrecioProdSeg.setUserData(null);
            txtPrecioProdSeg.setEditable(true);
            txtPrecioProdSeg.setStyle("-fx-background-radius: 6; -fx-border-color: #E0E0E0; -fx-border-radius: 6;");
        }
        txtPctCobertura.clear();
        if (txtMontoAsegPagado != null) txtMontoAsegPagado.clear();
        if (txtMontoCliPagado  != null) txtMontoCliPagado.clear();
        if (cmbMetodoPagoAseg  != null) cmbMetodoPagoAseg.setValue(null);
        if (cmbMetodoPagoCli   != null) cmbMetodoPagoCli.setValue(null);
        if (lblInfoVentaSeguro     != null) lblInfoVentaSeguro.setText("");
        if (lblNombreClienteSeguro != null) lblNombreClienteSeguro.setText("");
        if (cardSeguroCliente      != null) { cardSeguroCliente.setVisible(false); cardSeguroCliente.setManaged(false); }
        if (lblSinSeguro           != null) { lblSinSeguro.setVisible(false); lblSinSeguro.setManaged(false); }
        dpFechaVentaSeguro.setValue(java.time.LocalDate.now());
        listaSeguro.clear();
        idVentaSeguroSeleccionada = -1; idSeguroCliente = -1; coberturaBaseCliente = 0;
        pendienteAsegActual = 0; pendienteCliActual = 0;
        if (lblTotalSeguro        != null) lblTotalSeguro.setText("RD$ 0.00");
        if (lblMontoAseguradora   != null) lblMontoAseguradora.setText("RD$ 0.00");
        if (lblMontoClienteSeguro != null) lblMontoClienteSeguro.setText("RD$ 0.00");
        if (lblPendienteAseg      != null) { lblPendienteAseg.setText("RD$ 0.00"); lblPendienteAseg.setStyle(""); }
        if (lblPendienteCli       != null) { lblPendienteCli.setText("RD$ 0.00");  lblPendienteCli.setStyle(""); }

        if (chkComprobanteSeguro     != null) chkComprobanteSeguro.setSelected(false);
        if (cmbTipoComprobanteSeguro != null) cmbTipoComprobanteSeguro.setValue(null);
        if (lblNcfGeneradoSeguro     != null) lblNcfGeneradoSeguro.setText("");
        if (panelComprobanteSeguro   != null) { panelComprobanteSeguro.setVisible(false); panelComprobanteSeguro.setManaged(false); }
    }

    private void actualizarResumenSeguro() {
        double total = 0, aseg = 0, cli = 0;
        for (VentaSeguroItem i : listaSeguro) {
            total += i.getSubtotal(); aseg += i.getMontoAseguradora(); cli += i.getMontoCliente();
        }
        if (lblTotalSeguro        != null) lblTotalSeguro.setText("RD$ "        + String.format("%.2f", total));
        if (lblMontoAseguradora   != null) lblMontoAseguradora.setText("RD$ "   + String.format("%.2f", aseg));
        if (lblMontoClienteSeguro != null) lblMontoClienteSeguro.setText("RD$ " + String.format("%.2f", cli));
    }


    private void buscarProducto(TextField fldId, TextField fldNombre, TextField fldPrecio) {
        String sql = "SELECT p.nombre, ISNULL((SELECT TOP 1 precio_venta " +
                "FROM TBL_PRESENTACION_PRODUCTO WHERE id_producto = p.id_producto " +
                "ORDER BY id_presentacion ASC), 0) AS precio_venta " +
                "FROM TBL_PRODUCTO p WHERE p.id_producto = ?";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(fldId.getText().trim()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                fldNombre.setText(rs.getString("nombre"));
                double precio = rs.getDouble("precio_venta");
                fldPrecio.setText(precio > 0 ? String.format("%.2f", precio) : "0.00");
                fldPrecio.setUserData(precio);
                fldPrecio.setEditable(false);
                fldPrecio.setStyle("-fx-background-color: #F1F8E9; -fx-background-radius: 6; -fx-border-color: #C8E6C9; -fx-border-radius: 6;");
            } else {
                JOptionPane.showMessageDialog(null, "Producto no encontrado.");
                fldNombre.clear(); fldPrecio.clear(); fldPrecio.setUserData(null);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private double parsePagado(TextField field) {
        if (field == null || field.getText().isBlank()) return 0;
        try { return Double.parseDouble(field.getText().trim()); } catch (NumberFormatException e) { return 0; }
    }

    private void formatMoneda(TableColumn<VentaSeguroItem, Number> col) {
        col.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : "RD$ " + String.format("%.2f", item.doubleValue()));
            }
        });
    }

    private void formatPct(TableColumn<VentaSeguroItem, Number> col) {
        col.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : String.format("%.0f", item.doubleValue()) + " %");
            }
        });
    }

    public void restaurarEstadoEn(HelloController destino) {
        destino.listaTemporal.setAll(this.listaTemporal);
        destino.tablaVentaProducto.setItems(destino.listaTemporal);
        destino.idVentaSeleccionada = this.idVentaSeleccionada;
        if (this.txtIdVenta     != null && destino.txtIdVenta     != null) destino.txtIdVenta.setText(this.txtIdVenta.getText());
        if (this.txtIdCliente   != null && destino.txtIdCliente   != null) destino.txtIdCliente.setText(this.txtIdCliente.getText());
        if (this.txtIdEmpleado  != null && destino.txtIdEmpleado  != null) destino.txtIdEmpleado.setText(this.txtIdEmpleado.getText());
        if (this.txtCondicion   != null && destino.txtCondicion   != null) destino.txtCondicion.setText(this.txtCondicion.getText());
        if (this.txtMontoPagado != null && destino.txtMontoPagado != null) destino.txtMontoPagado.setText(this.txtMontoPagado.getText());
        if (this.cmbTipoVenta   != null && destino.cmbTipoVenta   != null) destino.cmbTipoVenta.setValue(this.cmbTipoVenta.getValue());
        if (this.dpFechaVenta   != null && destino.dpFechaVenta   != null) destino.dpFechaVenta.setValue(this.dpFechaVenta.getValue());
        if (this.lblInfoVenta   != null && destino.lblInfoVenta   != null) {
            destino.lblInfoVenta.setText(this.lblInfoVenta.getText());
            destino.lblInfoVenta.setStyle(this.lblInfoVenta.getStyle());
        }
        destino.actualizarTotalesNormal();
    }


    private JasperPrint generarReporteVentaSeguro(int idVentaSeguro) throws Exception {
        Connection connection = null;
        try {
            InputStream reportStream = getClass()
                    .getResourceAsStream("/reports/FacturaSeguro.jrxml");

            if (reportStream == null) {
                throw new Exception("No se encontró el archivo del reporte FacturaSeguro.jrxml");
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ID_VENTA_SEGURO", idVentaSeguro);
            parameters.put("LOGO_STREAM", getClass().getResourceAsStream("/reports/logusfarmablanco.png"));

            connection = conexion.establecerConexion();

            return JasperFillManager.fillReport(jasperReport, parameters, connection);
        } finally {
            if (connection != null) {
                try { connection.close(); } catch (Exception ignored) {}
            }
        }
    }


    @FXML
    public void onImprimirVentaSeguro(ActionEvent event) {
        if (idVentaSeguroSeleccionada == -1) {
            if (listaSeguro.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "No hay una venta con seguro seleccionada.\n" +
                                "Primero registra o busca una venta con seguro.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null,
                        "Hay productos en la tabla pero la venta no está guardada.\n\n" +
                                "Para imprimir:\n" +
                                "1. Registra la venta con 'Registrar Venta'\n" +
                                "2. O busca una venta existente por ID\n" +
                                "3. Luego presiona 'Imprimir Factura'",
                        "Venta no registrada", JOptionPane.WARNING_MESSAGE);
            }
            return;
        }
        try {
            JasperPrint jasperPrint = generarReporteVentaSeguro(idVentaSeguroSeleccionada);
            JasperViewer viewer = new JasperViewer(jasperPrint, false);
            viewer.setTitle("Factura Seguro #" + String.format("%06d", idVentaSeguroSeleccionada) + " — UsFarma");
            viewer.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error al generar el reporte:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @FXML
    public void onEnviarFacturaSeguro(ActionEvent event) {
        if (idVentaSeguroSeleccionada == -1) {
            JOptionPane.showMessageDialog(null,
                    "No hay una venta con seguro seleccionada.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            JasperPrint jasperPrint = generarReporteVentaSeguro(idVentaSeguroSeleccionada);
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Enviar Factura por Correo");
            dialog.setHeaderText("Ingrese el correo del destinatario:");
            dialog.setContentText("Correo:");
            String destinatario = dialog.showAndWait().orElse(null);
            if (destinatario == null || destinatario.isBlank()) return;

            byte[] pdf = JasperExportManager.exportReportToPdf(jasperPrint);
            EmailService emailService = new EmailService(
                    "smtp.gmail.com", "587",
                    "2230006@ipisa.edu.do", "dfjy zlqx nsve idyf");
            emailService.enviarConReporte(destinatario,
                    "Factura Seguro #" + String.format("%06d", idVentaSeguroSeleccionada),
                    "Adjunto la factura de venta con seguro.",
                    "FacturaSeguro_" + idVentaSeguroSeleccionada + ".pdf",
                    new java.io.ByteArrayInputStream(pdf));
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error al enviar la factura:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}