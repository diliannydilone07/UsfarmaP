package com.example.farmaventa;

import com.example.farmaventa.database.Conexion;
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

import javax.swing.JOptionPane;
import java.sql.*;

public class HelloController {

    Conexion conexion = new Conexion();

    // ══ HEADER ═══════════════════════════════════════════════════════════
    @FXML private TabPane tabPane;
    @FXML private Label   lblTipoActivo;

    // ══════════════════════════════════════════════════════════════════════
    // VENTA NORMAL
    // ══════════════════════════════════════════════════════════════════════
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

    @FXML private TableView<Venta>           tablaVentaProducto;
    @FXML private TableColumn<Venta, Number> colVentaId;
    @FXML private TableColumn<Venta, String> colVentaCliente;
    @FXML private TableColumn<Venta, String> colProductoNombre;
    @FXML private TableColumn<Venta, Number> colProductoCantidad;
    @FXML private TableColumn<Venta, Number> colProductoPrecio;
    @FXML private TableColumn<Venta, Number> colProductoSubtotal;

    private ObservableList<Venta> listaTemporal = FXCollections.observableArrayList();
    private int idVentaSeleccionada = -1;

    // ══════════════════════════════════════════════════════════════════════
    // VENTA CON SEGURO
    // ══════════════════════════════════════════════════════════════════════
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

    // ══ INITIALIZE ═══════════════════════════════════════════════════════
    @FXML
    public void initialize() {

        // ── Venta Normal ──────────────────────────────────────────────
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

        // ── Venta con Seguro ──────────────────────────────────────────
        dpFechaVentaSeguro.setValue(java.time.LocalDate.now());

        colSegProd.setCellValueFactory(c -> c.getValue().productoProperty());
        colSegCant.setCellValueFactory(c -> c.getValue().cantidadProperty());
        colSegPrecio.setCellValueFactory(c -> c.getValue().precioUnitarioProperty());
        colSegSub.setCellValueFactory(c -> c.getValue().subtotalProperty());
        colSegPctCob.setCellValueFactory(c -> c.getValue().porcentajeCobertProperty());
        colSegPctCli.setCellValueFactory(c -> c.getValue().porcentajeCliProperty());
        colSegMtoAseg.setCellValueFactory(c -> c.getValue().montoAseguradoraProperty());
        colSegMtoCli.setCellValueFactory(c -> c.getValue().montoClienteProperty());

        formatMoneda(colSegPrecio); formatMoneda(colSegSub);
        formatMoneda(colSegMtoAseg); formatMoneda(colSegMtoCli);
        formatPct(colSegPctCob); formatPct(colSegPctCli);

        tablaSeguro.setItems(listaSeguro);

        txtIdClienteSeguro.focusedProperty().addListener((obs, o, n) -> {
            if (!n && !txtIdClienteSeguro.getText().isBlank()) buscarInfoClienteSeguro();
        });

        // ── Tab indicator ─────────────────────────────────────────────
        if (tabPane != null) {
            tabPane.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
                if (n == null || lblTipoActivo == null) return;
                int idx = tabPane.getSelectionModel().getSelectedIndex();
                lblTipoActivo.setText(idx == 1 ? "Venta con Seguro" : "Venta Normal");
            });
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // VENTA NORMAL
    // ══════════════════════════════════════════════════════════════════════

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
                    lblInfoVenta.setStyle("-fx-text-fill: #C62828; -fx-font-size: 11px;"); return;
                }
                idVentaSeleccionada = rs.getInt("id_venta");
                txtIdCliente.setText(String.valueOf(rs.getInt("id_cliente")));
                txtIdEmpleado.setText(String.valueOf(rs.getInt("id_empleado")));
                cmbTipoVenta.setValue(rs.getString("tipo_venta"));
                txtCondicion.setText(rs.getString("condicion") != null ? rs.getString("condicion") : "");
                try { dpFechaVenta.setValue(java.time.LocalDate.parse(rs.getString("fecha"))); } catch (Exception ignored) {}
                double mt = rs.getDouble("monto_total"), mp = rs.getDouble("monto_pendiente");
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
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, "Error: " + e.getMessage()); }
    }

    private void cargarProductosDeVenta(Connection con, int idVenta) throws SQLException {
        listaTemporal.clear();
        PreparedStatement ps = con.prepareStatement(
                "SELECT vp.id_producto, pr.nombre, vp.cantidad, vp.precio_unitario " +
                        "FROM TBL_VENTA_PRODUCTO vp " +
                        "JOIN TBL_PRODUCTO pr ON pr.id_producto = vp.id_producto WHERE vp.id_venta = ?");
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
        if (txtIdProducto.getText().isBlank()) { JOptionPane.showMessageDialog(null, "Ingresa el ID del producto."); return; }
        buscarProducto(txtIdProducto, txtNombreProducto, txtPrecioProducto);
    }

    @FXML
    public void onAgregarProducto(ActionEvent event) {
        if (txtIdProducto.getText().isBlank() || txtNombreProducto.getText().isBlank()
                || txtCantidadProducto.getText().isBlank() || txtPrecioProducto.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Completa todos los campos del producto."); return;
        }
        try {
            int    idProd = Integer.parseInt(txtIdProducto.getText().trim());
            int    cant   = Integer.parseInt(txtCantidadProducto.getText().trim());
            double precio = Double.parseDouble(txtPrecioProducto.getText().trim());
            listaTemporal.add(new Venta(idProd, "", precio, cant, precio * cant,
                    txtNombreProducto.getText().trim(), "", "", "", 0, 0, ""));
            tablaVentaProducto.setItems(listaTemporal);
            actualizarTotalesNormal();
            txtIdProducto.clear(); txtNombreProducto.clear(); txtCantidadProducto.clear();
            if (txtPrecioProducto != null) {
                txtPrecioProducto.clear();
                txtPrecioProducto.setStyle("-fx-background-color: #F1F8E9; -fx-background-radius: 6; -fx-border-color: #C8E6C9; -fx-border-radius: 6;");
            }
        } catch (NumberFormatException e) { JOptionPane.showMessageDialog(null, "Cantidad y precio deben ser numeros."); }
    }

    @FXML
    public void fnBuscarProducto(ActionEvent event) {
        String b = txtBuscarProducto.getText().trim().toLowerCase();
        if (b.isEmpty()) { tablaVentaProducto.setItems(listaTemporal); return; }
        ObservableList<Venta> f = FXCollections.observableArrayList();
        for (Venta v : listaTemporal) if (v.getProducto().toLowerCase().contains(b)) f.add(v);
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
        } catch (Exception e) { JOptionPane.showMessageDialog(null, "Error al abrir catalogo: " + e.getMessage()); }
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

        double montoTotal = calcTotalNormal();
        double pendiente  = Math.max(0, montoTotal - parsePagado(txtMontoPagado));

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO TBL_VENTA (id_empleado, tipo_venta, fecha_transaccion, " +
                             "monto_total, monto_pendiente, condicion, id_cliente) VALUES (?,?,?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1,    txtIdEmpleado.getText().isBlank() ? 1 : Integer.parseInt(txtIdEmpleado.getText().trim()));
            ps.setString(2, cmbTipoVenta.getValue());
            ps.setDate(3,   Date.valueOf(dpFechaVenta.getValue()));
            ps.setDouble(4, montoTotal); ps.setDouble(5, pendiente);
            ps.setString(6, txtCondicion.getText().trim());
            ps.setInt(7,    Integer.parseInt(txtIdCliente.getText().trim()));
            ps.executeUpdate();
            int idVenta = -1;
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) idVenta = keys.getInt(1);
            for (Venta v : listaTemporal) {
                PreparedStatement psD = con.prepareStatement(
                        "INSERT INTO TBL_VENTA_PRODUCTO " +
                                "(id_venta, id_producto, cantidad, precio_unitario, fecha_venta, id_presentacion) " +
                                "VALUES (?,?,?,?,?,1)");
                psD.setInt(1, idVenta); psD.setInt(2, v.getIdVenta()); psD.setInt(3, v.getCantidad());
                psD.setDouble(4, v.getTotal()); psD.setDate(5, Date.valueOf(dpFechaVenta.getValue()));
                psD.executeUpdate();
            }
            JOptionPane.showMessageDialog(null, "Venta #" + idVenta + " registrada correctamente.");
            onLimpiarVenta(null);
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, "Error al registrar venta: " + e.getMessage()); }
    }

    @FXML
    public void onEditarVenta(ActionEvent event) {
        if (idVentaSeleccionada == -1) { JOptionPane.showMessageDialog(null, "Primero busca una venta por ID."); return; }
        if (txtIdCliente.getText().isBlank() || cmbTipoVenta.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Cliente y tipo de venta son obligatorios."); return;
        }
        double montoTotal = calcTotalNormal();
        double pendiente  = Math.max(0, montoTotal - parsePagado(txtMontoPagado));
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE TBL_VENTA SET id_empleado=?,tipo_venta=?,fecha_transaccion=?," +
                             "monto_total=?,monto_pendiente=?,condicion=?,id_cliente=? WHERE id_venta=?")) {
            ps.setInt(1, txtIdEmpleado.getText().isBlank() ? 1 : Integer.parseInt(txtIdEmpleado.getText().trim()));
            ps.setString(2, cmbTipoVenta.getValue()); ps.setDate(3, Date.valueOf(dpFechaVenta.getValue()));
            ps.setDouble(4, montoTotal); ps.setDouble(5, pendiente);
            ps.setString(6, txtCondicion.getText().trim());
            ps.setInt(7, Integer.parseInt(txtIdCliente.getText().trim()));
            ps.setInt(8, idVentaSeleccionada); ps.executeUpdate();
            con.prepareStatement("DELETE FROM TBL_VENTA_PRODUCTO WHERE id_venta=" + idVentaSeleccionada).executeUpdate();
            for (Venta v : listaTemporal) {
                PreparedStatement psD = con.prepareStatement(
                        "INSERT INTO TBL_VENTA_PRODUCTO " +
                                "(id_venta, id_producto, cantidad, precio_unitario, fecha_venta, id_presentacion) " +
                                "VALUES (?,?,?,?,?,1)");
                psD.setInt(1, idVentaSeleccionada); psD.setInt(2, v.getIdVenta()); psD.setInt(3, v.getCantidad());
                psD.setDouble(4, v.getTotal()); psD.setDate(5, Date.valueOf(dpFechaVenta.getValue()));
                psD.executeUpdate();
            }
            JOptionPane.showMessageDialog(null, "Venta #" + idVentaSeleccionada + " actualizada.");
            onLimpiarVenta(null);
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, "Error al editar venta: " + e.getMessage()); }
    }

    @FXML
    public void onLimpiarVenta(ActionEvent event) {
        txtIdVenta.clear(); txtIdCliente.clear(); txtIdEmpleado.clear();
        txtCondicion.clear(); txtMontoPagado.clear();
        txtIdProducto.clear(); txtNombreProducto.clear(); txtCantidadProducto.clear();
        if (txtPrecioProducto != null) txtPrecioProducto.clear();
        if (txtBuscarProducto != null) txtBuscarProducto.clear();
        if (lblInfoVenta      != null) lblInfoVenta.setText("");
        if (lblNombreCliente  != null) lblNombreCliente.setText("");
        if (lblSeguroCliente  != null) lblSeguroCliente.setText("");
        cmbTipoVenta.setValue(null);
        dpFechaVenta.setValue(java.time.LocalDate.now());
        listaTemporal.clear();
        idVentaSeleccionada = -1;
        lblMontoTotal.setText("RD$ 0.00");
        lblMontoPendiente.setText("RD$ 0.00");
        lblCantProductos.setText("0 productos");
    }

    // Mantener compatibilidad con SelectorProductoController si usa Limpiar()
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
        double t = 0; for (Venta v : listaTemporal) t += v.getSubtotal(); return t;
    }

    // ══════════════════════════════════════════════════════════════════════
    // VENTA CON SEGURO
    // ══════════════════════════════════════════════════════════════════════

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
                buscarInfoClienteSeguro();
                cargarProductosVentaSeguro(con, idVS);
            }
        } catch (NumberFormatException e) {
            lblInfoVentaSeguro.setText("ID invalido.");
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, "Error: " + e.getMessage()); }
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
        if (txtIdProdSeg.getText().isBlank() || txtNombreProdSeg.getText().isBlank()
                || txtCantProdSeg.getText().isBlank() || txtPrecioProdSeg.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Completa todos los campos del producto."); return;
        }
        if (txtPctCobertura.getText().isBlank()) { JOptionPane.showMessageDialog(null, "Ingresa el porcentaje de cobertura."); return; }
        try {
            int    idProd = Integer.parseInt(txtIdProdSeg.getText().trim());
            int    cant   = Integer.parseInt(txtCantProdSeg.getText().trim());
            double precio = Double.parseDouble(txtPrecioProdSeg.getText().trim());
            double pct    = Math.min(100, Math.max(0,
                    Double.parseDouble(txtPctCobertura.getText().replace("%", "").trim())));
            listaSeguro.add(new VentaSeguroItem(idProd, txtNombreProdSeg.getText().trim(), cant, precio, pct));
            tablaSeguro.setItems(listaSeguro);
            actualizarResumenSeguro();
            txtIdProdSeg.clear(); txtNombreProdSeg.clear(); txtCantProdSeg.clear(); txtPrecioProdSeg.clear();
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

        double totalVenta = 0, totalAseg = 0, totalCli = 0;
        for (VentaSeguroItem i : listaSeguro) {
            totalVenta += i.getSubtotal();
            totalAseg  += i.getMontoAseguradora();
            totalCli   += i.getMontoCliente();
        }

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO TBL_VENTA_SEGURO " +
                             "(numero_autorizacion, monto_aprobado, monto_total, monto_pendiente, " +
                             " id_empleado, id_cliente, fecha_transaccion, condicion) " +
                             "VALUES (?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, txtNumAutorizacion.getText().trim());
            ps.setDouble(2, totalAseg);
            ps.setDouble(3, totalVenta);
            ps.setDouble(4, totalCli);
            ps.setInt(5,    txtIdEmpleadoSeguro.getText().isBlank() ? 1
                    : Integer.parseInt(txtIdEmpleadoSeguro.getText().trim()));
            ps.setInt(6,    Integer.parseInt(txtIdClienteSeguro.getText().trim()));
            ps.setDate(7,   Date.valueOf(dpFechaVentaSeguro.getValue()));
            ps.setString(8, txtCondicionSeguro.getText().trim());
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
                psD.setInt(1,    idVS);
                psD.setInt(2,    item.getIdProducto());
                psD.setInt(3,    item.getCantidad());
                psD.setDouble(4, item.getPrecioUnitario());
                psD.setDate(5,   Date.valueOf(dpFechaVentaSeguro.getValue()));
                psD.setInt(6,    item.getIdPresentacion());
                psD.setDouble(7, item.getPorcentajeCobert());
                psD.setDouble(8, item.getPorcentajeCli());
                psD.executeUpdate();
            }

            JOptionPane.showMessageDialog(null,
                    "Venta con Seguro #" + idVS + " registrada.\n\n" +
                            "Autorizacion: " + txtNumAutorizacion.getText().trim() + "\n\n" +
                            "FACTURA 1 - Aseguradora:  RD$ " + String.format("%.2f", totalAseg) + "\n" +
                            "FACTURA 2 - Cliente:       RD$ " + String.format("%.2f", totalCli));
            onLimpiarSeguro(null);

        } catch (SQLException e) { JOptionPane.showMessageDialog(null, "Error al registrar: " + e.getMessage()); }
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
                             "monto_pendiente=?,id_empleado=?,id_cliente=?,fecha_transaccion=?,condicion=? " +
                             "WHERE id_ventaseguro=?")) {
            ps.setString(1, txtNumAutorizacion.getText().trim());
            ps.setDouble(2, totalAseg); ps.setDouble(3, totalVenta); ps.setDouble(4, totalCli);
            ps.setInt(5, txtIdEmpleadoSeguro.getText().isBlank() ? 1
                    : Integer.parseInt(txtIdEmpleadoSeguro.getText().trim()));
            ps.setInt(6, Integer.parseInt(txtIdClienteSeguro.getText().trim()));
            ps.setDate(7, Date.valueOf(dpFechaVentaSeguro.getValue()));
            ps.setString(8, txtCondicionSeguro.getText().trim());
            ps.setInt(9, idVentaSeguroSeleccionada);
            ps.executeUpdate();

            con.prepareStatement("DELETE FROM TBL_VENTA_PRODUCTO_SEGURO WHERE id_ventaseguro="
                    + idVentaSeguroSeleccionada).executeUpdate();

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
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, "Error: " + e.getMessage()); }
    }

    @FXML
    public void onLimpiarSeguro(ActionEvent event) {
        txtIdVentaSeguro.clear(); txtIdClienteSeguro.clear(); txtIdEmpleadoSeguro.clear();
        txtNumAutorizacion.clear(); txtCondicionSeguro.clear();
        txtIdProdSeg.clear(); txtNombreProdSeg.clear(); txtCantProdSeg.clear();
        txtPrecioProdSeg.clear(); txtPctCobertura.clear();
        if (lblInfoVentaSeguro     != null) lblInfoVentaSeguro.setText("");
        if (lblNombreClienteSeguro != null) lblNombreClienteSeguro.setText("");
        if (cardSeguroCliente      != null) { cardSeguroCliente.setVisible(false); cardSeguroCliente.setManaged(false); }
        if (lblSinSeguro           != null) { lblSinSeguro.setVisible(false); lblSinSeguro.setManaged(false); }
        dpFechaVentaSeguro.setValue(java.time.LocalDate.now());
        listaSeguro.clear();
        idVentaSeguroSeleccionada = -1; idSeguroCliente = -1; coberturaBaseCliente = 0;
        if (lblTotalSeguro        != null) lblTotalSeguro.setText("RD$ 0.00");
        if (lblMontoAseguradora   != null) lblMontoAseguradora.setText("RD$ 0.00");
        if (lblMontoClienteSeguro != null) lblMontoClienteSeguro.setText("RD$ 0.00");
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

    // ══ HELPERS COMPARTIDOS ═══════════════════════════════════════════════

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
                fldPrecio.setText(precio > 0 ? String.format("%.2f", precio) : "");
                fldPrecio.setEditable(false);
                fldPrecio.setStyle("-fx-background-color: #F1F8E9; -fx-background-radius: 6; -fx-border-color: #C8E6C9; -fx-border-radius: 6;");
            } else {
                JOptionPane.showMessageDialog(null, "Producto no encontrado.");
                fldNombre.clear(); fldPrecio.clear();
            }
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, "Error: " + e.getMessage()); }
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
                setText((empty || item == null) ? null : String.format("%.0f", item.doubleValue()) + " pct");
            }
        });
    }

    // Compatibilidad con restaurarEstadoEn si se usa desde SelectorProducto
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
}