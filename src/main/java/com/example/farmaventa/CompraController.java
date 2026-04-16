package com.example.farmaventa;

import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.Compra;
import com.example.farmaventa.modelo.Pago;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import javax.swing.JOptionPane;
import java.sql.*;
import java.util.LinkedHashMap;

public class CompraController {

    Conexion conexion = new Conexion();

    // ── Formulario Compra ─────────────────────────────────────────────────
    @FXML private TextField        txtIdCompra;
    @FXML private Label            lblInfoCompra;
    @FXML private TextField        txtIdProveedor;
    @FXML private TextField        txtIdEmpleado;
    @FXML private ComboBox<String> cmbTipoCompra;
    @FXML private DatePicker       dpFechaCompra;
    @FXML private TextField        txtCondicion;
    @FXML private TextField        txtMontoPagado;
    @FXML private Label            lblMontoTotal;
    @FXML private Label            lblMontoPendiente;
    @FXML private Label            lblCantProductos;
    @FXML private TextField        txtBuscarProducto;

    // ── Campos para agregar producto manualmente ───────────────────────────
    @FXML private TextField txtIdProducto;
    @FXML private TextField txtNombreProducto;
    @FXML private TextField txtCantidadProducto;
    @FXML private TextField txtPrecioProducto;

    // ── Tabla Productos ───────────────────────────────────────────────────
    @FXML private TableView<Compra>           tablaCompraProducto;
    @FXML private TableColumn<Compra, Number> colCompraId;
    @FXML private TableColumn<Compra, String> colProveedor;
    @FXML private TableColumn<Compra, String> colProductoNombre;
    @FXML private TableColumn<Compra, Number> colProductoCantidad;
    @FXML private TableColumn<Compra, Number> colProductoPrecio;
    @FXML private TableColumn<Compra, Number> colProductoSubtotal;

    // ── Formulario Pago ───────────────────────────────────────────────────
    @FXML private TextField        txtIdCompraPago;
    @FXML private ComboBox<String> cmbMetodoPago;
    @FXML private ComboBox<String> cmbCuenta;
    @FXML private DatePicker       dpFechaPago;
    @FXML private TextField        txtMontoPago;
    @FXML private Label            lblResumenPago;

    // ── Tabla Pagos ───────────────────────────────────────────────────────
    @FXML private TableView<Pago>           tablaPagos;
    @FXML private TableColumn<Pago, Number> colPagoId;
    @FXML private TableColumn<Pago, String> colPagoFecha;
    @FXML private TableColumn<Pago, Number> colPagoMonto;
    @FXML private TableColumn<Pago, String> colPagoMetodo;
    @FXML private TableColumn<Pago, String> colPagoCuenta;
    @FXML private TableColumn<Pago, String> colPagoEstado;

    // ── Datos internos ────────────────────────────────────────────────────
    private ObservableList<Compra> listaTemporal  = FXCollections.observableArrayList();
    private ObservableList<Pago>   listaPagos     = FXCollections.observableArrayList();
    private int idCompraSeleccionada  = -1;
    private int idPedidoCSeleccionado = -1;
    private LinkedHashMap<String, Integer> mapaCuentas = new LinkedHashMap<>();

    // ── Inicializar ───────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        cmbTipoCompra.getItems().addAll("Contado", "Credito", "Consignacion");
        dpFechaCompra.setValue(java.time.LocalDate.now());
        lblMontoTotal.setText("RD$ 0.00");
        lblMontoPendiente.setText("RD$ 0.00");
        lblCantProductos.setText("0 productos");

        colCompraId.setCellValueFactory(c -> c.getValue().idCompraProperty());
        colProveedor.setCellValueFactory(c -> c.getValue().proveedorProperty());
        colProductoNombre.setCellValueFactory(c -> c.getValue().productoProperty());
        colProductoCantidad.setCellValueFactory(c -> c.getValue().cantidadProperty());
        colProductoPrecio.setCellValueFactory(c -> c.getValue().precioUnitarioProperty());
        colProductoSubtotal.setCellValueFactory(c -> c.getValue().subtotalProperty());
        tablaCompraProducto.setItems(listaTemporal);

        cmbMetodoPago.getItems().addAll("Efectivo", "Transferencia", "Tarjeta", "Cheque");
        dpFechaPago.setValue(java.time.LocalDate.now());
        cargarCuentas();

        colPagoId.setCellValueFactory(p -> p.getValue().idPagoProperty());
        colPagoFecha.setCellValueFactory(p -> p.getValue().fechaProperty());
        colPagoMonto.setCellValueFactory(p -> p.getValue().montoProperty());
        colPagoMetodo.setCellValueFactory(p -> p.getValue().metodoPagoProperty());
        colPagoCuenta.setCellValueFactory(p -> p.getValue().cuentaProperty());
        colPagoEstado.setCellValueFactory(p -> p.getValue().estadoProperty());
        tablaPagos.setItems(listaPagos);
    }

    // ── Abrir catálogo de productos ───────────────────────────────────────
    @FXML
    public void onAbrirCatalogo(ActionEvent event) {
        try {
            Node nodo = (Node) event.getSource();
            StackPane contentArea = (StackPane) nodo.getScene().lookup("#contentArea");
            if (contentArea == null) {
                JOptionPane.showMessageDialog(null, "No se pudo encontrar el área de contenido."); return;
            }
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/farmaventa/SelectorProductoCompra.fxml"));
            Node selectorVista = loader.load();
            SelectorProductoCompraController selectorCtrl = loader.getController();
            selectorCtrl.init(this, contentArea, construirInfoCompraActual());
            contentArea.getChildren().setAll(selectorVista);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al abrir el catálogo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Llamado por SelectorProductoCompraController cuando el usuario elige un producto.
     * Precio pre-llenado desde la BD — no requiere ingreso manual.
     */
    public void recibirProductoDelCatalogo(int idProducto, String nombre, int cantidad, double precio) {
        listaTemporal.add(new Compra(
                idCompraSeleccionada == -1 ? 0 : idCompraSeleccionada,
                "", nombre, cantidad, precio, precio * cantidad,
                "", "", "", idProducto, 0, 0, 1, 0, 0));
        actualizarTotales();
    }

    /**
     * Restaura el estado del formulario al volver del catálogo.
     */
    public void restaurarEstadoEn(CompraController destino) {
        destino.listaTemporal.setAll(this.listaTemporal);
        destino.tablaCompraProducto.setItems(destino.listaTemporal);
        destino.idCompraSeleccionada  = this.idCompraSeleccionada;
        destino.idPedidoCSeleccionado = this.idPedidoCSeleccionado;

        if (this.txtIdCompra    != null && destino.txtIdCompra    != null) destino.txtIdCompra.setText(this.txtIdCompra.getText());
        if (this.txtIdProveedor != null && destino.txtIdProveedor != null) destino.txtIdProveedor.setText(this.txtIdProveedor.getText());
        if (this.txtIdEmpleado  != null && destino.txtIdEmpleado  != null) destino.txtIdEmpleado.setText(this.txtIdEmpleado.getText());
        if (this.txtCondicion   != null && destino.txtCondicion   != null) destino.txtCondicion.setText(this.txtCondicion.getText());
        if (this.txtMontoPagado != null && destino.txtMontoPagado != null) destino.txtMontoPagado.setText(this.txtMontoPagado.getText());
        if (this.cmbTipoCompra  != null && destino.cmbTipoCompra  != null) destino.cmbTipoCompra.setValue(this.cmbTipoCompra.getValue());
        if (this.dpFechaCompra  != null && destino.dpFechaCompra  != null) destino.dpFechaCompra.setValue(this.dpFechaCompra.getValue());
        if (this.lblInfoCompra  != null && destino.lblInfoCompra  != null) {
            destino.lblInfoCompra.setText(this.lblInfoCompra.getText());
            destino.lblInfoCompra.setStyle(this.lblInfoCompra.getStyle());
        }
        destino.actualizarTotales();
    }

    private String construirInfoCompraActual() {
        if (idCompraSeleccionada != -1)
            return "Editando Compra #" + idCompraSeleccionada + " | " + listaTemporal.size() + " producto(s)";
        String proveedor = (txtIdProveedor != null && !txtIdProveedor.getText().isBlank())
                ? "Proveedor #" + txtIdProveedor.getText().trim() : "Nueva Compra";
        return proveedor + " | " + listaTemporal.size() + " producto(s) en lista";
    }

    // ── Cargar cuentas bancarias ──────────────────────────────────────────
    private void cargarCuentas() {
        mapaCuentas.clear();
        cmbCuenta.getItems().clear();
        String sql = "SELECT id_cuenta, nombre, banco FROM TBL_CUENTA ORDER BY banco, nombre";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String label = rs.getString("banco") + " - " + rs.getString("nombre");
                mapaCuentas.put(label, rs.getInt("id_cuenta"));
                cmbCuenta.getItems().add(label);
            }
        } catch (SQLException e) {
            cmbCuenta.getItems().add("Sin cuentas registradas");
        }
    }

    // ── Buscar compra existente por ID ────────────────────────────────────
    @FXML
    public void onBuscarCompra(ActionEvent event) {
        if (txtIdCompra.getText().isBlank()) return;
        String sql = "SELECT c.id_compra, c.tipo_compra, c.condicion, " +
                "CONVERT(VARCHAR(10), c.fecha_transaccion, 120) AS fecha, " +
                "c.monto_total, c.monto_pendiente, " +
                "pc.id_pedido_c, pc.id_proveedor, pc.id_empleado, " +
                "pr.nombre AS proveedor " +
                "FROM TBL_COMPRA c " +
                "JOIN TBL_PEDIDO_C pc  ON pc.id_pedido_c  = c.id_pedido_c " +
                "JOIN TBL_PROVEEDOR pr ON pr.id_proveedor = pc.id_proveedor " +
                "WHERE c.id_compra = ?";
        try {
            int idC = Integer.parseInt(txtIdCompra.getText().trim());
            try (Connection con = conexion.establecerConexion();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idC);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    lblInfoCompra.setText("Compra no encontrada.");
                    lblInfoCompra.setStyle("-fx-text-fill: #C62828; -fx-font-size: 11px;");
                    return;
                }
                idCompraSeleccionada  = rs.getInt("id_compra");
                idPedidoCSeleccionado = rs.getInt("id_pedido_c");
                txtIdProveedor.setText(String.valueOf(rs.getInt("id_proveedor")));
                txtIdEmpleado.setText(String.valueOf(rs.getInt("id_empleado")));
                cmbTipoCompra.setValue(rs.getString("tipo_compra"));
                txtCondicion.setText(rs.getString("condicion") != null ? rs.getString("condicion") : "");
                try { dpFechaCompra.setValue(java.time.LocalDate.parse(rs.getString("fecha"))); }
                catch (Exception ignored) {}

                double montoTotal     = rs.getDouble("monto_total");
                double montoPendiente = rs.getDouble("monto_pendiente");
                double yaPagado       = montoTotal - montoPendiente;
                if (yaPagado > 0) txtMontoPagado.setText(String.format("%.2f", yaPagado));

                lblInfoCompra.setText("Compra #" + idC + " - " + rs.getString("proveedor")
                        + " | Total: RD$ " + String.format("%.2f", montoTotal)
                        + " | Pendiente: RD$ " + String.format("%.2f", montoPendiente));
                lblInfoCompra.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 11px;");

                cargarProductosDeCompra(con, idC);
                cargarPagosDeCompra(con, idC);
                actualizarResumenPago(montoTotal, montoPendiente);
                if (txtIdCompraPago != null) txtIdCompraPago.setText(String.valueOf(idC));
            }
        } catch (NumberFormatException e) {
            lblInfoCompra.setText("ID invalido.");
            lblInfoCompra.setStyle("-fx-text-fill: #C62828; -fx-font-size: 11px;");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private void cargarProductosDeCompra(Connection con, int idCompra) throws SQLException {
        listaTemporal.clear();
        String sql = "SELECT cp.id_producto, cp.cantidad, cp.precio_unitario, cp.id_presentacion, pr.nombre " +
                "FROM TBL_COMPRA_PRODUCTO cp " +
                "JOIN TBL_PRODUCTO pr ON pr.id_producto = cp.id_producto " +
                "WHERE cp.id_compra = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idCompra);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int    idProd = rs.getInt("id_producto");
            String nombre = rs.getString("nombre");
            int    cant   = rs.getInt("cantidad");
            double precio = rs.getDouble("precio_unitario");
            int    idPres = rs.getInt("id_presentacion");
            listaTemporal.add(new Compra(idCompra, "", nombre, cant, precio, precio * cant,
                    "", "", "", idProd, 0, idPedidoCSeleccionado, idPres, 0, 0));
        }
        tablaCompraProducto.setItems(listaTemporal);
        actualizarTotales();
    }

    private void cargarPagosDeCompra(Connection con, int idCompra) throws SQLException {
        listaPagos.clear();
        String sql = "SELECT p.id_pago, p.tipo_pago, CONVERT(VARCHAR(10), p.fecha_pago, 120) AS fecha, " +
                "p.monto_pago, p.metodo_pago, p.estado_pago, cu.nombre AS cuenta, cu.banco " +
                "FROM TBL_PAGO p " +
                "JOIN TBL_PAGO_COMPRA pc ON pc.id_pago   = p.id_pago " +
                "JOIN TBL_CUENTA cu      ON cu.id_cuenta = pc.id_cuenta " +
                "WHERE pc.id_compra = ? ORDER BY p.fecha_pago DESC";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idCompra);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            listaPagos.add(new Pago(
                    rs.getInt("id_pago"), rs.getString("tipo_pago"), rs.getString("fecha"),
                    rs.getDouble("monto_pago"), rs.getString("metodo_pago"),
                    rs.getBoolean("estado_pago"), rs.getString("cuenta"), rs.getString("banco"),
                    0, idCompra));
        }
        tablaPagos.setItems(listaPagos);
    }

    // ── Buscar producto por ID → llena nombre y precio desde BD ──────────
    @FXML
    public void onBuscarProductoId(ActionEvent event) {
        if (txtIdProducto.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Ingresa el ID del producto."); return;
        }
        // Trae nombre Y precio de presentación (pre-llenado automático)
        String sql = "SELECT p.nombre, " +
                "ISNULL((SELECT TOP 1 precio_venta FROM TBL_PRESENTACION_PRODUCTO " +
                "        WHERE id_producto = p.id_producto ORDER BY id_presentacion ASC), 0) AS precio_venta " +
                "FROM TBL_PRODUCTO p WHERE p.id_producto = ?";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(txtIdProducto.getText().trim()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtNombreProducto.setText(rs.getString("nombre"));
                double precio = rs.getDouble("precio_venta");
                // Pre-llenar precio automáticamente
                if (txtPrecioProducto != null) {
                    txtPrecioProducto.setText(precio > 0 ? String.format("%.2f", precio) : "");
                    txtPrecioProducto.setEditable(false); // solo lectura
                    txtPrecioProducto.setStyle("-fx-background-color: #F1F8E9; -fx-background-radius: 6; -fx-border-color: #C8E6C9; -fx-border-radius: 6;");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Producto no encontrado.");
                txtNombreProducto.clear();
                if (txtPrecioProducto != null) txtPrecioProducto.clear();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    // ── Buscar en tabla ───────────────────────────────────────────────────
    @FXML
    public void fnBuscarProducto(ActionEvent event) {
        String busqueda = txtBuscarProducto.getText().trim().toLowerCase();
        if (busqueda.isEmpty()) { tablaCompraProducto.setItems(listaTemporal); return; }
        ObservableList<Compra> listaFiltrada = FXCollections.observableArrayList();
        for (Compra c : listaTemporal) {
            if (c.getProducto().toLowerCase().contains(busqueda)) listaFiltrada.add(c);
        }
        tablaCompraProducto.setItems(listaFiltrada);
    }

    // ── Agregar producto manualmente ──────────────────────────────────────
    @FXML
    public void onAgregarProducto(ActionEvent event) {
        if (txtIdProducto.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Ingresa el ID del producto."); return;
        }
        if (txtNombreProducto.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Busca el producto primero."); return;
        }
        if (txtCantidadProducto.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "La cantidad es obligatoria."); return;
        }
        if (txtPrecioProducto.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El precio es obligatorio."); return;
        }
        try {
            int    idProd = Integer.parseInt(txtIdProducto.getText().trim());
            String nombre = txtNombreProducto.getText().trim();
            int    cant   = Integer.parseInt(txtCantidadProducto.getText().trim());
            double precio = Double.parseDouble(txtPrecioProducto.getText().trim());

            listaTemporal.add(new Compra(
                    idCompraSeleccionada == -1 ? 0 : idCompraSeleccionada,
                    "", nombre, cant, precio, precio * cant,
                    "", "", "", idProd, 0, 0, 1, 0, 0));
            tablaCompraProducto.setItems(listaTemporal);
            actualizarTotales();

            txtIdProducto.clear();
            txtNombreProducto.clear();
            txtCantidadProducto.clear();
            if (txtPrecioProducto != null) {
                txtPrecioProducto.clear();
                txtPrecioProducto.setEditable(true);
                txtPrecioProducto.setStyle("-fx-background-radius: 6; -fx-border-color: #E0E0E0; -fx-border-radius: 6;");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Cantidad y precio deben ser numeros.");
        }
    }

    // ── Quitar producto ───────────────────────────────────────────────────
    @FXML
    public void onQuitarProducto(ActionEvent event) {
        Compra sel = tablaCompraProducto.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona un producto."); return; }
        listaTemporal.remove(sel);
        tablaCompraProducto.setItems(listaTemporal);
        actualizarTotales();
    }

    // ── Registrar nueva compra ────────────────────────────────────────────
    @FXML
    public void onRegistrarCompra(ActionEvent event) {
        if (txtIdProveedor.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El ID de Proveedor es obligatorio."); return;
        }
        if (cmbTipoCompra.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona el tipo de compra."); return;
        }
        if (listaTemporal.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Agrega al menos un producto."); return;
        }

        int    idProveedor = Integer.parseInt(txtIdProveedor.getText().trim());
        int    idEmpleado  = txtIdEmpleado.getText().isBlank() ? 1 : Integer.parseInt(txtIdEmpleado.getText().trim());
        double montoTotal  = calcularTotal();
        double pagado      = parseMontoPagado();
        double pendiente   = Math.max(0, montoTotal - pagado);

        try (Connection con = conexion.establecerConexion()) {
            PreparedStatement psPedido = con.prepareStatement(
                    "INSERT INTO TBL_PEDIDO_C (id_empleado, id_proveedor, fecha_pedido) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            psPedido.setInt(1, idEmpleado);
            psPedido.setInt(2, idProveedor);
            psPedido.setDate(3, Date.valueOf(dpFechaCompra.getValue()));
            psPedido.executeUpdate();
            int idPedidoC = -1;
            ResultSet kp = psPedido.getGeneratedKeys();
            if (kp.next()) idPedidoC = kp.getInt(1);

            for (Compra c : listaTemporal) {
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO TBL_DETALLE_PEDIDO_C (id_pedido_c, id_producto, cantidad) VALUES (?,?,?)");
                ps.setInt(1, idPedidoC); ps.setInt(2, c.getIdProducto()); ps.setInt(3, c.getCantidad());
                ps.executeUpdate();
            }

            PreparedStatement psCompra = con.prepareStatement(
                    "INSERT INTO TBL_COMPRA (tipo_compra, fecha_transaccion, monto_total, monto_pendiente, condicion, id_pedido_c) VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            psCompra.setString(1, cmbTipoCompra.getValue());
            psCompra.setDate(2,   Date.valueOf(dpFechaCompra.getValue()));
            psCompra.setDouble(3, montoTotal);
            psCompra.setDouble(4, pendiente);
            psCompra.setString(5, txtCondicion.getText().trim());
            psCompra.setInt(6,    idPedidoC);
            psCompra.executeUpdate();
            int idCompra = -1;
            ResultSet kc = psCompra.getGeneratedKeys();
            if (kc.next()) idCompra = kc.getInt(1);

            for (Compra c : listaTemporal) {
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO TBL_COMPRA_PRODUCTO (id_compra, id_producto, cantidad, precio_unitario, id_presentacion, fecha_compra) VALUES (?,?,?,?,?,?)");
                ps.setInt(1, idCompra); ps.setInt(2, c.getIdProducto()); ps.setInt(3, c.getCantidad());
                ps.setDouble(4, c.getPrecioUnitario());
                ps.setInt(5, c.getIdPresentacion() > 0 ? c.getIdPresentacion() : 1);
                ps.setDate(6, Date.valueOf(dpFechaCompra.getValue()));
                ps.executeUpdate();

                PreparedStatement psStock = con.prepareStatement(
                        "UPDATE TBL_PRODUCTO SET cantidad_disponible = cantidad_disponible + ? WHERE id_producto=?");
                psStock.setInt(1, c.getCantidad()); psStock.setInt(2, c.getIdProducto());
                psStock.executeUpdate();
            }

            if (pagado > 0) registrarPagoInterno(con, idCompra, pagado);

            JOptionPane.showMessageDialog(null, "Compra #" + idCompra + " registrada correctamente.");
            Limpiar();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar compra: " + e.getMessage());
        }
    }

    // ── Editar compra existente ───────────────────────────────────────────
    @FXML
    public void onEditarCompra(ActionEvent event) {
        if (idCompraSeleccionada == -1) {
            JOptionPane.showMessageDialog(null, "Primero busca una compra por ID."); return;
        }
        if (txtIdProveedor.getText().isBlank() || cmbTipoCompra.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Proveedor y tipo de compra son obligatorios."); return;
        }

        double montoTotal = calcularTotal();
        double pagado     = parseMontoPagado();
        double pendiente  = Math.max(0, montoTotal - pagado);

        try (Connection con = conexion.establecerConexion()) {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE TBL_COMPRA SET tipo_compra=?, fecha_transaccion=?, monto_total=?, monto_pendiente=?, condicion=? WHERE id_compra=?");
            ps.setString(1, cmbTipoCompra.getValue());
            ps.setDate(2,   Date.valueOf(dpFechaCompra.getValue()));
            ps.setDouble(3, montoTotal); ps.setDouble(4, pendiente);
            ps.setString(5, txtCondicion.getText().trim()); ps.setInt(6, idCompraSeleccionada);
            ps.executeUpdate();

            PreparedStatement psOld = con.prepareStatement(
                    "SELECT id_producto, cantidad FROM TBL_COMPRA_PRODUCTO WHERE id_compra=?");
            psOld.setInt(1, idCompraSeleccionada);
            ResultSet rsOld = psOld.executeQuery();
            while (rsOld.next()) {
                PreparedStatement psRev = con.prepareStatement(
                        "UPDATE TBL_PRODUCTO SET cantidad_disponible = cantidad_disponible - ? WHERE id_producto=?");
                psRev.setInt(1, rsOld.getInt("cantidad")); psRev.setInt(2, rsOld.getInt("id_producto"));
                psRev.executeUpdate();
            }

            PreparedStatement psDel = con.prepareStatement("DELETE FROM TBL_COMPRA_PRODUCTO WHERE id_compra=?");
            psDel.setInt(1, idCompraSeleccionada); psDel.executeUpdate();

            for (Compra c : listaTemporal) {
                PreparedStatement psCP = con.prepareStatement(
                        "INSERT INTO TBL_COMPRA_PRODUCTO (id_compra, id_producto, cantidad, precio_unitario, id_presentacion, fecha_compra) VALUES (?,?,?,?,?,?)");
                psCP.setInt(1, idCompraSeleccionada); psCP.setInt(2, c.getIdProducto()); psCP.setInt(3, c.getCantidad());
                psCP.setDouble(4, c.getPrecioUnitario());
                psCP.setInt(5, c.getIdPresentacion() > 0 ? c.getIdPresentacion() : 1);
                psCP.setDate(6, Date.valueOf(dpFechaCompra.getValue())); psCP.executeUpdate();

                PreparedStatement psStock = con.prepareStatement(
                        "UPDATE TBL_PRODUCTO SET cantidad_disponible = cantidad_disponible + ? WHERE id_producto=?");
                psStock.setInt(1, c.getCantidad()); psStock.setInt(2, c.getIdProducto()); psStock.executeUpdate();
            }

            JOptionPane.showMessageDialog(null, "Compra #" + idCompraSeleccionada + " actualizada.");
            Limpiar();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al editar: " + e.getMessage());
        }
    }

    // ── Registrar pago ────────────────────────────────────────────────────
    @FXML
    public void onRegistrarPago(ActionEvent event) {
        int idCompraTarget = idCompraSeleccionada;
        if (txtIdCompraPago != null && !txtIdCompraPago.getText().isBlank()) {
            try { idCompraTarget = Integer.parseInt(txtIdCompraPago.getText().trim()); }
            catch (NumberFormatException ignored) {}
        }

        if (idCompraTarget == -1) {
            JOptionPane.showMessageDialog(null, "Primero busca una compra por ID.");
            return;
        }

        if (txtMontoPago.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Ingresa el monto del pago.");
            return;
        }

        if (cmbMetodoPago.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona el metodo de pago.");
            return;
        }

        if (cmbCuenta.getValue() == null || !mapaCuentas.containsKey(cmbCuenta.getValue())) {
            JOptionPane.showMessageDialog(null, "Selecciona una cuenta bancaria valida.");
            return;
        }

        double montoPago;
        try {
            montoPago = Double.parseDouble(txtMontoPago.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "El monto debe ser un numero valido.");
            return;
        }

        if (montoPago <= 0) {
            JOptionPane.showMessageDialog(null, "El monto debe ser mayor a cero.");
            return;
        }

        int idCuenta = mapaCuentas.get(cmbCuenta.getValue());

        try (Connection con = conexion.establecerConexion()) {

            PreparedStatement psCheck = con.prepareStatement(
                    "SELECT monto_total, monto_pendiente FROM TBL_COMPRA WHERE id_compra = ?");
            psCheck.setInt(1, idCompraTarget);
            ResultSet rsCheck = psCheck.executeQuery();

            if (!rsCheck.next()) {
                JOptionPane.showMessageDialog(null, "Compra #" + idCompraTarget + " no encontrada.");
                return;
            }

            double montoTotal = rsCheck.getDouble("monto_total");
            double montoPendiente = rsCheck.getDouble("monto_pendiente");

            if (montoPago > montoPendiente) {
                JOptionPane.showMessageDialog(null,
                        "El monto excede el pendiente. Pendiente actual: RD$ " + String.format("%.2f", montoPendiente),
                        "Error de pago",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            PreparedStatement psPago = con.prepareStatement(
                    "INSERT INTO TBL_PAGO (tipo_pago, fecha_pago, monto_pago, metodo_pago, estado_pago) VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);

            psPago.setString(1, "Compra");
            psPago.setDate(2, Date.valueOf(dpFechaPago.getValue()));
            psPago.setDouble(3, montoPago);
            psPago.setString(4, cmbMetodoPago.getValue());
            psPago.setBoolean(5, true);
            psPago.executeUpdate();

            int idPago = -1;
            ResultSet keys = psPago.getGeneratedKeys();
            if (keys.next()) idPago = keys.getInt(1);

            PreparedStatement psPagoC = con.prepareStatement(
                    "INSERT INTO TBL_PAGO_COMPRA (id_compra, id_cuenta, id_pago) VALUES (?,?,?)");

            psPagoC.setInt(1, idCompraTarget);
            psPagoC.setInt(2, idCuenta);
            psPagoC.setInt(3, idPago);
            psPagoC.executeUpdate();

            double nuevoPendiente = montoPendiente - montoPago;

            PreparedStatement psUpd = con.prepareStatement(
                    "UPDATE TBL_COMPRA SET monto_pendiente = ? WHERE id_compra = ?");
            psUpd.setDouble(1, nuevoPendiente);
            psUpd.setInt(2, idCompraTarget);
            psUpd.executeUpdate();

            JOptionPane.showMessageDialog(null,
                    "Pago #" + idPago + " registrado.\n" +
                            "Monto: RD$ " + String.format("%.2f", montoPago) +
                            " | Nuevo pendiente: RD$ " + String.format("%.2f", nuevoPendiente));

            cargarPagosDeCompra(con, idCompraTarget);
            actualizarResumenPago(montoTotal, nuevoPendiente);
            limpiarFormularioPago();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar pago: " + e.getMessage());
        }
    }
    // ── Eliminar pago ─────────────────────────────────────────────────────
    @FXML
    public void onEliminarPago(ActionEvent event) {
        Pago sel = tablaPagos.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona un pago de la lista."); return; }
        int resp = JOptionPane.showConfirmDialog(null,
                "Eliminar pago #" + sel.getIdPago() + " de RD$ " + String.format("%.2f", sel.getMonto()) + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (resp != JOptionPane.YES_OPTION) return;
        try (Connection con = conexion.establecerConexion()) {
            PreparedStatement psGet = con.prepareStatement("SELECT monto_pago FROM TBL_PAGO WHERE id_pago=?");
            psGet.setInt(1, sel.getIdPago());
            ResultSet rs = psGet.executeQuery();
            double monto = rs.next() ? rs.getDouble("monto_pago") : 0;

            PreparedStatement psDel1 = con.prepareStatement("DELETE FROM TBL_PAGO_COMPRA WHERE id_pago=?");
            psDel1.setInt(1, sel.getIdPago()); psDel1.executeUpdate();
            PreparedStatement psDel2 = con.prepareStatement("DELETE FROM TBL_PAGO WHERE id_pago=?");
            psDel2.setInt(1, sel.getIdPago()); psDel2.executeUpdate();

            if (idCompraSeleccionada != -1 && monto > 0) {
                PreparedStatement psUpd = con.prepareStatement(
                        "UPDATE TBL_COMPRA SET monto_pendiente = monto_pendiente + ? WHERE id_compra=?");
                psUpd.setDouble(1, monto); psUpd.setInt(2, idCompraSeleccionada); psUpd.executeUpdate();
                PreparedStatement psRef = con.prepareStatement("SELECT monto_total, monto_pendiente FROM TBL_COMPRA WHERE id_compra=?");
                psRef.setInt(1, idCompraSeleccionada);
                ResultSet rsRef = psRef.executeQuery();
                if (rsRef.next()) actualizarResumenPago(rsRef.getDouble("monto_total"), rsRef.getDouble("monto_pendiente"));
            }
            cargarPagosDeCompra(con, idCompraSeleccionada != -1 ? idCompraSeleccionada : sel.getIdCompra());
            JOptionPane.showMessageDialog(null, "Pago eliminado correctamente.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    // ── Limpiar formulario ────────────────────────────────────────────────
    @FXML
    public void Limpiar() {
        txtIdCompra.clear(); txtIdProveedor.clear(); txtIdEmpleado.clear();
        txtCondicion.clear(); txtMontoPagado.clear();
        if (txtIdProducto       != null) txtIdProducto.clear();
        if (txtNombreProducto   != null) txtNombreProducto.clear();
        if (txtCantidadProducto != null) txtCantidadProducto.clear();
        if (txtPrecioProducto   != null) {
            txtPrecioProducto.clear();
            txtPrecioProducto.setEditable(true);
            txtPrecioProducto.setStyle("-fx-background-radius: 6; -fx-border-color: #E0E0E0; -fx-border-radius: 6;");
        }
        if (txtBuscarProducto != null) txtBuscarProducto.clear();
        if (lblInfoCompra     != null) lblInfoCompra.setText("");
        if (lblResumenPago    != null) lblResumenPago.setText("");
        cmbTipoCompra.setValue(null);
        dpFechaCompra.setValue(java.time.LocalDate.now());
        listaTemporal.clear(); listaPagos.clear();
        tablaCompraProducto.setItems(listaTemporal);
        tablaPagos.setItems(listaPagos);
        idCompraSeleccionada  = -1; idPedidoCSeleccionado = -1;
        lblMontoTotal.setText("RD$ 0.00"); lblMontoPendiente.setText("RD$ 0.00");
        lblCantProductos.setText("0 productos");
        limpiarFormularioPago();
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private void registrarPagoInterno(Connection con, int idCompra, double montoPagado) throws SQLException {
        String metodo = cmbMetodoPago.getValue() != null ? cmbMetodoPago.getValue() : "Efectivo";
        int    cuenta = (cmbCuenta.getValue() != null && mapaCuentas.containsKey(cmbCuenta.getValue()))
                ? mapaCuentas.get(cmbCuenta.getValue()) : 1;
        PreparedStatement psPago = con.prepareStatement(
                "INSERT INTO TBL_PAGO (tipo_pago, fecha_pago, monto_pago, metodo_pago, estado_pago) VALUES (?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
        psPago.setString(1, "Compra"); psPago.setDate(2, Date.valueOf(dpFechaCompra.getValue()));
        psPago.setDouble(3, montoPagado); psPago.setString(4, metodo); psPago.setBoolean(5, true);
        psPago.executeUpdate();
        int idPago = -1;
        ResultSet keys = psPago.getGeneratedKeys();
        if (keys.next()) idPago = keys.getInt(1);
        PreparedStatement psPagoC = con.prepareStatement(
                "INSERT INTO TBL_PAGO_COMPRA (id_compra, id_cuenta, id_pago) VALUES (?,?,?)");
        psPagoC.setInt(1, idCompra); psPagoC.setInt(2, cuenta); psPagoC.setInt(3, idPago);
        psPagoC.executeUpdate();
    }

    private void limpiarFormularioPago() {
        if (txtMontoPago    != null) txtMontoPago.clear();
        if (txtIdCompraPago != null) txtIdCompraPago.setText(
                idCompraSeleccionada != -1 ? String.valueOf(idCompraSeleccionada) : "");
        cmbMetodoPago.setValue(null); cmbCuenta.setValue(null);
        dpFechaPago.setValue(java.time.LocalDate.now());
    }

    private double parseMontoPagado() {
        if (txtMontoPagado == null || txtMontoPagado.getText().isBlank()) return 0;
        try { return Double.parseDouble(txtMontoPagado.getText().trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    private double calcularTotal() {
        double total = 0;
        for (Compra c : listaTemporal) total += c.getSubtotal();
        return total;
    }

    private void actualizarTotales() {
        double total     = calcularTotal();
        double pagado    = parseMontoPagado();
        double pendiente = Math.max(0, total - pagado);
        lblMontoTotal.setText("RD$ " + String.format("%.2f", total));
        lblMontoPendiente.setText("RD$ " + String.format("%.2f", pendiente));
        lblCantProductos.setText(listaTemporal.size() + " productos");
    }

    private void actualizarResumenPago(double total, double pendiente) {
        if (lblResumenPago == null) return;
        double pagado = total - pendiente;
        lblResumenPago.setText("Total: RD$ " + String.format("%.2f", total)
                + "   Pagado: RD$ " + String.format("%.2f", pagado)
                + "   Pendiente: RD$ " + String.format("%.2f", pendiente));
        lblResumenPago.setStyle(pendiente <= 0
                ? "-fx-text-fill: #2E7D32; -fx-font-size: 11px;"
                : "-fx-text-fill: #C62828; -fx-font-size: 11px;");
    }
}