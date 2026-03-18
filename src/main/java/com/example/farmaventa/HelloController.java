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

public class HelloController {

    Conexion conexion = new Conexion();

    // ── Formulario ────────────────────────────────────────────────────────
    @FXML private TextField        txtIdVenta;      // buscar venta existente
    @FXML private Label            lblInfoVenta;
    @FXML private TextField        txtIdCliente;
    @FXML private TextField        txtIdEmpleado;
    @FXML private ComboBox<String> cmbTipoVenta;
    @FXML private DatePicker       dpFechaVenta;
    @FXML private TextField        txtCondicion;
    @FXML private TextField        txtMontoPagado;
    @FXML private Label            lblMontoTotal;
    @FXML private Label            lblMontoPendiente;
    @FXML private Label            lblCantProductos;
    @FXML private TextField        txtBuscarProducto;

    // ── Tabla ─────────────────────────────────────────────────────────────
    @FXML private TableView<Venta>           tablaVentaProducto;
    @FXML private TableColumn<Venta, Number> colVentaId;
    @FXML private TableColumn<Venta, String> colVentaCliente;
    @FXML private TableColumn<Venta, String> colProductoNombre;
    @FXML private TableColumn<Venta, Number> colProductoCantidad;
    @FXML private TableColumn<Venta, Number> colProductoPrecio;
    @FXML private TableColumn<Venta, Number> colProductoSubtotal;

    private final ObservableList<Venta> listaTemporal = FXCollections.observableArrayList();
    private FilteredList<Venta>         listaFiltrada;
    private int idVentaSeleccionada = -1; // -1 = modo nueva venta

    // ══════════════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        cmbTipoVenta.getItems().addAll("Contado", "Crédito", "Seguro");
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

        // Actualizar pendiente en tiempo real al escribir monto pagado
        txtMontoPagado.textProperty().addListener((obs, o, n) -> actualizarTotales());

        listaFiltrada = new FilteredList<>(listaTemporal, p -> true);
        tablaVentaProducto.setItems(listaFiltrada);

        if (txtBuscarProducto != null) {
            txtBuscarProducto.textProperty().addListener((obs, o, n) ->
                    listaFiltrada.setPredicate(v -> {
                        if (n == null || n.isBlank()) return true;
                        return v.getProducto().toLowerCase().contains(n.toLowerCase());
                    })
            );
        }
    }

    // ── Buscar venta existente por ID ─────────────────────────────────────
    @FXML
    public void onBuscarVenta(ActionEvent event) {
        if (txtIdVenta.getText().isBlank()) return;
        try {
            int idV = Integer.parseInt(txtIdVenta.getText().trim());
            String sql = """
                    SELECT v.id_venta, v.id_cliente, v.id_empleado,
                           v.tipo_venta, v.condicion,
                           CONVERT(VARCHAR(10), v.fecha_transaccion, 120) AS fecha,
                           v.monto_total,
                           v.monto_pendiente,
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

                // Cargar datos en el formulario
                idVentaSeleccionada = rs.getInt("id_venta");
                txtIdCliente.setText(String.valueOf(rs.getInt("id_cliente")));
                txtIdEmpleado.setText(String.valueOf(rs.getInt("id_empleado")));
                cmbTipoVenta.setValue(rs.getString("tipo_venta"));
                txtCondicion.setText(rs.getString("condicion") != null ? rs.getString("condicion") : "");
                try { dpFechaVenta.setValue(java.time.LocalDate.parse(rs.getString("fecha"))); }
                catch (Exception ignored) {}

                double montoPend = rs.getDouble("monto_total") - 0; // se recalcula al cargar
                lblInfoVenta.setText("Venta #" + idV + " - " + rs.getString("cliente")
                        + " | Total: RD$ " + String.format("%.2f", rs.getDouble("monto_total"))
                        + " | Pendiente: RD$ " + String.format("%.2f", rs.getDouble("monto_pendiente")));
                lblInfoVenta.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 11px;");
                // Cargar monto ya pagado
                double yaPagedo = rs.getDouble("monto_total") - rs.getDouble("monto_pendiente");
                if (txtMontoPagado != null && yaPagedo > 0)
                    txtMontoPagado.setText(String.format("%.2f", yaPagedo));

                // Cargar productos de la venta en la tabla
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
                SELECT vp.id_producto, pr.nombre, vp.cantidad, vp.precio_unitario
                FROM TBL_VENTA_PRODUCTO vp
                JOIN TBL_PRODUCTO pr ON pr.id_producto = vp.id_producto
                WHERE vp.id_venta = ?
                """;
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idVenta);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int    idProd  = rs.getInt("id_producto");
            String nombre  = rs.getString("nombre");
            int    cant    = rs.getInt("cantidad");
            double precio  = rs.getDouble("precio_unitario");
            listaTemporal.add(new Venta(idProd, "", precio, cant, precio * cant,
                    nombre, "", "", "", 0, 0, ""));
        }
        actualizarTotales();
    }

    // ── Agregar producto a la lista temporal ──────────────────────────────
    @FXML
    public void onAgregarProducto(ActionEvent event) {
        String nombre = JOptionPane.showInputDialog(null, "Nombre del producto:");
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
            String precioStr = JOptionPane.showInputDialog(null, "Precio unitario (RD$):");
            if (precioStr == null || precioStr.isBlank()) return;

            int    cant   = Integer.parseInt(cantStr.trim());
            double precio = Double.parseDouble(precioStr.trim());

            listaTemporal.add(new Venta(idProducto, "", precio, cant, precio * cant,
                    nombreProducto, "", "", "", 0, 0, ""));
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
        Venta sel = tablaVentaProducto.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona un producto."); return; }
        listaTemporal.remove(sel);
        actualizarTotales();
    }

    // ── Registrar nueva venta ─────────────────────────────────────────────
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

        double montoTotal = listaTemporal.stream().mapToDouble(Venta::getSubtotal).sum();

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO TBL_VENTA (id_empleado, tipo_venta, fecha_transaccion, monto_total, monto_pendiente, condicion, id_cliente) VALUES (?,?,?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1,    txtIdEmpleado.getText().isBlank() ? 1 : Integer.parseInt(txtIdEmpleado.getText().trim()));
            ps.setString(2, cmbTipoVenta.getValue());
            ps.setDate(3,   Date.valueOf(dpFechaVenta.getValue()));
            double pagadoReg = 0;
            if (txtMontoPagado != null && !txtMontoPagado.getText().isBlank()) {
                try { pagadoReg = Double.parseDouble(txtMontoPagado.getText().trim()); }
                catch (NumberFormatException ignored) {}
            }
            double pendienteReg = Math.max(0, montoTotal - pagadoReg);
            ps.setDouble(4, montoTotal);
            ps.setDouble(5, pendienteReg);
            ps.setString(6, txtCondicion.getText().trim());
            ps.setInt(7,    Integer.parseInt(txtIdCliente.getText().trim()));
            ps.executeUpdate();

            int idVenta = -1;
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) idVenta = keys.getInt(1);

            for (Venta v : listaTemporal) {
                PreparedStatement psD = con.prepareStatement(
                        "INSERT INTO TBL_VENTA_PRODUCTO (id_venta, id_producto, cantidad, precio_unitario, fecha_venta, id_presentacion) VALUES (?,?,?,?,?,1)");
                psD.setInt(1, idVenta);
                psD.setInt(2, v.getIdVenta());
                psD.setInt(3, v.getCantidad());
                psD.setDouble(4, v.getTotal());
                psD.setDate(5, Date.valueOf(dpFechaVenta.getValue()));
                psD.executeUpdate();
            }

            JOptionPane.showMessageDialog(null, "✔ Venta #" + idVenta + " registrada correctamente.");
            Limpiar();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar venta: " + e.getMessage());
        }
    }

    // ── Editar venta existente ────────────────────────────────────────────
    @FXML
    public void onEditarVenta(ActionEvent event) {
        if (idVentaSeleccionada == -1) {
            JOptionPane.showMessageDialog(null, "Primero busca una venta por ID."); return;
        }
        if (txtIdCliente.getText().isBlank() || cmbTipoVenta.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Cliente y tipo de venta son obligatorios."); return;
        }

        double montoTotal = listaTemporal.stream().mapToDouble(Venta::getSubtotal).sum();

        try (Connection con = conexion.establecerConexion()) {

            // Actualizar cabecera
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE TBL_VENTA SET id_empleado=?, tipo_venta=?, fecha_transaccion=?, monto_total=?, monto_pendiente=?, condicion=?, id_cliente=? WHERE id_venta=?");
            ps.setInt(1,    txtIdEmpleado.getText().isBlank() ? 1 : Integer.parseInt(txtIdEmpleado.getText().trim()));
            ps.setString(2, cmbTipoVenta.getValue());
            ps.setDate(3,   Date.valueOf(dpFechaVenta.getValue()));
            double pagadoReg = 0;
            if (txtMontoPagado != null && !txtMontoPagado.getText().isBlank()) {
                try { pagadoReg = Double.parseDouble(txtMontoPagado.getText().trim()); }
                catch (NumberFormatException ignored) {}
            }
            double pendienteReg = Math.max(0, montoTotal - pagadoReg);
            ps.setDouble(4, montoTotal);
            ps.setDouble(5, pendienteReg);
            ps.setString(6, txtCondicion.getText().trim());
            ps.setInt(7,    Integer.parseInt(txtIdCliente.getText().trim()));
            ps.setInt(8,    idVentaSeleccionada);
            ps.executeUpdate();

            // Reemplazar productos: borrar los anteriores e insertar los actuales
            con.prepareStatement(
                    "DELETE FROM TBL_VENTA_PRODUCTO WHERE id_venta=" + idVentaSeleccionada).executeUpdate();

            for (Venta v : listaTemporal) {
                PreparedStatement psD = con.prepareStatement(
                        "INSERT INTO TBL_VENTA_PRODUCTO (id_venta, id_producto, cantidad, precio_unitario, fecha_venta, id_presentacion) VALUES (?,?,?,?,?,1)");
                psD.setInt(1, idVentaSeleccionada);
                psD.setInt(2, v.getIdVenta());
                psD.setInt(3, v.getCantidad());
                psD.setDouble(4, v.getTotal());
                psD.setDate(5, Date.valueOf(dpFechaVenta.getValue()));
                psD.executeUpdate();
            }

            JOptionPane.showMessageDialog(null, "✔ Venta #" + idVentaSeleccionada + " actualizada.");
            Limpiar();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al editar venta: " + e.getMessage());
        }
    }

    // ── Limpiar ───────────────────────────────────────────────────────────
    @FXML
    public void Limpiar() {
        txtIdVenta.clear();
        txtIdCliente.clear();
        txtIdEmpleado.clear();
        txtCondicion.clear();
        if (txtBuscarProducto != null) txtBuscarProducto.clear();
        if (txtMontoPagado != null) txtMontoPagado.clear();
        if (lblInfoVenta != null) lblInfoVenta.setText("");
        cmbTipoVenta.setValue(null);
        dpFechaVenta.setValue(java.time.LocalDate.now());
        listaTemporal.clear();
        idVentaSeleccionada = -1;
        lblMontoTotal.setText("RD$ 0.00");
        lblMontoPendiente.setText("RD$ 0.00");
        lblCantProductos.setText("0 productos");
    }

    private void actualizarTotales() {
        double total = listaTemporal.stream().mapToDouble(Venta::getSubtotal).sum();
        double pagado = 0;
        if (txtMontoPagado != null && !txtMontoPagado.getText().isBlank()) {
            try { pagado = Double.parseDouble(txtMontoPagado.getText().trim()); }
            catch (NumberFormatException ignored) {}
        }
        double pendiente = Math.max(0, total - pagado);
        lblMontoTotal.setText("RD$ " + String.format("%.2f", total));
        lblMontoPendiente.setText("RD$ " + String.format("%.2f", pendiente));
        lblCantProductos.setText(listaTemporal.size() + " productos");
    }
}