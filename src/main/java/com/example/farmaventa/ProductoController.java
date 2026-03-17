package com.example.farmaventa;

import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.Producto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javax.swing.JOptionPane;
import java.sql.*;

public class ProductoController {

    Conexion conexion = new Conexion();

    @FXML private TextField        txtId;
    @FXML private TextField        txtNombre;
    @FXML private TextField        txtStockActual;
    @FXML private TextField        txtStockMinimo;
    @FXML private TextField        txtPrecio;
    @FXML private TextField        txtDescuento;
    @FXML private TextField        txtUbicacion;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TextField        txtBusqueda;

    @FXML private TableView<Producto>           tablaProductos;
    @FXML private TableColumn<Producto, Number> colId;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, Number> colStock;
    @FXML private TableColumn<Producto, Number> colMinimo;
    @FXML private TableColumn<Producto, Number> colPrecio;
    @FXML private TableColumn<Producto, Number> colDescuento;
    @FXML private TableColumn<Producto, String> colUbicacion;

    @FXML private Spinner<Integer> spinCantidad;

    @FXML
    public void initialize() {
        spinCantidad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));

        cargarComboCategorias();

        tablaProductos.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) cargarEnFormulario(sel);
        });

        colId.setCellValueFactory(c -> c.getValue().idProductoProperty());
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colCategoria.setCellValueFactory(c -> c.getValue().categoriaProperty());
        colStock.setCellValueFactory(c -> c.getValue().stockActualProperty());
        colMinimo.setCellValueFactory(c -> c.getValue().stockMinimoProperty());
        colDescuento.setCellValueFactory(c -> c.getValue().descuentoProperty());
        colUbicacion.setCellValueFactory(c -> c.getValue().ubicacionProperty());

        // colPrecio no está en TBL_PRODUCTO, se deja vacío por ahora
        if (colPrecio != null) colPrecio.setCellValueFactory(c -> c.getValue().descuentoProperty());

        actualizarTabla();
    }

    private void cargarComboCategorias() {
        String sql = "SELECT nombre_categoria FROM TBL_CATEGORIA_DE_PRODUCTO";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cmbCategoria.getItems().add(rs.getString("nombre_categoria"));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar categorías: " + e.getMessage());
        }
    }

    // Botón "💾 Guardar Producto" → onAction="#onGuardarProductoClick"
    @FXML
    public void onGuardarProductoClick(ActionEvent event) {
        if (txtNombre.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El nombre del producto es obligatorio.");
            return;
        }
        if (cmbCategoria.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona una categoría.");
            return;
        }

        int idCategoria = obtenerIdCategoria(cmbCategoria.getValue());
        if (idCategoria == -1) return;

        String sql = "INSERT INTO TBL_PRODUCTO (nombre, descuento, cantidad_minima, cantidad_disponible, ubicacion, id_categoria) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, txtNombre.getText().trim());
            ps.setDouble(2, txtDescuento.getText().isBlank() ? 0 : Double.parseDouble(txtDescuento.getText().trim()));
            ps.setInt(3,    txtStockMinimo.getText().isBlank() ? 0 : Integer.parseInt(txtStockMinimo.getText().trim()));
            ps.setInt(4,    txtStockActual.getText().isBlank() ? 0 : Integer.parseInt(txtStockActual.getText().trim()));
            ps.setString(5, txtUbicacion.getText().trim());
            ps.setInt(6,    idCategoria);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "Producto guardado correctamente.");
            actualizarTabla();
            Limpiar();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al guardar: " + e.getMessage());
        }
    }

    // Botón "🗑 Eliminar Producto" → onAction="#onCancelarClick"
    @FXML
    public void onCancelarClick(ActionEvent event) {
        if (txtId.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Selecciona un producto de la tabla primero.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(null, "¿Eliminar este producto?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM TBL_PRODUCTO WHERE id_producto=?";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(txtId.getText().trim()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Producto eliminado.");
            actualizarTabla();
            Limpiar();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar: " + e.getMessage());
        }
    }

    // Botón "🛒 Añadir a Venta" → onAction="#onAñadirClick"
    @FXML
    public void onAñadirClick(ActionEvent event) {
        Producto sel = tablaProductos.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona un producto primero."); return; }
        int cantidad = spinCantidad.getValue();
        JOptionPane.showMessageDialog(null, "\"" + sel.getNombre() + "\" x" + cantidad + " listo para agregar.");
    }

    @FXML
    public void Limpiar() {
        txtId.clear();
        txtNombre.clear();
        txtStockActual.clear();
        txtStockMinimo.clear();
        txtDescuento.clear();
        txtUbicacion.clear();
        if (txtPrecio != null) txtPrecio.clear();
        cmbCategoria.setValue(null);
        tablaProductos.getSelectionModel().clearSelection();
    }

    private int obtenerIdCategoria(String nombre) {
        String sql = "SELECT id_categoria FROM TBL_CATEGORIA_DE_PRODUCTO WHERE nombre_categoria = ?";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_categoria");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar categoría: " + e.getMessage());
        }
        return -1;
    }

    private void actualizarTabla() {
        String sql = "SELECT p.id_producto, p.nombre, c.nombre_categoria, "
                + "p.cantidad_disponible, p.cantidad_minima, p.descuento, p.ubicacion "
                + "FROM TBL_PRODUCTO p "
                + "JOIN TBL_CATEGORIA_DE_PRODUCTO c ON c.id_categoria = p.id_categoria";

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ObservableList<Producto> lista = FXCollections.observableArrayList();
            while (rs.next()) {
                lista.add(new Producto(
                        rs.getInt("id_producto"),
                        rs.getString("nombre"),
                        rs.getString("nombre_categoria"),
                        rs.getInt("cantidad_disponible"),
                        rs.getInt("cantidad_minima"),
                        rs.getDouble("descuento"),
                        rs.getString("ubicacion")
                ));
            }
            tablaProductos.setItems(lista);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar productos: " + e.getMessage());
        }
    }

    private void cargarEnFormulario(Producto p) {
        txtId.setText(String.valueOf(p.getIdProducto()));
        txtNombre.setText(p.getNombre());
        cmbCategoria.setValue(p.getCategoria());
        txtStockActual.setText(String.valueOf(p.getStockActual()));
        txtStockMinimo.setText(String.valueOf(p.getStockMinimo()));
        txtDescuento.setText(String.valueOf(p.getDescuento()));
        txtUbicacion.setText(p.getUbicacion());
    }
}