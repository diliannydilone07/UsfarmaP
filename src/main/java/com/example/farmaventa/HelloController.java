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

    // Formulario
    @FXML private TextField        txtIdCliente;
    @FXML private TextField        txtIdEmpleado;
    @FXML private ComboBox<String> cmbTipoVenta;
    @FXML private DatePicker       dpFechaVenta;
    @FXML private TextField        txtCondicion;

    // Labels totales
    @FXML private Label lblMontoTotal;
    @FXML private Label lblMontoPendiente;
    @FXML private Label lblCantProductos;

    // Tabla de VENTAS — columnas renombradas para mostrar datos de venta, no de producto
    // (los fx:id del FXML se mantienen igual para no romper nada)
    @FXML private TableView<Venta>           tablaVentaProducto;
    @FXML private TableColumn<Venta, Number> colProductoNombre;    // mostrará: id_venta
    @FXML private TableColumn<Venta, String> colProductoCantidad;  // mostrará: nombre cliente
    @FXML private TableColumn<Venta, String> colProductoPrecio;    // mostrará: fecha
    @FXML private TableColumn<Venta, Number> colProductoSubtotal;  // mostrará: monto_total

    @FXML
    public void initialize() {
        cmbTipoVenta.getItems().addAll("Contado", "Crédito", "Seguro");
        dpFechaVenta.setValue(java.time.LocalDate.now());
        lblMontoTotal.setText("RD$ 0.00");
        lblMontoPendiente.setText("RD$ 0.00");

        // reasignar columnas a datos de venta
        colProductoNombre.setCellValueFactory(c -> c.getValue().idVentaProperty());
        colProductoCantidad.setCellValueFactory(c -> c.getValue().nombreClienteProperty());
        colProductoPrecio.setCellValueFactory(c -> c.getValue().fechaVentaProperty());
        colProductoSubtotal.setCellValueFactory(c -> c.getValue().montoTotalProperty());

        // encabezados de columna que tienen sentido para ventas
        colProductoNombre.setText("#");
        colProductoCantidad.setText("Cliente");
        colProductoPrecio.setText("Fecha");
        colProductoSubtotal.setText("Monto");

        // al seleccionar una fila de la tabla cargar el formulario
        tablaVentaProducto.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) cargarEnFormulario(sel);
        });

        actualizarTabla();
    }

    // Botón "✔ Registrar Venta"
    @FXML
    public void onRegistrarVenta(ActionEvent event) {
        if (txtIdCliente.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El ID de Cliente es obligatorio.");
            return;
        }
        if (cmbTipoVenta.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona el tipo de venta.");
            return;
        }

        String sql = "INSERT INTO TBL_VENTA "
                + "(id_empleado, tipo_venta, fecha_transaccion, monto_total, monto_pendiente, condicion, id_cliente) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1,    txtIdEmpleado.getText().isBlank() ? 1 : Integer.parseInt(txtIdEmpleado.getText().trim()));
            ps.setString(2, cmbTipoVenta.getValue());
            ps.setDate(3,   Date.valueOf(dpFechaVenta.getValue()));
            ps.setDouble(4, 0.0);
            ps.setDouble(5, 0.0);
            ps.setString(6, txtCondicion.getText().trim());
            ps.setInt(7,    Integer.parseInt(txtIdCliente.getText().trim()));
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "Venta registrada correctamente.");
            actualizarTabla();
            Limpiar();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar venta: " + e.getMessage());
        }
    }

    // Botón "🗑 Limpiar Formulario"
    @FXML
    public void onLimpiarFormulario(ActionEvent event) { Limpiar(); }

    @FXML
    public void Limpiar() {
        txtIdCliente.clear();
        txtIdEmpleado.clear();
        txtCondicion.clear();
        cmbTipoVenta.setValue(null);
        dpFechaVenta.setValue(java.time.LocalDate.now());
        lblMontoTotal.setText("RD$ 0.00");
        lblMontoPendiente.setText("RD$ 0.00");
        tablaVentaProducto.getSelectionModel().clearSelection();
    }

    private void actualizarTabla() {
        String sql = "SELECT v.id_venta, v.id_cliente, v.id_empleado, v.tipo_venta, "
                + "CONVERT(VARCHAR, v.fecha_transaccion, 23) AS fecha_transaccion, "
                + "v.condicion, v.monto_total, v.monto_pendiente, "
                + "p.nombre + ' ' + p.apellido AS nombre_cliente "
                + "FROM TBL_VENTA v "
                + "JOIN TBL_CLIENTE c ON c.id_cliente = v.id_cliente "
                + "JOIN TBL_PERSONA p ON p.id_persona = c.id_persona";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ObservableList<Venta> lista = FXCollections.observableArrayList();
            while (rs.next()) {
                lista.add(new Venta(
                        rs.getInt("id_venta"),
                        rs.getInt("id_cliente"),
                        rs.getInt("id_empleado"),
                        rs.getString("tipo_venta"),
                        rs.getString("fecha_transaccion"),
                        rs.getString("condicion"),
                        rs.getDouble("monto_total"),
                        rs.getDouble("monto_pendiente"),
                        rs.getString("nombre_cliente")
                ));
            }
            tablaVentaProducto.setItems(lista);
            if (lblCantProductos != null)
                lblCantProductos.setText(lista.size() + " ventas");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar ventas: " + e.getMessage());
        }
    }

    private void cargarEnFormulario(Venta v) {
        txtIdCliente.setText(String.valueOf(v.getIdCliente()));
        txtIdEmpleado.setText(String.valueOf(v.getIdEmpleado()));
        cmbTipoVenta.setValue(v.getTipoVenta());
        txtCondicion.setText(v.getCondicion());
        lblMontoTotal.setText("RD$ " + v.getMontoTotal());
        lblMontoPendiente.setText("RD$ " + v.getMontoPendiente());
    }
}