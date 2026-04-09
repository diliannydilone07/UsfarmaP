package com.example.farmaventa;

import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.Venta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javax.swing.JOptionPane;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HelloController {

    Conexion conexion = new Conexion();

    @FXML private TextField        txtIdVenta;
    @FXML private Label            lblInfoVenta;
    @FXML private TextField        txtIdCliente;
    @FXML private TextField        txtIdEmpleado;
    @FXML private ComboBox<String> cmbTipoVenta;
    @FXML private DatePicker       dpFechaVenta;
    @FXML private TextField        txtCondicion;
    @FXML private TextField        txtMontoPagado;
    @FXML private ComboBox<String> cmbMetodoPago;
    @FXML private ComboBox<String> cmbTipoPago;
    @FXML private Label            lblMontoTotal;
    @FXML private Label            lblMontoPendiente;
    @FXML private Label            lblCantProductos;
    @FXML private TextField        txtBuscarProducto;

    @FXML private TableView<Venta>           tablaVentaProducto;
    @FXML private TableColumn<Venta, Number> colVentaId;
    @FXML private TableColumn<Venta, String> colVentaCliente;
    @FXML private TableColumn<Venta, String> colProductoNombre;
    @FXML private TableColumn<Venta, Number> colProductoCantidad;
    @FXML private TableColumn<Venta, Number> colProductoPrecio;
    @FXML private TableColumn<Venta, Number> colProductoSubtotal;

    private final ObservableList<Venta> listaTemporal = FXCollections.observableArrayList();
    private FilteredList<Venta> listaFiltrada;
    private int idVentaSeleccionada = -1;

    @FXML
    public void initialize() {
        cmbTipoVenta.getItems().addAll("Contado", "Crédito", "Seguro");
        cmbMetodoPago.getItems().addAll("Efectivo", "Tarjeta", "Transferencia", "Seguro");
        cmbTipoPago.getItems().addAll("Venta", "Abono", "Anticipo");
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

        txtMontoPagado.textProperty().addListener((obs, o, n) -> actualizarTotales());

        listaFiltrada = new FilteredList<>(listaTemporal, p -> true);
        tablaVentaProducto.setItems(listaFiltrada);

        txtBuscarProducto.textProperty().addListener((obs, o, n) ->
                listaFiltrada.setPredicate(v -> {
                    if (n == null || n.isBlank()) return true;
                    return v.getProducto().toLowerCase().contains(n.toLowerCase());
                })
        );
    }

    @FXML
    public void onBuscarVenta(ActionEvent event) {
        if (txtIdVenta.getText().isBlank()) return;
        try {
            int idV = Integer.parseInt(txtIdVenta.getText().trim());
            String sql = """
                    SELECT v.id_venta, v.id_cliente, v.id_empleado,
                           v.tipo_venta, v.condicion,
                           CONVERT(VARCHAR(10), v.fecha_transaccion, 120) AS fecha,
                           v.monto_total, v.monto_pendiente,
                           CONCAT(p.nombre, ' ', p.apellido) AS cliente
                    FROM TBL_VENTA v
                    JOIN TBL_CLIENTE cl ON cl.id_cliente = v.id_cliente
                    JOIN TBL_PERSONA p  ON p.id_persona  = cl.id_persona
                    WHERE v.id_venta = ?
                    """;
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
                try { dpFechaVenta.setValue(java.time.LocalDate.parse(rs.getString("fecha"))); }
                catch (Exception ignored) {}

                lblInfoVenta.setText("Venta #" + idV + " — " + rs.getString("cliente")
                        + " | Total: RD$ "     + String.format("%.2f", rs.getDouble("monto_total"))
                        + " | Pendiente: RD$ "  + String.format("%.2f", rs.getDouble("monto_pendiente")));
                lblInfoVenta.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 11px;");

                double yaPagado = rs.getDouble("monto_total") - rs.getDouble("monto_pendiente");
                if (yaPagado > 0)
                    txtMontoPagado.setText(String.format("%.2f", yaPagado));

                cargarProductosDeVenta(con, idV);
            }
        } catch (NumberFormatException e) {
            lblInfoVenta.setText("ID inválido.");
            lblInfoVenta.setStyle("-fx-text-fill: #C62828; -fx-font-size: 11px;");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private void cargarProductosDeVenta(Connection con, int idVenta) throws SQLException {
        listaTemporal.clear();
        String sql = """
                SELECT vp.id_producto, pr.nombre,
                       vp.cantidad, vp.precio_unitario, vp.id_presentacion
                FROM TBL_VENTA_PRODUCTO vp
                JOIN TBL_PRODUCTO pr ON pr.id_producto = vp.id_producto
                WHERE vp.id_venta = ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int    idProd  = rs.getInt("id_producto");
                String nombre  = rs.getString("nombre");
                int    cant    = rs.getInt("cantidad");
                double precio  = rs.getDouble("precio_unitario");
                int    idPres  = rs.getInt("id_presentacion");
                Venta v = new Venta(idProd, "", precio, cant, precio * cant,
                        nombre, "", "", "", 0, 0, "");
                v.setIdPresentacion(idPres);
                listaTemporal.add(v);
            }
        }
        actualizarTotales();
    }

    @FXML
    public void onAgregarProducto(ActionEvent event) {
        String nombre = JOptionPane.showInputDialog(null, "Nombre del producto:");
        if (nombre == null || nombre.isBlank()) return;

        try (Connection con = conexion.establecerConexion()) {

            PreparedStatement psProd = con.prepareStatement(
                    "SELECT id_producto, nombre FROM TBL_PRODUCTO WHERE nombre LIKE ?");
            psProd.setString(1, "%" + nombre.trim() + "%");
            ResultSet rsProd = psProd.executeQuery();

            if (!rsProd.next()) {
                JOptionPane.showMessageDialog(null, "Producto no encontrado.");
                return;
            }
            int    idProducto     = rsProd.getInt("id_producto");
            String nombreProducto = rsProd.getString("nombre");

            PreparedStatement psPres = con.prepareStatement("""
                    SELECT pp.id_presentacion,
                           pres.nombre   AS presentacion,
                           pp.precio_venta,
                           pp.fecha_caducidad
                    FROM TBL_PRESENTACION_PRODUCTO pp
                    JOIN TBL_PRESENTACION pres ON pres.id_presentacion = pp.id_presentacion
                    WHERE pp.id_producto = ?
                      AND pp.fecha_caducidad >= CAST(GETDATE() AS DATE)
                    """);
            psPres.setInt(1, idProducto);
            ResultSet rsPres = psPres.executeQuery();

            List<Integer> idsPresent = new ArrayList<>();
            List<Double>  precios    = new ArrayList<>();
            StringBuilder opciones   = new StringBuilder("Presentaciones disponibles:\n");
            int idx = 1;
            while (rsPres.next()) {
                idsPresent.add(rsPres.getInt("id_presentacion"));
                precios.add(rsPres.getDouble("precio_venta"));
                opciones.append(idx).append(") ")
                        .append(rsPres.getString("presentacion"))
                        .append(" — RD$ ").append(String.format("%.2f", rsPres.getDouble("precio_venta")))
                        .append("  (cad: ").append(rsPres.getString("fecha_caducidad")).append(")\n");
                idx++;
            }

            if (idsPresent.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "No hay presentaciones activas para \"" + nombreProducto + "\".");
                return;
            }

            String selStr = JOptionPane.showInputDialog(null,
                    opciones + "\nIngresa el número de presentación:");
            if (selStr == null || selStr.isBlank()) return;

            int selIdx;
            try { selIdx = Integer.parseInt(selStr.trim()) - 1; }
            catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Opción inválida."); return;
            }
            if (selIdx < 0 || selIdx >= idsPresent.size()) {
                JOptionPane.showMessageDialog(null, "Opción fuera de rango."); return;
            }

            int    idPresentacion = idsPresent.get(selIdx);
            double precioVenta    = precios.get(selIdx);

            String cantStr = JOptionPane.showInputDialog(null,
                    "Cantidad de \"" + nombreProducto + "\":");
            if (cantStr == null || cantStr.isBlank()) return;

            int cant;
            try { cant = Integer.parseInt(cantStr.trim()); }
            catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Cantidad debe ser un número entero."); return;
            }
            if (cant <= 0) {
                JOptionPane.showMessageDialog(null, "Cantidad debe ser mayor a 0."); return;
            }

            Venta v = new Venta(idProducto, "", precioVenta, cant, precioVenta * cant,
                    nombreProducto, "", "", "", 0, 0, "");
            v.setIdPresentacion(idPresentacion);
            listaTemporal.add(v);
            actualizarTotales();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    @FXML
    public void onQuitarProducto(ActionEvent event) {
        Venta sel = tablaVentaProducto.getSelectionModel().getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(null, "Selecciona un producto.");
            return;
        }
        listaTemporal.remove(sel);
        actualizarTotales();
    }

    @FXML
    public void onRegistrarVenta(ActionEvent event) {
        if (txtIdCliente.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El ID de Cliente es obligatorio."); return;
        }
        if (cmbTipoVenta.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona el tipo de venta."); return;
        }
        if (listaTemporal.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Agrega al menos un producto."); return;
        }
        if (cmbMetodoPago.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona el método de pago."); return;
        }
        if (cmbTipoPago.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona el tipo de pago."); return;
        }

        String metodoPago = cmbMetodoPago.getValue();
        String tipoPago   = cmbTipoPago.getValue();
        double montoTotal = listaTemporal.stream().mapToDouble(Venta::getSubtotal).sum();
        double pagado     = parseMontoPagado();
        double pendiente  = Math.max(0, montoTotal - pagado);

        try (Connection con = conexion.establecerConexion()) {
            con.setAutoCommit(false);
            try {
                PreparedStatement psVenta = con.prepareStatement(
                        "INSERT INTO TBL_VENTA (id_empleado, tipo_venta, fecha_transaccion, monto_total, monto_pendiente, condicion, id_cliente) VALUES (?,?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);
                psVenta.setInt(1,    txtIdEmpleado.getText().isBlank()
                        ? 1 : Integer.parseInt(txtIdEmpleado.getText().trim()));
                psVenta.setString(2, cmbTipoVenta.getValue());
                psVenta.setDate(3,   Date.valueOf(dpFechaVenta.getValue()));
                psVenta.setDouble(4, montoTotal);
                psVenta.setDouble(5, pendiente);
                psVenta.setString(6, txtCondicion.getText().trim());
                psVenta.setInt(7,    Integer.parseInt(txtIdCliente.getText().trim()));
                psVenta.executeUpdate();

                int idVenta = -1;
                ResultSet keys = psVenta.getGeneratedKeys();
                if (keys.next()) idVenta = keys.getInt(1);

                for (Venta v : listaTemporal) {
                    PreparedStatement psDetalle = con.prepareStatement(
                            "INSERT INTO TBL_VENTA_PRODUCTO (id_venta, id_producto, cantidad, precio_unitario, fecha_venta, id_presentacion) VALUES (?,?,?,?,?,?)");
                    psDetalle.setInt(1,    idVenta);
                    psDetalle.setInt(2,    v.getIdVenta());
                    psDetalle.setInt(3,    v.getCantidad());
                    psDetalle.setDouble(4, v.getTotal());
                    psDetalle.setDate(5,   Date.valueOf(dpFechaVenta.getValue()));
                    psDetalle.setInt(6,    v.getIdPresentacion());
                    psDetalle.executeUpdate();
                }

                if (pagado > 0) {
                    PreparedStatement psPago = con.prepareStatement(
                            "INSERT INTO TBL_PAGO (tipo_pago, fecha_pago, monto_pago, metodo_pago, estado_pago) VALUES (?,?,?,?,?)",
                            Statement.RETURN_GENERATED_KEYS);
                    psPago.setString(1,  tipoPago);
                    psPago.setDate(2,    Date.valueOf(dpFechaVenta.getValue()));
                    psPago.setDouble(3,  pagado);
                    psPago.setString(4,  metodoPago);
                    psPago.setBoolean(5, pendiente == 0);
                    psPago.executeUpdate();

                    int idPago = -1;
                    ResultSet keysPago = psPago.getGeneratedKeys();
                    if (keysPago.next()) idPago = keysPago.getInt(1);

                    PreparedStatement psPagoVenta = con.prepareStatement(
                            "INSERT INTO TBL_PAGO_VENTA (id_venta, id_pago) VALUES (?,?)");
                    psPagoVenta.setInt(1, idVenta);
                    psPagoVenta.setInt(2, idPago);
                    psPagoVenta.executeUpdate();
                }

                con.commit();
                JOptionPane.showMessageDialog(null, "✔ Venta #" + idVenta + " registrada correctamente.");
                Limpiar();

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar venta: " + e.getMessage());
        }
    }

    @FXML
    public void onEditarVenta(ActionEvent event) {
        if (idVentaSeleccionada == -1) {
            JOptionPane.showMessageDialog(null, "Primero busca una venta por ID."); return;
        }
        if (txtIdCliente.getText().isBlank() || cmbTipoVenta.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Cliente y tipo de venta son obligatorios."); return;
        }
        if (cmbMetodoPago.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona el método de pago."); return;
        }
        if (cmbTipoPago.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona el tipo de pago."); return;
        }

        String metodoPago = cmbMetodoPago.getValue();
        String tipoPago   = cmbTipoPago.getValue();
        double montoTotal = listaTemporal.stream().mapToDouble(Venta::getSubtotal).sum();
        double pagado     = parseMontoPagado();
        double pendiente  = Math.max(0, montoTotal - pagado);

        try (Connection con = conexion.establecerConexion()) {
            con.setAutoCommit(false);
            try {
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE TBL_VENTA SET id_empleado=?, tipo_venta=?, fecha_transaccion=?, monto_total=?, monto_pendiente=?, condicion=?, id_cliente=? WHERE id_venta=?");
                ps.setInt(1,    txtIdEmpleado.getText().isBlank()
                        ? 1 : Integer.parseInt(txtIdEmpleado.getText().trim()));
                ps.setString(2, cmbTipoVenta.getValue());
                ps.setDate(3,   Date.valueOf(dpFechaVenta.getValue()));
                ps.setDouble(4, montoTotal);
                ps.setDouble(5, pendiente);
                ps.setString(6, txtCondicion.getText().trim());
                ps.setInt(7,    Integer.parseInt(txtIdCliente.getText().trim()));
                ps.setInt(8,    idVentaSeleccionada);
                ps.executeUpdate();

                con.prepareStatement(
                                "DELETE FROM TBL_VENTA_PRODUCTO WHERE id_venta=" + idVentaSeleccionada)
                        .executeUpdate();

                for (Venta v : listaTemporal) {
                    PreparedStatement psD = con.prepareStatement(
                            "INSERT INTO TBL_VENTA_PRODUCTO (id_venta, id_producto, cantidad, precio_unitario, fecha_venta, id_presentacion) VALUES (?,?,?,?,?,?)");
                    psD.setInt(1,    idVentaSeleccionada);
                    psD.setInt(2,    v.getIdVenta());
                    psD.setInt(3,    v.getCantidad());
                    psD.setDouble(4, v.getTotal());
                    psD.setDate(5,   Date.valueOf(dpFechaVenta.getValue()));
                    psD.setInt(6,    v.getIdPresentacion());
                    psD.executeUpdate();
                }

                if (pagado > 0) {
                    PreparedStatement psPago = con.prepareStatement(
                            "INSERT INTO TBL_PAGO (tipo_pago, fecha_pago, monto_pago, metodo_pago, estado_pago) VALUES (?,?,?,?,?)",
                            Statement.RETURN_GENERATED_KEYS);
                    psPago.setString(1,  tipoPago);
                    psPago.setDate(2,    Date.valueOf(dpFechaVenta.getValue()));
                    psPago.setDouble(3,  pagado);
                    psPago.setString(4,  metodoPago);
                    psPago.setBoolean(5, pendiente == 0);
                    psPago.executeUpdate();

                    int idPago = -1;
                    ResultSet kp = psPago.getGeneratedKeys();
                    if (kp.next()) idPago = kp.getInt(1);

                    PreparedStatement psPV = con.prepareStatement(
                            "INSERT INTO TBL_PAGO_VENTA (id_venta, id_pago) VALUES (?,?)");
                    psPV.setInt(1, idVentaSeleccionada);
                    psPV.setInt(2, idPago);
                    psPV.executeUpdate();
                }

                con.commit();
                JOptionPane.showMessageDialog(null, "✔ Venta #" + idVentaSeleccionada + " actualizada.");
                Limpiar();

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al editar venta: " + e.getMessage());
        }
    }

    @FXML
    public void Limpiar() {
        txtIdVenta.clear();
        txtIdCliente.clear();
        txtIdEmpleado.clear();
        txtCondicion.clear();
        txtBuscarProducto.clear();
        txtMontoPagado.clear();
        lblInfoVenta.setText("");
        cmbTipoVenta.setValue(null);
        cmbMetodoPago.setValue(null);
        cmbTipoPago.setValue(null);
        dpFechaVenta.setValue(java.time.LocalDate.now());
        listaTemporal.clear();
        idVentaSeleccionada = -1;
        lblMontoTotal.setText("RD$ 0.00");
        lblMontoPendiente.setText("RD$ 0.00");
        lblCantProductos.setText("0 productos");
    }

    private double parseMontoPagado() {
        if (txtMontoPagado == null || txtMontoPagado.getText().isBlank()) return 0;
        try { return Double.parseDouble(txtMontoPagado.getText().trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    private void actualizarTotales() {
        double total     = listaTemporal.stream().mapToDouble(Venta::getSubtotal).sum();
        double pagado    = parseMontoPagado();
        double pendiente = Math.max(0, total - pagado);
        lblMontoTotal.setText("RD$ "    + String.format("%.2f", total));
        lblMontoPendiente.setText("RD$ " + String.format("%.2f", pendiente));
        lblCantProductos.setText(listaTemporal.size() + " productos");
    }
}