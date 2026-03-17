package com.example.farmaventa;

import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.Venta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javax.swing.JOptionPane;
import java.sql.*;

public class HelloController {

    Conexion conexion = new Conexion();

    @FXML private TextField        txtIdCliente;
    @FXML private TextField        txtIdEmpleado;
    @FXML private ComboBox<String> cmbTipoVenta;
    @FXML private DatePicker       dpFechaVenta;
    @FXML private TextField        txtCondicion;
    @FXML private Label            lblMontoTotal;
    @FXML private Label            lblMontoPendiente;
    @FXML private Label            lblCantProductos;

    @FXML private TableView<Venta>           tablaVentaProducto;
    @FXML private TableColumn<Venta, Number> colVentaId;
    @FXML private TableColumn<Venta, String> colVentaCliente;
    @FXML private TableColumn<Venta, String> colProductoNombre;
    @FXML private TableColumn<Venta, Number> colProductoCantidad;
    @FXML private TableColumn<Venta, Number> colProductoPrecio;
    @FXML private TableColumn<Venta, Number> colProductoSubtotal;

    // Lista de lo que el usuario agrega para la venta actual (temporal, no guardado aún)
    private final ObservableList<Venta> listaTemporal = FXCollections.observableArrayList();

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

        tablaVentaProducto.setItems(listaTemporal);
    }

    // Botón "➕ Agregar" → busca producto en BD y lo agrega a la lista temporal
    @FXML
    public void onAgregarProducto(ActionEvent event) {
        String nombre = JOptionPane.showInputDialog(null, "Nombre del producto:");
        if (nombre == null || nombre.isBlank()) return;

        String sql = "SELECT id_producto, nombre FROM TBL_PRODUCTO WHERE nombre LIKE ?";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + nombre.trim() + "%");
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(null, "Producto no encontrado.");
                return;
            }

            int    idProducto     = rs.getInt("id_producto");
            String nombreProducto = rs.getString("nombre");

            String cantStr   = JOptionPane.showInputDialog(null, "Cantidad de \"" + nombreProducto + "\":");
            if (cantStr == null || cantStr.isBlank()) return;
            String precioStr = JOptionPane.showInputDialog(null, "Precio unitario (RD$):");
            if (precioStr == null || precioStr.isBlank()) return;

            int    cant   = Integer.parseInt(cantStr.trim());
            double precio = Double.parseDouble(precioStr.trim());

            // Usamos id = idProducto temporalmente, total = precio unitario, subtotal = total
            Venta fila = new Venta(idProducto, "", precio, cant, precio * cant,
                    nombreProducto, "", "", "", 0, 0, "");
            listaTemporal.add(fila);
            actualizarTotales();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar producto: " + e.getMessage());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Cantidad y precio deben ser números.");
        }
    }

    // Botón "✖ Quitar"
    @FXML
    public void onQuitarProducto(ActionEvent event) {
        Venta sel = tablaVentaProducto.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona un producto."); return; }
        listaTemporal.remove(sel);
        actualizarTotales();
    }

    // Botón "✔ Registrar Venta"
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

        String sqlVenta = "INSERT INTO TBL_VENTA "
                + "(id_empleado, tipo_venta, fecha_transaccion, monto_total, monto_pendiente, condicion, id_cliente) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1,    txtIdEmpleado.getText().isBlank() ? 1 : Integer.parseInt(txtIdEmpleado.getText().trim()));
            ps.setString(2, cmbTipoVenta.getValue());
            ps.setDate(3,   Date.valueOf(dpFechaVenta.getValue()));
            ps.setDouble(4, montoTotal);
            ps.setDouble(5, montoTotal);
            ps.setString(6, txtCondicion.getText().trim());
            ps.setInt(7,    Integer.parseInt(txtIdCliente.getText().trim()));
            ps.executeUpdate();

            int idVenta = -1;
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) idVenta = keys.getInt(1);

            // Insertar detalle en TBL_VENTA_PRODUCTO
            String sqlDetalle = "INSERT INTO TBL_VENTA_PRODUCTO "
                    + "(id_venta, id_producto, cantidad, precio_unitario, fecha_venta, id_presentacion) "
                    + "VALUES (?, ?, ?, ?, ?, 1)";

            for (Venta v : listaTemporal) {
                PreparedStatement psD = con.prepareStatement(sqlDetalle);
                psD.setInt(1,    idVenta);
                psD.setInt(2,    v.getIdVenta()); // id_producto guardado aquí temporalmente
                psD.setInt(3,    v.getCantidad());
                psD.setDouble(4, v.getTotal());   // precio unitario
                psD.setDate(5,   Date.valueOf(dpFechaVenta.getValue()));
                psD.executeUpdate();
            }

            JOptionPane.showMessageDialog(null, "Venta #" + idVenta + " registrada correctamente.");
            Limpiar();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar venta: " + e.getMessage());
        }
    }

    // Botón "🗑 Limpiar"
    @FXML
    public void Limpiar() {
        txtIdCliente.clear();
        txtIdEmpleado.clear();
        txtCondicion.clear();
        cmbTipoVenta.setValue(null);
        dpFechaVenta.setValue(java.time.LocalDate.now());
        listaTemporal.clear();
        lblMontoTotal.setText("RD$ 0.00");
        lblMontoPendiente.setText("RD$ 0.00");
        lblCantProductos.setText("0 productos");
    }

    private void actualizarTotales() {
        double total = listaTemporal.stream().mapToDouble(Venta::getSubtotal).sum();
        lblMontoTotal.setText("RD$ " + String.format("%.2f", total));
        lblMontoPendiente.setText("RD$ " + String.format("%.2f", total));
        lblCantProductos.setText(listaTemporal.size() + " productos");
    }
}