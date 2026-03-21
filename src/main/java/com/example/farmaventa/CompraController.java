package com.example.farmaventa;

import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.Compra;
import com.example.farmaventa.modelo.Pago;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javax.swing.JOptionPane;
import java.sql.*;

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

    // ── Estado interno ────────────────────────────────────────────────────
    private final ObservableList<Compra> listaTemporal  = FXCollections.observableArrayList();
    private final ObservableList<Pago>   listaPagos     = FXCollections.observableArrayList();
    private FilteredList<Compra>         listaFiltrada;
    private int idCompraSeleccionada  = -1;
    private int idPedidoCSeleccionado = -1;
    private final java.util.LinkedHashMap<String, Integer> mapaCuentas = new java.util.LinkedHashMap<>();

    // ══════════════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        cmbTipoCompra.getItems().addAll("Contado", "Crédito", "Consignación");
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

        txtMontoPagado.textProperty().addListener((obs, o, n) -> actualizarTotales());

        listaFiltrada = new FilteredList<>(listaTemporal, p -> true);
        tablaCompraProducto.setItems(listaFiltrada);

        if (txtBuscarProducto != null) {
            txtBuscarProducto.textProperty().addListener((obs, o, n) ->
                    listaFiltrada.setPredicate(c -> {
                        if (n == null || n.isBlank()) return true;
                        return c.getProducto().toLowerCase().contains(n.toLowerCase());
                    })
            );
        }

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

    private void cargarCuentas() {
        mapaCuentas.clear();
        cmbCuenta.getItems().clear();
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id_cuenta, nombre, banco FROM TBL_CUENTA ORDER BY banco, nombre")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String label = rs.getString("banco") + " — " + rs.getString("nombre");
                mapaCuentas.put(label, rs.getInt("id_cuenta"));
                cmbCuenta.getItems().add(label);
            }
        } catch (SQLException e) {
            cmbCuenta.getItems().add("Sin cuentas registradas");
        }
    }

    @FXML
    public void onBuscarCompra(ActionEvent event) {
        if (txtIdCompra.getText().isBlank()) return;
        try {
            int idC = Integer.parseInt(txtIdCompra.getText().trim());
            String sql = """
                    SELECT c.id_compra, c.tipo_compra, c.condicion,
                           CONVERT(VARCHAR(10), c.fecha_transaccion, 120) AS fecha,
                           c.monto_total, c.monto_pendiente,
                           pc.id_pedido_c, pc.id_proveedor, pc.id_empleado,
                           pr.nombre AS proveedor
                    FROM TBL_COMPRA c
                    JOIN TBL_PEDIDO_C pc  ON pc.id_pedido_c  = c.id_pedido_c
                    JOIN TBL_PROVEEDOR pr ON pr.id_proveedor = pc.id_proveedor
                    WHERE c.id_compra = ?
                    """;
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
                double yaPagedo       = montoTotal - montoPendiente;
                if (yaPagedo > 0) txtMontoPagado.setText(String.format("%.2f", yaPagedo));

                lblInfoCompra.setText("Compra #" + idC + " — " + rs.getString("proveedor")
                        + " | Total: RD$ " + String.format("%.2f", montoTotal)
                        + " | Pendiente: RD$ " + String.format("%.2f", montoPendiente));
                lblInfoCompra.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 11px;");

                cargarProductosDeCompra(con, idC);
                cargarPagosDeCompra(con, idC);
                actualizarResumenPago(montoTotal, montoPendiente);
                if (txtIdCompraPago != null) txtIdCompraPago.setText(String.valueOf(idC));
            }
        } catch (NumberFormatException e) {
            lblInfoCompra.setText("ID inválido.");
            lblInfoCompra.setStyle("-fx-text-fill: #C62828; -fx-font-size: 11px;");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private void cargarProductosDeCompra(Connection con, int idCompra) throws SQLException {
        listaTemporal.clear();
        PreparedStatement ps = con.prepareStatement("""
                SELECT cp.id_producto, cp.cantidad, cp.precio_unitario,
                       cp.id_presentacion, pr.nombre
                FROM TBL_COMPRA_PRODUCTO cp
                JOIN TBL_PRODUCTO pr ON pr.id_producto = cp.id_producto
                WHERE cp.id_compra = ?""");
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
        actualizarTotales();
    }

    private void cargarPagosDeCompra(Connection con, int idCompra) throws SQLException {
        listaPagos.clear();
        PreparedStatement ps = con.prepareStatement("""
                SELECT p.id_pago, p.tipo_pago,
                       CONVERT(VARCHAR(10), p.fecha_pago, 120) AS fecha,
                       p.monto_pago, p.metodo_pago, p.estado_pago,
                       cu.nombre AS cuenta, cu.banco
                FROM TBL_PAGO p
                JOIN TBL_PAGO_COMPRA pc ON pc.id_pago   = p.id_pago
                JOIN TBL_CUENTA cu      ON cu.id_cuenta = pc.id_cuenta
                WHERE pc.id_compra = ?
                ORDER BY p.fecha_pago DESC""");
        ps.setInt(1, idCompra);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            listaPagos.add(new Pago(
                    rs.getInt("id_pago"), rs.getString("tipo_pago"), rs.getString("fecha"),
                    rs.getDouble("monto_pago"), rs.getString("metodo_pago"),
                    rs.getBoolean("estado_pago"), rs.getString("cuenta"), rs.getString("banco"),
                    0, idCompra));
        }
    }

    private void actualizarResumenPago(double total, double pendiente) {
        if (lblResumenPago == null) return;
        double pagado = total - pendiente;
        lblResumenPago.setText("Total: RD$ " + String.format("%.2f", total)
                + "   •   Pagado: RD$ " + String.format("%.2f", pagado)
                + "   •   Pendiente: RD$ " + String.format("%.2f", pendiente));
        lblResumenPago.setStyle(pendiente <= 0
                ? "-fx-text-fill: #2E7D32; -fx-font-size: 11px;"
                : "-fx-text-fill: #C62828; -fx-font-size: 11px;");
    }

    @FXML
    public void onRegistrarPago(ActionEvent event) {
        int idCompraTarget = idCompraSeleccionada;
        if (txtIdCompraPago != null && !txtIdCompraPago.getText().isBlank()) {
            try { idCompraTarget = Integer.parseInt(txtIdCompraPago.getText().trim()); }
            catch (NumberFormatException ignored) {}
        }
        if (idCompraTarget == -1) {
            JOptionPane.showMessageDialog(null, "Primero busca una compra por ID."); return;
        }
        if (txtMontoPago.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Ingresa el monto del pago."); return;
        }
        if (cmbMetodoPago.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona el método de pago."); return;
        }
        if (cmbCuenta.getValue() == null || !mapaCuentas.containsKey(cmbCuenta.getValue())) {
            JOptionPane.showMessageDialog(null, "Selecciona una cuenta bancaria válida."); return;
        }

        double montoPago;
        try { montoPago = Double.parseDouble(txtMontoPago.getText().trim()); }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "El monto debe ser un número válido."); return;
        }
        if (montoPago <= 0) {
            JOptionPane.showMessageDialog(null, "El monto debe ser mayor a cero."); return;
        }

        int idCuenta = mapaCuentas.get(cmbCuenta.getValue());

        try (Connection con = conexion.establecerConexion()) {
            PreparedStatement psCheck = con.prepareStatement(
                    "SELECT monto_total, monto_pendiente FROM TBL_COMPRA WHERE id_compra = ?");
            psCheck.setInt(1, idCompraTarget);
            ResultSet rsCheck = psCheck.executeQuery();
            if (!rsCheck.next()) {
                JOptionPane.showMessageDialog(null, "Compra #" + idCompraTarget + " no encontrada."); return;
            }
            double montoTotal     = rsCheck.getDouble("monto_total");
            double montoPendiente = rsCheck.getDouble("monto_pendiente");

            if (montoPago > montoPendiente) {
                int resp = JOptionPane.showConfirmDialog(null,
                        "El monto (RD$ " + String.format("%.2f", montoPago)
                                + ") supera el pendiente (RD$ " + String.format("%.2f", montoPendiente)
                                + ").\n¿Continuar de todas formas?",
                        "Monto excede pendiente", JOptionPane.YES_NO_OPTION);
                if (resp != JOptionPane.YES_OPTION) return;
            }

            // Insertar TBL_PAGO
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

            // Insertar TBL_PAGO_COMPRA
            PreparedStatement psPagoC = con.prepareStatement(
                    "INSERT INTO TBL_PAGO_COMPRA (id_compra, id_cuenta, id_pago) VALUES (?,?,?)");
            psPagoC.setInt(1, idCompraTarget); psPagoC.setInt(2, idCuenta); psPagoC.setInt(3, idPago);
            psPagoC.executeUpdate();

            // Actualizar monto_pendiente
            double nuevoPendiente = Math.max(0, montoPendiente - montoPago);
            PreparedStatement psUpd = con.prepareStatement(
                    "UPDATE TBL_COMPRA SET monto_pendiente = ? WHERE id_compra = ?");
            psUpd.setDouble(1, nuevoPendiente); psUpd.setInt(2, idCompraTarget);
            psUpd.executeUpdate();

            JOptionPane.showMessageDialog(null,
                    "✔ Pago #" + idPago + " registrado.\n"
                            + "Monto: RD$ " + String.format("%.2f", montoPago)
                            + "  |  Nuevo pendiente: RD$ " + String.format("%.2f", nuevoPendiente));

            cargarPagosDeCompra(con, idCompraTarget);
            actualizarResumenPago(montoTotal, nuevoPendiente);
            limpiarFormularioPago();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar pago: " + e.getMessage());
        }
    }

    @FXML
    public void onEliminarPago(ActionEvent event) {
        Pago sel = tablaPagos.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona un pago de la lista."); return; }
        int resp = JOptionPane.showConfirmDialog(null,
                "¿Eliminar pago #" + sel.getIdPago() + " de RD$ " + String.format("%.2f", sel.getMonto()) + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (resp != JOptionPane.YES_OPTION) return;

        try (Connection con = conexion.establecerConexion()) {
            PreparedStatement psGet = con.prepareStatement("SELECT monto_pago FROM TBL_PAGO WHERE id_pago=?");
            psGet.setInt(1, sel.getIdPago());
            ResultSet rs = psGet.executeQuery();
            double monto = rs.next() ? rs.getDouble("monto_pago") : 0;

            con.prepareStatement("DELETE FROM TBL_PAGO_COMPRA WHERE id_pago=" + sel.getIdPago()).executeUpdate();
            con.prepareStatement("DELETE FROM TBL_PAGO WHERE id_pago=" + sel.getIdPago()).executeUpdate();

            if (idCompraSeleccionada != -1 && monto > 0) {
                PreparedStatement psUpd = con.prepareStatement(
                        "UPDATE TBL_COMPRA SET monto_pendiente = monto_pendiente + ? WHERE id_compra=?");
                psUpd.setDouble(1, monto); psUpd.setInt(2, idCompraSeleccionada);
                psUpd.executeUpdate();

                PreparedStatement psRef = con.prepareStatement(
                        "SELECT monto_total, monto_pendiente FROM TBL_COMPRA WHERE id_compra=?");
                psRef.setInt(1, idCompraSeleccionada);
                ResultSet rsRef = psRef.executeQuery();
                if (rsRef.next()) actualizarResumenPago(rsRef.getDouble("monto_total"), rsRef.getDouble("monto_pendiente"));
            }
            cargarPagosDeCompra(con, idCompraSeleccionada != -1 ? idCompraSeleccionada : sel.getIdCompra());
            JOptionPane.showMessageDialog(null, "✔ Pago eliminado correctamente.");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    @FXML
    public void onAgregarProducto(ActionEvent event) {
        String nombre = JOptionPane.showInputDialog(null, "Nombre del producto a buscar:");
        if (nombre == null || nombre.isBlank()) return;
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id_producto, nombre FROM TBL_PRODUCTO WHERE nombre LIKE ?")) {
            ps.setString(1, "%" + nombre.trim() + "%");
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) { JOptionPane.showMessageDialog(null, "Producto no encontrado."); return; }
            int    idProd = rs.getInt("id_producto");
            String nomProd = rs.getString("nombre");
            String cantStr   = JOptionPane.showInputDialog(null, "Cantidad de \"" + nomProd + "\":");
            if (cantStr == null || cantStr.isBlank()) return;
            String precioStr = JOptionPane.showInputDialog(null, "Precio unitario (RD$):");
            if (precioStr == null || precioStr.isBlank()) return;
            String presStr   = JOptionPane.showInputDialog(null, "ID Presentación (1 si no aplica):");
            int idPres = 1;
            if (presStr != null && !presStr.isBlank()) {
                try { idPres = Integer.parseInt(presStr.trim()); } catch (NumberFormatException ignored) {}
            }
            int cant = Integer.parseInt(cantStr.trim());
            double precio = Double.parseDouble(precioStr.trim());
            listaTemporal.add(new Compra(idCompraSeleccionada == -1 ? 0 : idCompraSeleccionada,
                    "", nomProd, cant, precio, precio * cant, "", "", "",
                    idProd, 0, 0, idPres, 0, 0));
            actualizarTotales();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Cantidad y precio deben ser números.");
        }
    }

    @FXML
    public void onQuitarProducto(ActionEvent event) {
        Compra sel = tablaCompraProducto.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona un producto."); return; }
        listaTemporal.remove(sel);
        actualizarTotales();
    }

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
        double montoTotal  = listaTemporal.stream().mapToDouble(Compra::getSubtotal).sum();
        double pagado      = parseMontoPagado();
        double pendiente   = Math.max(0, montoTotal - pagado);

        try (Connection con = conexion.establecerConexion()) {
            PreparedStatement psPedido = con.prepareStatement(
                    "INSERT INTO TBL_PEDIDO_C (id_empleado, id_proveedor, fecha_pedido) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            psPedido.setInt(1, idEmpleado); psPedido.setInt(2, idProveedor);
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
            psCompra.setDate(2, Date.valueOf(dpFechaCompra.getValue()));
            psCompra.setDouble(3, montoTotal); psCompra.setDouble(4, pendiente);
            psCompra.setString(5, txtCondicion.getText().trim()); psCompra.setInt(6, idPedidoC);
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

            JOptionPane.showMessageDialog(null, "✔ Compra #" + idCompra + " registrada correctamente.");
            Limpiar();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar compra: " + e.getMessage());
        }
    }

    @FXML
    public void onEditarCompra(ActionEvent event) {
        if (idCompraSeleccionada == -1) {
            JOptionPane.showMessageDialog(null, "Primero busca una compra por ID."); return;
        }
        if (txtIdProveedor.getText().isBlank() || cmbTipoCompra.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Proveedor y tipo de compra son obligatorios."); return;
        }
        double montoTotal = listaTemporal.stream().mapToDouble(Compra::getSubtotal).sum();
        double pagado     = parseMontoPagado();
        double pendiente  = Math.max(0, montoTotal - pagado);

        try (Connection con = conexion.establecerConexion()) {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE TBL_COMPRA SET tipo_compra=?, fecha_transaccion=?, monto_total=?, monto_pendiente=?, condicion=? WHERE id_compra=?");
            ps.setString(1, cmbTipoCompra.getValue());
            ps.setDate(2, Date.valueOf(dpFechaCompra.getValue()));
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
            con.prepareStatement("DELETE FROM TBL_COMPRA_PRODUCTO WHERE id_compra=" + idCompraSeleccionada).executeUpdate();

            for (Compra c : listaTemporal) {
                PreparedStatement psCP = con.prepareStatement(
                        "INSERT INTO TBL_COMPRA_PRODUCTO (id_compra, id_producto, cantidad, precio_unitario, id_presentacion, fecha_compra) VALUES (?,?,?,?,?,?)");
                psCP.setInt(1, idCompraSeleccionada); psCP.setInt(2, c.getIdProducto()); psCP.setInt(3, c.getCantidad());
                psCP.setDouble(4, c.getPrecioUnitario());
                psCP.setInt(5, c.getIdPresentacion() > 0 ? c.getIdPresentacion() : 1);
                psCP.setDate(6, Date.valueOf(dpFechaCompra.getValue()));
                psCP.executeUpdate();

                PreparedStatement psStock = con.prepareStatement(
                        "UPDATE TBL_PRODUCTO SET cantidad_disponible = cantidad_disponible + ? WHERE id_producto=?");
                psStock.setInt(1, c.getCantidad()); psStock.setInt(2, c.getIdProducto());
                psStock.executeUpdate();
            }
            JOptionPane.showMessageDialog(null, "✔ Compra #" + idCompraSeleccionada + " actualizada.");
            Limpiar();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al editar: " + e.getMessage());
        }
    }

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
        cmbMetodoPago.setValue(null);
        cmbCuenta.setValue(null);
        dpFechaPago.setValue(java.time.LocalDate.now());
    }

    private double parseMontoPagado() {
        if (txtMontoPagado == null || txtMontoPagado.getText().isBlank()) return 0;
        try { return Double.parseDouble(txtMontoPagado.getText().trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    @FXML
    public void Limpiar() {
        txtIdCompra.clear(); txtIdProveedor.clear(); txtIdEmpleado.clear(); txtCondicion.clear();
        if (txtBuscarProducto != null) txtBuscarProducto.clear();
        if (txtMontoPagado    != null) txtMontoPagado.clear();
        if (lblInfoCompra     != null) lblInfoCompra.setText("");
        if (lblResumenPago    != null) lblResumenPago.setText("");
        cmbTipoCompra.setValue(null);
        dpFechaCompra.setValue(java.time.LocalDate.now());
        listaTemporal.clear(); listaPagos.clear();
        idCompraSeleccionada = -1; idPedidoCSeleccionado = -1;
        lblMontoTotal.setText("RD$ 0.00"); lblMontoPendiente.setText("RD$ 0.00");
        lblCantProductos.setText("0 productos");
        limpiarFormularioPago();
    }

    private void actualizarTotales() {
        double total     = listaTemporal.stream().mapToDouble(Compra::getSubtotal).sum();
        double pagado    = parseMontoPagado();
        double pendiente = Math.max(0, total - pagado);
        lblMontoTotal.setText("RD$ " + String.format("%.2f", total));
        lblMontoPendiente.setText("RD$ " + String.format("%.2f", pendiente));
        lblCantProductos.setText(listaTemporal.size() + " productos");
    }
}