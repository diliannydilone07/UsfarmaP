package com.example.farmaventa.controlador;

import Usuarios.Permisos;
import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.Producto;
import com.example.farmaventa.modelo.PresentacionDetalle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javax.swing.JOptionPane;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProductoController {

    Conexion conexion = new Conexion();



    @FXML private Button btnGuardarProducto;
    @FXML private Button btnAnadirStock;
    @FXML private Button btnEliminarDetalle;
    @FXML private Button btnEliminarProducto;   // ← NUEVO: botón "Eliminar Producto"

    @FXML private TextField        txtId;
    @FXML private TextField        txtNombre;
    @FXML private TextField        txtStockActual;
    @FXML private TextField        txtStockMinimo;
    @FXML private TextField        txtDescuento;
    @FXML private TextField        txtUbicacion;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TextField        txtBusqueda;
    @FXML private Spinner<Integer> spinCantidad;



    @FXML private ComboBox<String> cmbDetPresentacion;
    @FXML private TextField        txtNuevaPresentacion;
    @FXML private TextField        txtDetPrecio;
    @FXML private DatePicker       dpDetFechaCaducidad;

    @FXML private TableView<PresentacionDetalle>           tablaDetalle;
    @FXML private TableColumn<PresentacionDetalle, String> colDetNombre;
    @FXML private TableColumn<PresentacionDetalle, Number> colDetPrecio;
    @FXML private TableColumn<PresentacionDetalle, String> colDetFecha;

    private final ObservableList<PresentacionDetalle> listaDetalle =
            FXCollections.observableArrayList();

    private final Map<String, Integer> mapPresentaciones = new LinkedHashMap<>();



    @FXML private TableView<Producto>           tablaProductos;
    @FXML private TableColumn<Producto, Number> colId;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, Number> colStock;
    @FXML private TableColumn<Producto, Number> colMinimo;
    @FXML private TableColumn<Producto, Number> colPrecio;
    @FXML private TableColumn<Producto, Number> colDescuento;
    @FXML private TableColumn<Producto, String> colUbicacion;
    @FXML private TableColumn<Producto, String> colPresentacion;

    private final ObservableList<Producto> listaProductos =
            FXCollections.observableArrayList();



    @FXML
    public void initialize() {
        spinCantidad.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));

        cargarComboCategorias();
        cargarComboPresentaciones();

        colId.setCellValueFactory(c -> c.getValue().idProductoProperty());
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colCategoria.setCellValueFactory(c -> c.getValue().categoriaProperty());
        colStock.setCellValueFactory(c -> c.getValue().stockActualProperty());
        colMinimo.setCellValueFactory(c -> c.getValue().stockMinimoProperty());
        colPrecio.setCellValueFactory(c -> c.getValue().precioProperty());
        colDescuento.setCellValueFactory(c -> c.getValue().descuentoProperty());
        colUbicacion.setCellValueFactory(c -> c.getValue().ubicacionProperty());
        colPresentacion.setCellValueFactory(c -> c.getValue().presentacionProperty());

        tablaProductos.setItems(listaProductos);
        tablaProductos.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> { if (sel != null) cargarEnFormulario(sel); });

        colDetNombre.setCellValueFactory(c -> c.getValue().nombrePresentacionProperty());
        colDetPrecio.setCellValueFactory(c -> c.getValue().precioProperty());
        colDetFecha.setCellValueFactory(c -> c.getValue().fechaCaducidadProperty());
        tablaDetalle.setItems(listaDetalle);

        actualizarTabla();

        Permisos.aplicarBtn(btnGuardarProducto,  Permisos.Accion.REGISTRAR);
        Permisos.aplicarBtn(btnAnadirStock,      Permisos.Accion.EDITAR);
        Permisos.aplicarBtn(btnEliminarDetalle,  Permisos.Accion.ELIMINAR);
        Permisos.aplicarBtn(btnEliminarProducto, Permisos.Accion.ELIMINAR); // ← NUEVO
    }



    private void cargarComboCategorias() {
        String sql = "SELECT nombre_categoria FROM TBL_CATEGORIA_DE_PRODUCTO " +
                "ORDER BY nombre_categoria";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                cmbCategoria.getItems().add(rs.getString("nombre_categoria"));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar categorías: " + e.getMessage());
        }
    }

    private void cargarComboPresentaciones() {
        String sql = "SELECT id_presentacion, nombre FROM TBL_PRESENTACION ORDER BY nombre";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            mapPresentaciones.clear();
            cmbDetPresentacion.getItems().clear();
            while (rs.next()) {
                String nombre = rs.getString("nombre");
                int    id     = rs.getInt("id_presentacion");
                mapPresentaciones.put(nombre, id);
                cmbDetPresentacion.getItems().add(nombre);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error al cargar presentaciones: " + e.getMessage());
        }
    }



    @FXML
    public void onAgregarPresentacionDetalle(ActionEvent event) {
        String nombrePres = cmbDetPresentacion.getValue();
        String precioTxt  = txtDetPrecio.getText().trim();

        if (nombrePres == null) {
            JOptionPane.showMessageDialog(null, "Selecciona una presentación."); return;
        }
        if (precioTxt.isBlank()) {
            JOptionPane.showMessageDialog(null, "Ingresa el precio de venta."); return;
        }
        if (dpDetFechaCaducidad.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona la fecha de caducidad."); return;
        }

        int idPres = mapPresentaciones.getOrDefault(nombrePres, -1);
        if (idPres == -1) {
            JOptionPane.showMessageDialog(null, "Presentación no válida."); return;
        }

        for (PresentacionDetalle d : listaDetalle) {
            if (d.getIdPresentacion() == idPres) {
                JOptionPane.showMessageDialog(null,
                        "La presentación \"" + nombrePres + "\" ya está en la lista.\n" +
                                "Elimínala primero si deseas cambiar el precio.");
                return;
            }
        }

        double precio = 0;
        try { precio = Double.parseDouble(precioTxt); }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "El precio debe ser un número válido."); return;
        }

        String fecha = dpDetFechaCaducidad.getValue().toString();
        listaDetalle.add(new PresentacionDetalle(idPres, nombrePres, precio, fecha));

        cmbDetPresentacion.setValue(null);
        txtDetPrecio.clear();
        dpDetFechaCaducidad.setValue(null);
    }

    @FXML
    public void onQuitarPresentacionDetalle(ActionEvent event) {
        PresentacionDetalle sel = tablaDetalle.getSelectionModel().getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(null,
                    "Selecciona una fila de la tabla de presentaciones para quitarla.");
            return;
        }
        listaDetalle.remove(sel);
    }



    @FXML
    public void onCrearPresentacion(ActionEvent event) {
        String nombre = txtNuevaPresentacion.getText().trim();
        if (nombre.isBlank()) {
            JOptionPane.showMessageDialog(null, "Escribe el nombre de la nueva presentación."); return;
        }
        if (mapPresentaciones.containsKey(nombre)) {
            JOptionPane.showMessageDialog(null,
                    "Ya existe una presentación con ese nombre.\n" +
                            "Búscala en el combo de abajo.");
            return;
        }
        String sql = "INSERT INTO TBL_PRESENTACION (nombre) VALUES (?)";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
            int nuevoId = -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) nuevoId = keys.getInt(1);
            }
            if (nuevoId != -1) {
                mapPresentaciones.put(nombre, nuevoId);
                cmbDetPresentacion.getItems().add(nombre);
                cmbDetPresentacion.setValue(nombre);
                txtNuevaPresentacion.clear();
                JOptionPane.showMessageDialog(null,
                        "Presentación \"" + nombre + "\" creada y seleccionada.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al crear presentación: " + e.getMessage());
        }
    }



    @FXML
    public void fnBuscar() {
        String busqueda = txtBusqueda.getText().trim().toLowerCase();
        if (busqueda.isEmpty()) { tablaProductos.setItems(listaProductos); return; }
        ObservableList<Producto> filtrada = FXCollections.observableArrayList();
        for (Producto p : listaProductos) {
            if (p.getNombre().toLowerCase().contains(busqueda)
                    || p.getCategoria().toLowerCase().contains(busqueda)
                    || String.valueOf(p.getIdProducto()).contains(busqueda)) {
                filtrada.add(p);
            }
        }
        tablaProductos.setItems(filtrada);
    }



    @FXML
    public void onGuardarProductoClick(ActionEvent event) {
        if (txtNombre.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El nombre del producto es obligatorio."); return;
        }
        if (cmbCategoria.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona una categoría."); return;
        }
        if (listaDetalle.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Debes agregar al menos una presentación con precio y\n" +
                            "fecha de caducidad antes de guardar el producto.");
            return;
        }

        int idCategoria = obtenerIdCategoria(cmbCategoria.getValue());
        if (idCategoria == -1) return;

        if (!txtId.getText().isBlank()) editarProducto(idCategoria);
        else                            insertarProducto(idCategoria);
    }



    private void insertarProducto(int idCategoria) {
        String sqlProducto =
                "INSERT INTO TBL_PRODUCTO " +
                        "(nombre, descuento, cantidad_minima, cantidad_disponible, ubicacion, id_categoria) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(
                     sqlProducto, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, txtNombre.getText().trim());
            ps.setDouble(2, txtDescuento.getText().isBlank() ? 0
                    : Double.parseDouble(txtDescuento.getText().trim()));
            ps.setInt(3,    txtStockMinimo.getText().isBlank() ? 0
                    : Integer.parseInt(txtStockMinimo.getText().trim()));
            ps.setInt(4,    txtStockActual.getText().isBlank() ? 0
                    : Integer.parseInt(txtStockActual.getText().trim()));
            ps.setString(5, txtUbicacion.getText().trim());
            ps.setInt(6,    idCategoria);
            ps.executeUpdate();

            int idProducto = -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) idProducto = keys.getInt(1);
            }

            if (idProducto != -1 && !listaDetalle.isEmpty()) {
                insertarDetallePresentaciones(con, idProducto);
            }

            JOptionPane.showMessageDialog(null, "Producto guardado correctamente.");
            actualizarTabla();
            Limpiar();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al guardar: " + e.getMessage());
        }
    }



    private void editarProducto(int idCategoria) {
        int idProducto = Integer.parseInt(txtId.getText().trim());
        String sqlProducto =
                "UPDATE TBL_PRODUCTO " +
                        "SET nombre=?, descuento=?, cantidad_minima=?, " +
                        "    cantidad_disponible=?, ubicacion=?, id_categoria=? " +
                        "WHERE id_producto=?";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sqlProducto)) {

            ps.setString(1, txtNombre.getText().trim());
            ps.setDouble(2, txtDescuento.getText().isBlank() ? 0
                    : Double.parseDouble(txtDescuento.getText().trim()));
            ps.setInt(3,    txtStockMinimo.getText().isBlank() ? 0
                    : Integer.parseInt(txtStockMinimo.getText().trim()));
            ps.setInt(4,    txtStockActual.getText().isBlank() ? 0
                    : Integer.parseInt(txtStockActual.getText().trim()));
            ps.setString(5, txtUbicacion.getText().trim());
            ps.setInt(6,    idCategoria);
            ps.setInt(7,    idProducto);
            ps.executeUpdate();

            if (!listaDetalle.isEmpty()) {
                try (PreparedStatement psDel = con.prepareStatement(
                        "DELETE FROM TBL_PRESENTACION_PRODUCTO WHERE id_producto = ?")) {
                    psDel.setInt(1, idProducto);
                    psDel.executeUpdate();
                }
                insertarDetallePresentaciones(con, idProducto);
            }

            JOptionPane.showMessageDialog(null, "Producto actualizado.");
            actualizarTabla();
            Limpiar();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al editar: " + e.getMessage());
        }
    }



    private void insertarDetallePresentaciones(Connection con, int idProducto)
            throws SQLException {
        String sqlIns =
                "INSERT INTO TBL_PRESENTACION_PRODUCTO " +
                        "(id_producto, id_presentacion, precio_venta, fecha_caducidad) " +
                        "VALUES (?, ?, ?, ?)";
        try (PreparedStatement psIns = con.prepareStatement(sqlIns)) {
            for (PresentacionDetalle det : listaDetalle) {
                psIns.setInt(1,    idProducto);
                psIns.setInt(2,    det.getIdPresentacion());
                psIns.setDouble(3, det.getPrecio());
                psIns.setDate(4,   java.sql.Date.valueOf(det.getFechaCaducidad()));
                psIns.addBatch();
            }
            psIns.executeBatch();
        }
    }



    @FXML
    public void onCancelarClick(ActionEvent event) {
        if (txtId.getText().isBlank()) {
            JOptionPane.showMessageDialog(null,
                    "Selecciona un producto de la tabla primero."); return;
        }
        int confirm = JOptionPane.showConfirmDialog(null, "¿Eliminar este producto?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int idProducto = Integer.parseInt(txtId.getText().trim());

        try (Connection con = conexion.establecerConexion()) {
            try (PreparedStatement psMedIds = con.prepareStatement(
                    "SELECT id_medicamento FROM TBL_MEDICAMENTO WHERE id_producto=?")) {
                psMedIds.setInt(1, idProducto);
                try (ResultSet rsMed = psMedIds.executeQuery()) {
                    while (rsMed.next()) {
                        try (PreparedStatement psEnf = con.prepareStatement(
                                "DELETE FROM TBL_MEDICAMENTO_ENF_APL WHERE id_medicamento=?")) {
                            psEnf.setInt(1, rsMed.getInt("id_medicamento"));
                            psEnf.executeUpdate();
                        }
                    }
                }
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
                try (PreparedStatement ps2 = con.prepareStatement(sqlDep)) {
                    ps2.setInt(1, idProducto);
                    ps2.executeUpdate();
                }
            }
            try (PreparedStatement psElim = con.prepareStatement(
                    "DELETE FROM TBL_PRODUCTO WHERE id_producto=?")) {
                psElim.setInt(1, idProducto);
                psElim.executeUpdate();
            }

            JOptionPane.showMessageDialog(null, "Producto eliminado.");
            actualizarTabla();
            Limpiar();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar: " + e.getMessage());
        }
    }



    @FXML
    public void Limpiar() {
        txtId.clear();
        txtNombre.clear();
        txtStockActual.clear();
        txtStockMinimo.clear();
        txtDescuento.clear();
        txtUbicacion.clear();
        txtBusqueda.clear();
        cmbCategoria.setValue(null);

        cmbDetPresentacion.setValue(null);
        txtDetPrecio.clear();
        dpDetFechaCaducidad.setValue(null);
        listaDetalle.clear();

        tablaProductos.getSelectionModel().clearSelection();
        tablaProductos.setItems(listaProductos);
    }



    @FXML
    public void onAñadirClick(ActionEvent event) {
        Producto sel = tablaProductos.getSelectionModel().getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(null, "Selecciona un producto primero."); return;
        }
        JOptionPane.showMessageDialog(null,
                "\"" + sel.getNombre() + "\" x" + spinCantidad.getValue()
                        + " listo para agregar.");
    }



    public void actualizarTabla() {
        listaProductos.clear();
        String sql =
                "SELECT p.id_producto, p.nombre, c.nombre_categoria, " +
                        "       p.cantidad_disponible, p.cantidad_minima, p.descuento, p.ubicacion, " +
                        "       ISNULL(( " +
                        "           SELECT TOP 1 pp.precio_venta " +
                        "           FROM TBL_PRESENTACION_PRODUCTO pp " +
                        "           WHERE pp.id_producto = p.id_producto " +
                        "           ORDER BY pp.id_presentacion ASC " +
                        "       ), 0) AS precio_venta, " +
                        "       ISNULL(( " +
                        "           SELECT TOP 1 pr.nombre " +
                        "           FROM TBL_PRESENTACION_PRODUCTO pp " +
                        "           JOIN TBL_PRESENTACION pr ON pr.id_presentacion = pp.id_presentacion " +
                        "           WHERE pp.id_producto = p.id_producto " +
                        "           ORDER BY pp.id_presentacion ASC " +
                        "       ), '-') AS nombre_presentacion, " +
                        "       ISNULL(( " +
                        "           SELECT TOP 1 pp.id_presentacion " +
                        "           FROM TBL_PRESENTACION_PRODUCTO pp " +
                        "           WHERE pp.id_producto = p.id_producto " +
                        "           ORDER BY pp.id_presentacion ASC " +
                        "       ), 0) AS id_presentacion " +
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
                        rs.getDouble("precio_venta"),
                        rs.getDouble("descuento"),
                        rs.getString("ubicacion"),
                        rs.getString("nombre_presentacion"),
                        rs.getInt("id_presentacion")
                ));
            }
            tablaProductos.setItems(listaProductos);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar: " + e.getMessage());
        }
    }



    private int obtenerIdCategoria(String nombre) {
        String sql = "SELECT id_categoria FROM TBL_CATEGORIA_DE_PRODUCTO " +
                "WHERE nombre_categoria=?";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id_categoria");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error al buscar categoría: " + e.getMessage());
        }
        return -1;
    }

    private void cargarEnFormulario(Producto p) {
        txtId.setText(String.valueOf(p.getIdProducto()));
        txtNombre.setText(p.getNombre());
        cmbCategoria.setValue(p.getCategoria());
        txtStockActual.setText(String.valueOf(p.getStockActual()));
        txtStockMinimo.setText(String.valueOf(p.getStockMinimo()));
        txtDescuento.setText(String.valueOf(p.getDescuento()));
        txtUbicacion.setText(p.getUbicacion());

        listaDetalle.clear();
        cargarDetallePresentaciones(p.getIdProducto());
    }

    private void cargarDetallePresentaciones(int idProducto) {
        String sql =
                "SELECT pp.id_presentacion, pr.nombre, pp.precio_venta, pp.fecha_caducidad " +
                        "FROM TBL_PRESENTACION_PRODUCTO pp " +
                        "JOIN TBL_PRESENTACION pr ON pr.id_presentacion = pp.id_presentacion " +
                        "WHERE pp.id_producto = ? " +
                        "ORDER BY pp.id_presentacion ASC";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listaDetalle.add(new PresentacionDetalle(
                            rs.getInt("id_presentacion"),
                            rs.getString("nombre"),
                            rs.getDouble("precio_venta"),
                            rs.getDate("fecha_caducidad").toString()
                    ));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error al cargar presentaciones del producto: " + e.getMessage());
        }
    }

    public void precargarProducto(int idProducto) {
        for (Producto p : listaProductos) {
            if (p.getIdProducto() == idProducto) {
                cargarEnFormulario(p);
                tablaProductos.getSelectionModel().select(p);
                tablaProductos.scrollTo(p);
                return;
            }
        }
    }
}