package com.example.farmaventa;

import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.Compra;
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

    // ── Formulario ────────────────────────────────────────────────────────
    @FXML private TextField        txtIdCompra;       // buscar compra existente
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

    // ── Tabla ─────────────────────────────────────────────────────────────
    @FXML private TableView<Compra>           tablaCompraProducto;
    @FXML private TableColumn<Compra, Number> colCompraId;
    @FXML private TableColumn<Compra, String> colProveedor;
    @FXML private TableColumn<Compra, String> colProductoNombre;
    @FXML private TableColumn<Compra, Number> colProductoCantidad;
    @FXML private TableColumn<Compra, Number> colProductoPrecio;
    @FXML private TableColumn<Compra, Number> colProductoSubtotal;

    private final ObservableList<Compra> listaTemporal = FXCollections.observableArrayList();
    private FilteredList<Compra>         listaFiltrada;
    private int idCompraSeleccionada = -1;  // -1 = modo nueva compra
    private int idPedidoCSeleccionado = -1; // pedido asociado a la compra buscada

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
    }

    // ── Buscar compra existente por ID ────────────────────────────────────
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

                double yaPagedo = rs.getDouble("monto_total") - rs.getDouble("monto_pendiente");
                if (yaPagedo > 0) txtMontoPagado.setText(String.format("%.2f", yaPagedo));

                lblInfoCompra.setText("Compra #" + idC
                        + " — Proveedor: " + rs.getString("proveedor")
                        + " | Total: RD$ " + String.format("%.2f", rs.getDouble("monto_total"))
                        + " | Pendiente: RD$ " + String.format("%.2f", rs.getDouble("monto_pendiente")));
                lblInfoCompra.setStyle("-fx-text-fill: #1B5E20; -fx-font-size: 11px;");

                cargarProductosDeCompra(con, idC);
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
        String sql = """
                SELECT cp.id_producto, cp.cantidad, cp.precio_unitario,
                       cp.id_presentacion, pr.nombre
                FROM TBL_COMPRA_PRODUCTO cp
                JOIN TBL_PRODUCTO pr ON pr.id_producto = cp.id_producto
                WHERE cp.id_compra = ?
                """;
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idCompra);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int    idProd    = rs.getInt("id_producto");
            String nombre    = rs.getString("nombre");
            int    cant      = rs.getInt("cantidad");
            double precio    = rs.getDouble("precio_unitario");
            int    idPres    = rs.getInt("id_presentacion");
            listaTemporal.add(new Compra(
                    idCompra, "", nombre, cant, precio, precio * cant,
                    "", "", "",
                    idProd, 0, idPedidoCSeleccionado, idPres, 0, 0));
        }
        actualizarTotales();
    }

    // ── Agregar producto a la lista temporal ──────────────────────────────
    @FXML
    public void onAgregarProducto(ActionEvent event) {
        String nombre = JOptionPane.showInputDialog(null, "Nombre del producto a buscar:");
        if (nombre == null || nombre.isBlank()) return;

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id_producto, nombre FROM TBL_PRODUCTO WHERE nombre LIKE ?")) {
            ps.setString(1, "%" + nombre.trim() + "%");
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(null, "Producto no encontrado."); return;
            }
            int    idProducto     = rs.getInt("id_producto");
            String nombreProducto = rs.getString("nombre");

            String cantStr   = JOptionPane.showInputDialog(null, "Cantidad de \"" + nombreProducto + "\":");
            if (cantStr == null || cantStr.isBlank()) return;
            String precioStr = JOptionPane.showInputDialog(null, "Precio unitario de compra (RD$):");
            if (precioStr == null || precioStr.isBlank()) return;
            String presStr   = JOptionPane.showInputDialog(null, "ID de Presentación (dejar en 1 si no aplica):");
            int idPres = 1;
            if (presStr != null && !presStr.isBlank()) {
                try { idPres = Integer.parseInt(presStr.trim()); } catch (NumberFormatException ignored) {}
            }

            int    cant   = Integer.parseInt(cantStr.trim());
            double precio = Double.parseDouble(precioStr.trim());

            listaTemporal.add(new Compra(
                    idCompraSeleccionada == -1 ? 0 : idCompraSeleccionada,
                    "", nombreProducto, cant, precio, precio * cant,
                    "", "", "",
                    idProducto, 0, 0, idPres, 0, 0));
            actualizarTotales();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Cantidad y precio deben ser números.");
        }
    }

    // ── Quitar producto de la lista ───────────────────────────────────────
    @FXML
    public void onQuitarProducto(ActionEvent event) {
        Compra sel = tablaCompraProducto.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona un producto."); return; }
        listaTemporal.remove(sel);
        actualizarTotales();
    }

    // ── Registrar nueva compra ─────────────────────────────────────────────
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

        int idProveedor = Integer.parseInt(txtIdProveedor.getText().trim());
        int idEmpleado  = txtIdEmpleado.getText().isBlank() ? 1
                : Integer.parseInt(txtIdEmpleado.getText().trim());

        double montoTotal = listaTemporal.stream().mapToDouble(Compra::getSubtotal).sum();
        double pagado     = parseMontoPagado();
        double pendiente  = Math.max(0, montoTotal - pagado);

        try (Connection con = conexion.establecerConexion()) {

            // 1. Insertar TBL_PEDIDO_C
            PreparedStatement psPedido = con.prepareStatement(
                    "INSERT INTO TBL_PEDIDO_C (id_empleado, id_proveedor, fecha_pedido) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            psPedido.setInt(1, idEmpleado);
            psPedido.setInt(2, idProveedor);
            psPedido.setDate(3, Date.valueOf(dpFechaCompra.getValue()));
            psPedido.executeUpdate();

            int idPedidoC = -1;
            ResultSet keysPedido = psPedido.getGeneratedKeys();
            if (keysPedido.next()) idPedidoC = keysPedido.getInt(1);

            // 2. Insertar TBL_DETALLE_PEDIDO_C
            for (Compra c : listaTemporal) {
                PreparedStatement psDetalle = con.prepareStatement(
                        "INSERT INTO TBL_DETALLE_PEDIDO_C (id_pedido_c, id_producto, cantidad) VALUES (?,?,?)");
                psDetalle.setInt(1, idPedidoC);
                psDetalle.setInt(2, c.getIdProducto());
                psDetalle.setInt(3, c.getCantidad());
                psDetalle.executeUpdate();
            }

            // 3. Insertar TBL_COMPRA
            PreparedStatement psCompra = con.prepareStatement(
                    "INSERT INTO TBL_COMPRA (tipo_compra, fecha_transaccion, monto_total, monto_pendiente, condicion, id_pedido_c) VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            psCompra.setString(1, cmbTipoCompra.getValue());
            psCompra.setDate(2, Date.valueOf(dpFechaCompra.getValue()));
            psCompra.setDouble(3, montoTotal);
            psCompra.setDouble(4, pendiente);
            psCompra.setString(5, txtCondicion.getText().trim());
            psCompra.setInt(6, idPedidoC);
            psCompra.executeUpdate();

            int idCompra = -1;
            ResultSet keysCompra = psCompra.getGeneratedKeys();
            if (keysCompra.next()) idCompra = keysCompra.getInt(1);

            // 4. Insertar TBL_COMPRA_PRODUCTO
            for (Compra c : listaTemporal) {
                PreparedStatement psCP = con.prepareStatement(
                        "INSERT INTO TBL_COMPRA_PRODUCTO (id_compra, id_producto, cantidad, precio_unitario, id_presentacion, fecha_compra) VALUES (?,?,?,?,?,?)");
                psCP.setInt(1, idCompra);
                psCP.setInt(2, c.getIdProducto());
                psCP.setInt(3, c.getCantidad());
                psCP.setDouble(4, c.getPrecioUnitario());
                psCP.setInt(5, c.getIdPresentacion() > 0 ? c.getIdPresentacion() : 1);
                psCP.setDate(6, Date.valueOf(dpFechaCompra.getValue()));
                psCP.executeUpdate();

                // 5. Actualizar stock en TBL_PRODUCTO
                PreparedStatement psStock = con.prepareStatement(
                        "UPDATE TBL_PRODUCTO SET cantidad_disponible = cantidad_disponible + ? WHERE id_producto = ?");
                psStock.setInt(1, c.getCantidad());
                psStock.setInt(2, c.getIdProducto());
                psStock.executeUpdate();
            }

            // 6. Registrar pago si se indicó monto pagado
            if (pagado > 0) {
                registrarPagoCompra(con, idCompra, pagado);
            }

            JOptionPane.showMessageDialog(null, "✔ Compra #" + idCompra + " registrada correctamente.");
            Limpiar();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar compra: " + e.getMessage());
        }
    }

    // ── Editar compra existente ────────────────────────────────────────────
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

            // Actualizar TBL_COMPRA
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE TBL_COMPRA SET tipo_compra=?, fecha_transaccion=?, monto_total=?, monto_pendiente=?, condicion=? WHERE id_compra=?");
            ps.setString(1, cmbTipoCompra.getValue());
            ps.setDate(2, Date.valueOf(dpFechaCompra.getValue()));
            ps.setDouble(3, montoTotal);
            ps.setDouble(4, pendiente);
            ps.setString(5, txtCondicion.getText().trim());
            ps.setInt(6, idCompraSeleccionada);
            ps.executeUpdate();

            // Revertir stock de los productos anteriores antes de borrarlos
            PreparedStatement psOld = con.prepareStatement(
                    "SELECT id_producto, cantidad FROM TBL_COMPRA_PRODUCTO WHERE id_compra=?");
            psOld.setInt(1, idCompraSeleccionada);
            ResultSet rsOld = psOld.executeQuery();
            while (rsOld.next()) {
                PreparedStatement psRev = con.prepareStatement(
                        "UPDATE TBL_PRODUCTO SET cantidad_disponible = cantidad_disponible - ? WHERE id_producto=?");
                psRev.setInt(1, rsOld.getInt("cantidad"));
                psRev.setInt(2, rsOld.getInt("id_producto"));
                psRev.executeUpdate();
            }

            // Borrar detalle anterior y reinsertar
            con.prepareStatement(
                            "DELETE FROM TBL_COMPRA_PRODUCTO WHERE id_compra=" + idCompraSeleccionada)
                    .executeUpdate();

            for (Compra c : listaTemporal) {
                PreparedStatement psCP = con.prepareStatement(
                        "INSERT INTO TBL_COMPRA_PRODUCTO (id_compra, id_producto, cantidad, precio_unitario, id_presentacion, fecha_compra) VALUES (?,?,?,?,?,?)");
                psCP.setInt(1, idCompraSeleccionada);
                psCP.setInt(2, c.getIdProducto());
                psCP.setInt(3, c.getCantidad());
                psCP.setDouble(4, c.getPrecioUnitario());
                psCP.setInt(5, c.getIdPresentacion() > 0 ? c.getIdPresentacion() : 1);
                psCP.setDate(6, Date.valueOf(dpFechaCompra.getValue()));
                psCP.executeUpdate();

                // Actualizar stock con la nueva cantidad
                PreparedStatement psStock = con.prepareStatement(
                        "UPDATE TBL_PRODUCTO SET cantidad_disponible = cantidad_disponible + ? WHERE id_producto=?");
                psStock.setInt(1, c.getCantidad());
                psStock.setInt(2, c.getIdProducto());
                psStock.executeUpdate();
            }

            JOptionPane.showMessageDialog(null, "✔ Compra #" + idCompraSeleccionada + " actualizada.");
            Limpiar();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al editar compra: " + e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /** Inserta un pago en TBL_PAGO y lo vincula en TBL_PAGO_COMPRA (cuenta 1 por defecto). */
    private void registrarPagoCompra(Connection con, int idCompra, double montoPagado) throws SQLException {
        PreparedStatement psPago = con.prepareStatement(
                "INSERT INTO TBL_PAGO (tipo_pago, fecha_pago, monto_pago, metodo_pago, estado_pago) VALUES (?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
        psPago.setString(1, "Compra");
        psPago.setDate(2, Date.valueOf(dpFechaCompra.getValue()));
        psPago.setDouble(3, montoPagado);
        psPago.setString(4, "Transferencia");
        psPago.setBoolean(5, true);
        psPago.executeUpdate();

        int idPago = -1;
        ResultSet keysPago = psPago.getGeneratedKeys();
        if (keysPago.next()) idPago = keysPago.getInt(1);

        PreparedStatement psPagoC = con.prepareStatement(
                "INSERT INTO TBL_PAGO_COMPRA (id_compra, id_cuenta, id_pago) VALUES (?,?,?)");
        psPagoC.setInt(1, idCompra);
        psPagoC.setInt(2, 1); // cuenta predeterminada; se puede parametrizar
        psPagoC.setInt(3, idPago);
        psPagoC.executeUpdate();
    }

    private double parseMontoPagado() {
        if (txtMontoPagado == null || txtMontoPagado.getText().isBlank()) return 0;
        try { return Double.parseDouble(txtMontoPagado.getText().trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    // ── Limpiar ───────────────────────────────────────────────────────────
    @FXML
    public void Limpiar() {
        txtIdCompra.clear();
        txtIdProveedor.clear();
        txtIdEmpleado.clear();
        txtCondicion.clear();
        if (txtBuscarProducto != null) txtBuscarProducto.clear();
        if (txtMontoPagado    != null) txtMontoPagado.clear();
        if (lblInfoCompra     != null) lblInfoCompra.setText("");
        cmbTipoCompra.setValue(null);
        dpFechaCompra.setValue(java.time.LocalDate.now());
        listaTemporal.clear();
        idCompraSeleccionada  = -1;
        idPedidoCSeleccionado = -1;
        lblMontoTotal.setText("RD$ 0.00");
        lblMontoPendiente.setText("RD$ 0.00");
        lblCantProductos.setText("0 productos");
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