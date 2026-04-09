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

    // ── Formulario ────────────────────────────────────────────────────────
    @FXML private TextField        txtId;
    @FXML private TextField        txtNombre;
    @FXML private TextField        txtStockActual;
    @FXML private TextField        txtStockMinimo;
    @FXML private TextField        txtPrecio;
    @FXML private TextField        txtDescuento;
    @FXML private TextField        txtUbicacion;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TextField        txtBusqueda;
    @FXML private Spinner<Integer> spinCantidad;

    // ── Tabla ─────────────────────────────────────────────────────────────
    @FXML private TableView<Producto>           tablaProductos;
    @FXML private TableColumn<Producto, Number> colId;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, Number> colStock;
    @FXML private TableColumn<Producto, Number> colMinimo;
    @FXML private TableColumn<Producto, Number> colPrecio;
    @FXML private TableColumn<Producto, Number> colDescuento;
    @FXML private TableColumn<Producto, String> colUbicacion;

    // ── Lista de datos ────────────────────────────────────────────────────
    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList();

    // ── Inicializar ───────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        spinCantidad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));

        cargarComboCategorias();

        colId.setCellValueFactory(c -> c.getValue().idProductoProperty());
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colCategoria.setCellValueFactory(c -> c.getValue().categoriaProperty());
        colStock.setCellValueFactory(c -> c.getValue().stockActualProperty());
        colMinimo.setCellValueFactory(c -> c.getValue().stockMinimoProperty());
        colDescuento.setCellValueFactory(c -> c.getValue().descuentoProperty());
        colUbicacion.setCellValueFactory(c -> c.getValue().ubicacionProperty());
        if (colPrecio != null) colPrecio.setCellValueFactory(c -> c.getValue().descuentoProperty());

        tablaProductos.setItems(listaProductos);

        // Clic en fila → carga datos en formulario
        tablaProductos.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) cargarEnFormulario(sel);
        });

        actualizarTabla();
    }

    // ── Cargar combo categorías ───────────────────────────────────────────
    private void cargarComboCategorias() {
        String sql = "SELECT nombre_categoria FROM TBL_CATEGORIA_DE_PRODUCTO ORDER BY nombre_categoria";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cmbCategoria.getItems().add(rs.getString("nombre_categoria"));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar categorías: " + e.getMessage());
        }
    }

    // ── Buscar en tabla ───────────────────────────────────────────────────
    @FXML
    public void fnBuscar(ActionEvent event) {
        String busqueda = txtBusqueda.getText().trim().toLowerCase();
        if (busqueda.isEmpty()) {
            tablaProductos.setItems(listaProductos);
            return;
        }

        ObservableList<Producto> listaFiltrada = FXCollections.observableArrayList();
        for (Producto p : listaProductos) {
            if (p.getNombre().toLowerCase().contains(busqueda)
                    || p.getCategoria().toLowerCase().contains(busqueda)
                    || String.valueOf(p.getIdProducto()).contains(busqueda)) {
                listaFiltrada.add(p);
            }
        }
        tablaProductos.setItems(listaFiltrada);
    }

    // ── Guardar o editar según si hay ID ─────────────────────────────────
    @FXML
    public void onGuardarProductoClick(ActionEvent event) {
        if (txtNombre.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El nombre del producto es obligatorio."); return;
        }
        if (cmbCategoria.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona una categoría."); return;
        }

        int idCategoria = obtenerIdCategoria(cmbCategoria.getValue());
        if (idCategoria == -1) return;

        if (!txtId.getText().isBlank()) {
            editarProducto(idCategoria);
        } else {
            insertarProducto(idCategoria);
        }
    }

    // ── Insertar nuevo ────────────────────────────────────────────────────
    private void insertarProducto(int idCategoria) {
        String sql = "INSERT INTO TBL_PRODUCTO (nombre, descuento, cantidad_minima, cantidad_disponible, ubicacion, id_categoria) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
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

    // ── Editar existente ──────────────────────────────────────────────────
    private void editarProducto(int idCategoria) {
        String sql = "UPDATE TBL_PRODUCTO " +
                "SET nombre=?, descuento=?, cantidad_minima=?, cantidad_disponible=?, ubicacion=?, id_categoria=? " +
                "WHERE id_producto=?";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtNombre.getText().trim());
            ps.setDouble(2, txtDescuento.getText().isBlank() ? 0 : Double.parseDouble(txtDescuento.getText().trim()));
            ps.setInt(3,    txtStockMinimo.getText().isBlank() ? 0 : Integer.parseInt(txtStockMinimo.getText().trim()));
            ps.setInt(4,    txtStockActual.getText().isBlank() ? 0 : Integer.parseInt(txtStockActual.getText().trim()));
            ps.setString(5, txtUbicacion.getText().trim());
            ps.setInt(6,    idCategoria);
            ps.setInt(7,    Integer.parseInt(txtId.getText().trim()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Producto actualizado.");
            actualizarTabla();
            Limpiar();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al editar: " + e.getMessage());
        }
    }

    // ── Eliminar ──────────────────────────────────────────────────────────
    @FXML
    public void onCancelarClick(ActionEvent event) {
        if (txtId.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Selecciona un producto de la tabla primero."); return;
        }

        int confirm = JOptionPane.showConfirmDialog(null, "¿Eliminar este producto?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int idProducto = Integer.parseInt(txtId.getText().trim());

        try (Connection con = conexion.establecerConexion()) {
            // Eliminar dependencias
            PreparedStatement psMedIds = con.prepareStatement(
                    "SELECT id_medicamento FROM TBL_MEDICAMENTO WHERE id_producto=?");
            psMedIds.setInt(1, idProducto);
            ResultSet rsMed = psMedIds.executeQuery();
            while (rsMed.next()) {
                PreparedStatement psEnf = con.prepareStatement(
                        "DELETE FROM TBL_MEDICAMENTO_ENF_APL WHERE id_medicamento=?");
                psEnf.setInt(1, rsMed.getInt("id_medicamento"));
                psEnf.executeUpdate();
            }

            String[] tablasDependientes = {
                    "DELETE FROM TBL_VENTA_PRODUCTO              WHERE id_producto=?",
                    "DELETE FROM TBL_COMPRA_PRODUCTO             WHERE id_producto=?",
                    "DELETE FROM TBL_PRODUCTO_RECLAMACION_VENTA  WHERE id_producto=?",
                    "DELETE FROM TBL_PRODUCTO_RECLAMACION_COMPRA WHERE id_producto=?",
                    "DELETE FROM TBL_PRESENTACION_PRODUCTO       WHERE id_producto=?",
                    "DELETE FROM TBL_PRODUCTO_OFERTA             WHERE id_producto=?",
                    "DELETE FROM TBL_PERDIDA                     WHERE id_producto=?",
                    "DELETE FROM TBL_DETALLE_PEDIDO_C            WHERE id_producto=?",
                    "DELETE FROM TBL_CONVENIO                    WHERE id_producto=?",
                    "DELETE FROM TBL_MEDICAMENTO                 WHERE id_producto=?"
            };

            for (String sqlDep : tablasDependientes) {
                PreparedStatement ps2 = con.prepareStatement(sqlDep);
                ps2.setInt(1, idProducto);
                ps2.executeUpdate();
            }

            PreparedStatement psElim = con.prepareStatement(
                    "DELETE FROM TBL_PRODUCTO WHERE id_producto=?");
            psElim.setInt(1, idProducto);
            psElim.executeUpdate();

            JOptionPane.showMessageDialog(null, "Producto eliminado.");
            actualizarTabla();
            Limpiar();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar: " + e.getMessage());
        }
    }

    // ── Añadir al carrito ─────────────────────────────────────────────────
    @FXML
    public void onAñadirClick(ActionEvent event) {
        Producto sel = tablaProductos.getSelectionModel().getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(null, "Selecciona un producto primero."); return;
        }
        JOptionPane.showMessageDialog(null, "\"" + sel.getNombre() + "\" x" + spinCantidad.getValue() + " listo para agregar.");
    }

    // ── Limpiar formulario ────────────────────────────────────────────────
    @FXML
    public void Limpiar() {
        txtId.clear();
        txtNombre.clear();
        txtStockActual.clear();
        txtStockMinimo.clear();
        txtDescuento.clear();
        txtUbicacion.clear();
        txtBusqueda.clear();
        if (txtPrecio != null) txtPrecio.clear();
        cmbCategoria.setValue(null);
        tablaProductos.getSelectionModel().clearSelection();
        tablaProductos.setItems(listaProductos);
    }

    // ── Actualizar tabla ──────────────────────────────────────────────────
    private void actualizarTabla() {
        listaProductos.clear();
        String sql = "SELECT p.id_producto, p.nombre, c.nombre_categoria, " +
                "p.cantidad_disponible, p.cantidad_minima, p.descuento, p.ubicacion " +
                "FROM TBL_PRODUCTO p " +
                "JOIN TBL_CATEGORIA_DE_PRODUCTO c ON c.id_categoria = p.id_categoria " +
                "ORDER BY p.id_producto DESC";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                listaProductos.add(new Producto(
                        rs.getInt("id_producto"),
                        rs.getString("nombre"),
                        rs.getString("nombre_categoria"),
                        rs.getInt("cantidad_disponible"),
                        rs.getInt("cantidad_minima"),
                        rs.getDouble("descuento"),
                        rs.getString("ubicacion")));
            }
            tablaProductos.setItems(listaProductos);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar: " + e.getMessage());
        }
    }

    // ── Obtener ID de categoría por nombre ────────────────────────────────
    private int obtenerIdCategoria(String nombre) {
        String sql = "SELECT id_categoria FROM TBL_CATEGORIA_DE_PRODUCTO WHERE nombre_categoria=?";
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

    // ── Cargar fila seleccionada en formulario ────────────────────────────
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