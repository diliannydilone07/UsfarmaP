package com.example.farmaventa;

import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.Venta;
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

public class HelloController {

    Conexion conexion = new Conexion();

    // ── Formulario venta ──────────────────────────────────────────────────
    @FXML private TextField        txtIdVenta;
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

    // ── Campos para agregar producto ──────────────────────────────────────
    @FXML private TextField txtIdProducto;
    @FXML private TextField txtNombreProducto;
    @FXML private TextField txtCantidadProducto;
    @FXML private TextField txtPrecioProducto;
    @FXML private TextField txtBuscarProducto;

    // ── Tabla ─────────────────────────────────────────────────────────────
    @FXML private TableView<Venta>           tablaVentaProducto;
    @FXML private TableColumn<Venta, Number> colVentaId;
    @FXML private TableColumn<Venta, String> colVentaCliente;
    @FXML private TableColumn<Venta, String> colProductoNombre;
    @FXML private TableColumn<Venta, Number> colProductoCantidad;
    @FXML private TableColumn<Venta, Number> colProductoPrecio;
    @FXML private TableColumn<Venta, Number> colProductoSubtotal;

    // ── Lista de datos ────────────────────────────────────────────────────
    private ObservableList<Venta> listaTemporal = FXCollections.observableArrayList();
    private int idVentaSeleccionada = -1;

    // ── Inicializar ───────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        cmbTipoVenta.getItems().addAll("Contado", "Credito", "Seguro");
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
                    getClass().getResource("/com/example/farmaventa/SelectorProducto.fxml"));
            Node selectorVista = loader.load();
            SelectorProductoController selectorCtrl = loader.getController();
            selectorCtrl.init(this, contentArea, construirInfoVentaActual());
            contentArea.getChildren().setAll(selectorVista);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al abrir el catálogo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void recibirProductoDelCatalogo(int idProducto, String nombre, int cantidad, double precio) {
        listaTemporal.add(new Venta(idProducto, "", precio, cantidad, precio * cantidad,
                nombre, "", "", "", 0, 0, ""));
        actualizarTotales();
    }

    public void restaurarEstadoEn(HelloController destino) {
        destino.listaTemporal.setAll(this.listaTemporal);
        destino.tablaVentaProducto.setItems(destino.listaTemporal);
        destino.idVentaSeleccionada = this.idVentaSeleccionada;
        if (this.txtIdVenta    != null && destino.txtIdVenta    != null) destino.txtIdVenta.setText(this.txtIdVenta.getText());
        if (this.txtIdCliente  != null && destino.txtIdCliente  != null) destino.txtIdCliente.setText(this.txtIdCliente.getText());
        if (this.txtIdEmpleado != null && destino.txtIdEmpleado != null) destino.txtIdEmpleado.setText(this.txtIdEmpleado.getText());
        if (this.txtCondicion  != null && destino.txtCondicion  != null) destino.txtCondicion.setText(this.txtCondicion.getText());
        if (this.txtMontoPagado != null && destino.txtMontoPagado != null) destino.txtMontoPagado.setText(this.txtMontoPagado.getText());
        if (this.cmbTipoVenta  != null && destino.cmbTipoVenta  != null) destino.cmbTipoVenta.setValue(this.cmbTipoVenta.getValue());
        if (this.dpFechaVenta  != null && destino.dpFechaVenta  != null) destino.dpFechaVenta.setValue(this.dpFechaVenta.getValue());
        if (this.lblInfoVenta  != null && destino.lblInfoVenta  != null) {
            destino.lblInfoVenta.setText(this.lblInfoVenta.getText());
            destino.lblInfoVenta.setStyle(this.lblInfoVenta.getStyle());
        }
        destino.actualizarTotales();
    }

    private String construirInfoVentaActual() {
        if (idVentaSeleccionada != -1)
            return "Editando Venta #" + idVentaSeleccionada + " | " + listaTemporal.size() + " producto(s) en lista";
        String cliente = (txtIdCliente != null && !txtIdCliente.getText().isBlank())
                ? "Cliente #" + txtIdCliente.getText().trim() : "Nueva Venta";
        return cliente + " | " + listaTemporal.size() + " producto(s) en lista";
    }

    // ── Buscar venta existente por ID ─────────────────────────────────────
    @FXML
    public void onBuscarVenta(ActionEvent event) {
        if (txtIdVenta.getText().isBlank()) return;

        String sql = "SELECT v.id_venta, v.id_cliente, v.id_empleado, " +
                "v.tipo_venta, v.condicion, " +
                "CONVERT(VARCHAR(10), v.fecha_transaccion, 120) AS fecha, " +
                "v.monto_total, v.monto_pendiente, " +
                "p.nombre + ' ' + p.apellido AS cliente " +
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
                try { dpFechaVenta.setValue(java.time.LocalDate.parse(rs.getString("fecha"))); }
                catch (Exception ignored) {}

                double montoTotal     = rs.getDouble("monto_total");
                double montoPendiente = rs.getDouble("monto_pendiente");
                double yaPagado       = montoTotal - montoPendiente;

                lblInfoVenta.setText("Venta #" + idV + " - " + rs.getString("cliente")
                        + " | Total: RD$ " + String.format("%.2f", montoTotal)
                        + " | Pendiente: RD$ " + String.format("%.2f", montoPendiente));
                lblInfoVenta.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 11px;");

                if (yaPagado > 0) txtMontoPagado.setText(String.format("%.2f", yaPagado));
                cargarProductosDeVenta(con, idV);
            }
        } catch (NumberFormatException e) {
            lblInfoVenta.setText("ID invalido.");
            lblInfoVenta.setStyle("-fx-text-fill: #C62828; -fx-font-size: 11px;");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    // ── Cargar productos de una venta en la tabla ─────────────────────────
    private void cargarProductosDeVenta(Connection con, int idVenta) throws SQLException {
        listaTemporal.clear();
        String sql = "SELECT vp.id_producto, pr.nombre, vp.cantidad, vp.precio_unitario " +
                "FROM TBL_VENTA_PRODUCTO vp " +
                "JOIN TBL_PRODUCTO pr ON pr.id_producto = vp.id_producto " +
                "WHERE vp.id_venta = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idVenta);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int    idProd = rs.getInt("id_producto");
            String nombre = rs.getString("nombre");
            int    cant   = rs.getInt("cantidad");
            double precio = rs.getDouble("precio_unitario");
            listaTemporal.add(new Venta(idProd, "", precio, cant, precio * cant,
                    nombre, "", "", "", 0, 0, ""));
        }
        tablaVentaProducto.setItems(listaTemporal);
        actualizarTotales();
    }

    // ── Buscar producto por ID → llena nombre y precio ────────────────────
    @FXML
    public void onBuscarProductoId(ActionEvent event) {
        if (txtIdProducto.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Ingresa el ID del producto."); return;
        }
        // Trae nombre Y precio de presentación (pre-llenado automático, solo lectura)
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
                if (txtPrecioProducto != null) {
                    txtPrecioProducto.setText(precio > 0 ? String.format("%.2f", precio) : "");
                    txtPrecioProducto.setEditable(false);
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

    // ── Agregar producto a la lista usando los campos del formulario ───────
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

            listaTemporal.add(new Venta(idProd, "", precio, cant, precio * cant,
                    nombre, "", "", "", 0, 0, ""));
            tablaVentaProducto.setItems(listaTemporal);
            actualizarTotales();

            // Limpiar campos de agregar producto
            txtIdProducto.clear();
            txtNombreProducto.clear();
            txtCantidadProducto.clear();
            if (txtPrecioProducto != null) {
                txtPrecioProducto.clear();
                txtPrecioProducto.setEditable(false);
                txtPrecioProducto.setStyle("-fx-background-color: #F1F8E9; -fx-background-radius: 6; -fx-border-color: #C8E6C9; -fx-border-radius: 6;");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Cantidad y precio deben ser numeros.");
        }
    }

    // ── Buscar en tabla ───────────────────────────────────────────────────
    @FXML
    public void fnBuscarProducto(ActionEvent event) {
        String busqueda = txtBuscarProducto.getText().trim().toLowerCase();
        if (busqueda.isEmpty()) {
            tablaVentaProducto.setItems(listaTemporal);
            return;
        }
        ObservableList<Venta> listaFiltrada = FXCollections.observableArrayList();
        for (Venta v : listaTemporal) {
            if (v.getProducto().toLowerCase().contains(busqueda)) {
                listaFiltrada.add(v);
            }
        }
        tablaVentaProducto.setItems(listaFiltrada);
    }

    // ── Quitar producto de la lista ───────────────────────────────────────
    @FXML
    public void onQuitarProducto(ActionEvent event) {
        Venta sel = tablaVentaProducto.getSelectionModel().getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(null, "Selecciona un producto de la tabla."); return;
        }
        listaTemporal.remove(sel);
        tablaVentaProducto.setItems(listaTemporal);
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

        double montoTotal = calcularTotal();
        double pagado     = parseMontoPagado();
        double pendiente  = Math.max(0, montoTotal - pagado);

        String sql = "INSERT INTO TBL_VENTA (id_empleado, tipo_venta, fecha_transaccion, " +
                "monto_total, monto_pendiente, condicion, id_cliente) VALUES (?,?,?,?,?,?,?)";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1,    txtIdEmpleado.getText().isBlank() ? 1 : Integer.parseInt(txtIdEmpleado.getText().trim()));
            ps.setString(2, cmbTipoVenta.getValue());
            ps.setDate(3,   Date.valueOf(dpFechaVenta.getValue()));
            ps.setDouble(4, montoTotal);
            ps.setDouble(5, pendiente);
            ps.setString(6, txtCondicion.getText().trim());
            ps.setInt(7,    Integer.parseInt(txtIdCliente.getText().trim()));
            ps.executeUpdate();

            int idVenta = -1;
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) idVenta = keys.getInt(1);

            for (Venta v : listaTemporal) {
                PreparedStatement psD = con.prepareStatement(
                        "INSERT INTO TBL_VENTA_PRODUCTO (id_venta, id_producto, cantidad, precio_unitario, fecha_venta, id_presentacion) " +
                                "VALUES (?,?,?,?,?,1)");
                psD.setInt(1,    idVenta);
                psD.setInt(2,    v.getIdVenta());
                psD.setInt(3,    v.getCantidad());
                psD.setDouble(4, v.getTotal());
                psD.setDate(5,   Date.valueOf(dpFechaVenta.getValue()));
                psD.executeUpdate();
            }

            JOptionPane.showMessageDialog(null, "Venta #" + idVenta + " registrada correctamente.");
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

        double montoTotal = calcularTotal();
        double pagado     = parseMontoPagado();
        double pendiente  = Math.max(0, montoTotal - pagado);

        String sql = "UPDATE TBL_VENTA SET id_empleado=?, tipo_venta=?, fecha_transaccion=?, " +
                "monto_total=?, monto_pendiente=?, condicion=?, id_cliente=? WHERE id_venta=?";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1,    txtIdEmpleado.getText().isBlank() ? 1 : Integer.parseInt(txtIdEmpleado.getText().trim()));
            ps.setString(2, cmbTipoVenta.getValue());
            ps.setDate(3,   Date.valueOf(dpFechaVenta.getValue()));
            ps.setDouble(4, montoTotal);
            ps.setDouble(5, pendiente);
            ps.setString(6, txtCondicion.getText().trim());
            ps.setInt(7,    Integer.parseInt(txtIdCliente.getText().trim()));
            ps.setInt(8,    idVentaSeleccionada);
            ps.executeUpdate();

            PreparedStatement psDel = con.prepareStatement(
                    "DELETE FROM TBL_VENTA_PRODUCTO WHERE id_venta=?");
            psDel.setInt(1, idVentaSeleccionada);
            psDel.executeUpdate();

            for (Venta v : listaTemporal) {
                PreparedStatement psD = con.prepareStatement(
                        "INSERT INTO TBL_VENTA_PRODUCTO (id_venta, id_producto, cantidad, precio_unitario, fecha_venta, id_presentacion) " +
                                "VALUES (?,?,?,?,?,1)");
                psD.setInt(1,    idVentaSeleccionada);
                psD.setInt(2,    v.getIdVenta());
                psD.setInt(3,    v.getCantidad());
                psD.setDouble(4, v.getTotal());
                psD.setDate(5,   Date.valueOf(dpFechaVenta.getValue()));
                psD.executeUpdate();
            }

            JOptionPane.showMessageDialog(null, "Venta #" + idVentaSeleccionada + " actualizada.");
            Limpiar();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al editar venta: " + e.getMessage());
        }
    }

    // ── Limpiar formulario ────────────────────────────────────────────────
    @FXML
    public void Limpiar() {
        txtIdVenta.clear();
        txtIdCliente.clear();
        txtIdEmpleado.clear();
        txtCondicion.clear();
        txtMontoPagado.clear();
        txtIdProducto.clear();
        txtNombreProducto.clear();
        txtCantidadProducto.clear();
        txtPrecioProducto.clear();
        if (txtBuscarProducto != null) txtBuscarProducto.clear();
        if (lblInfoVenta      != null) lblInfoVenta.setText("");
        cmbTipoVenta.setValue(null);
        dpFechaVenta.setValue(java.time.LocalDate.now());
        listaTemporal.clear();
        tablaVentaProducto.setItems(listaTemporal);
        idVentaSeleccionada = -1;
        lblMontoTotal.setText("RD$ 0.00");
        lblMontoPendiente.setText("RD$ 0.00");
        lblCantProductos.setText("0 productos");
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private double calcularTotal() {
        double total = 0;
        for (Venta v : listaTemporal) total += v.getSubtotal();
        return total;
    }

    private double parseMontoPagado() {
        if (txtMontoPagado == null || txtMontoPagado.getText().isBlank()) return 0;
        try { return Double.parseDouble(txtMontoPagado.getText().trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    private void actualizarTotales() {
        double total     = calcularTotal();
        double pagado    = parseMontoPagado();
        double pendiente = Math.max(0, total - pagado);
        lblMontoTotal.setText("RD$ " + String.format("%.2f", total));
        lblMontoPendiente.setText("RD$ " + String.format("%.2f", pendiente));
        lblCantProductos.setText(listaTemporal.size() + " productos");
    }
}