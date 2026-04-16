package com.example.farmaventa;

import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.Producto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javax.swing.JOptionPane;
import java.sql.*;

public class SelectorProductoController {

    Conexion conexion = new Conexion();

    // ── Encabezado ────────────────────────────────────────────────────────
    @FXML private Label lblVentaActual;

    // ── Filtros ───────────────────────────────────────────────────────────
    @FXML private TextField        txtBuscar;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private Label            lblTotal;

    // ── Tabla ─────────────────────────────────────────────────────────────
    @FXML private TableView<Producto>           tablaProductos;
    @FXML private TableColumn<Producto, Number> colId;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, Number> colStock;
    @FXML private TableColumn<Producto, Number> colPrecio;
    @FXML private TableColumn<Producto, Number> colDescuento;
    @FXML private TableColumn<Producto, String> colUbicacion;

    // ── Panel detalle ─────────────────────────────────────────────────────
    @FXML private VBox  panelDetalle;
    @FXML private Label lblDetNombre;
    @FXML private Label lblDetId;
    @FXML private Label lblDetCategoria;
    @FXML private Label lblDetPrecio;
    @FXML private Label lblDetDescuento;
    @FXML private Label lblDetStock;
    @FXML private Label lblDetStockMin;
    @FXML private Label lblDetUbicacion;
    @FXML private Label lblDetPrecioFinal;

    // ── Sección agregar ───────────────────────────────────────────────────
    @FXML private TextField txtCantidad;
    @FXML private Label     lblPrecioFijo;         // muestra el precio — NO editable
    @FXML private Label     lblSubtotal;
    @FXML private Label     lblAdvertenciaPrecio;
    @FXML private Button    btnAgregarVenta;

    // ── Datos internos ────────────────────────────────────────────────────
    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private Producto productoSeleccionado = null;
    private double   precioOficialActivo  = 0.0;

    private HelloController ventaController;
    private StackPane       contentArea;

    // ── Inicializar ───────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> c.getValue().idProductoProperty());
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colCategoria.setCellValueFactory(c -> c.getValue().categoriaProperty());
        colStock.setCellValueFactory(c -> c.getValue().stockActualProperty());
        colPrecio.setCellValueFactory(c -> c.getValue().precioProperty());
        colDescuento.setCellValueFactory(c -> c.getValue().descuentoProperty());
        colUbicacion.setCellValueFactory(c -> c.getValue().ubicacionProperty());

        tablaProductos.setItems(listaProductos);
        tablaProductos.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> { if (sel != null) mostrarDetalle(sel); });

        // Actualizar subtotal al cambiar cantidad
        txtCantidad.textProperty().addListener((obs, o, n) -> calcularSubtotal());

        // Ocultar advertencia al inicio
        ocultarAdvertencia();

        cargarCategorias();
        cargarProductos();
    }

    // ── Inyección desde HelloController ──────────────────────────────────
    public void init(HelloController ventaCtrl, StackPane contentArea, String infoVenta) {
        this.ventaController = ventaCtrl;
        this.contentArea     = contentArea;
        if (infoVenta != null && !infoVenta.isBlank()) lblVentaActual.setText(infoVenta);
    }

    // ── Cargar categorías ─────────────────────────────────────────────────
    private void cargarCategorias() {
        cmbCategoria.getItems().add("Todas");
        String sql = "SELECT nombre_categoria FROM TBL_CATEGORIA_DE_PRODUCTO ORDER BY nombre_categoria";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cmbCategoria.getItems().add(rs.getString("nombre_categoria"));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar categorías: " + e.getMessage());
        }
        cmbCategoria.setValue("Todas");
    }

    // ── Cargar productos con precio desde TBL_PRESENTACION_PRODUCTO ───────
    private void cargarProductos() {
        listaProductos.clear();

        // LEFT JOIN a TBL_PRESENTACION_PRODUCTO para obtener el precio_venta.
        // Si un producto tiene varias presentaciones se toma el precio más bajo (MIN).
        // Si no tiene presentación registrada, precio = 0.
        String sql =
                "SELECT p.id_producto, p.nombre, c.nombre_categoria, " +
                        "       p.cantidad_disponible, p.cantidad_minima, p.descuento, p.ubicacion, " +
                        "       ISNULL(( " +
                        "           SELECT TOP 1 precio_venta " +
                        "           FROM TBL_PRESENTACION_PRODUCTO " +
                        "           WHERE id_producto = p.id_producto " +
                        "           ORDER BY id_presentacion ASC " +
                        "       ), 0) AS precio_venta " +
                        "FROM TBL_PRODUCTO p " +
                        "JOIN TBL_CATEGORIA_DE_PRODUCTO c ON c.id_categoria = p.id_categoria " +
                        "ORDER BY p.nombre ASC";

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
                        rs.getDouble("precio_venta"),   // precio real
                        rs.getDouble("descuento"),
                        rs.getString("ubicacion")
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar productos: " + e.getMessage());
        }
        tablaProductos.setItems(listaProductos);
        lblTotal.setText(listaProductos.size() + " productos");
    }

    // ── Filtrar ───────────────────────────────────────────────────────────
    @FXML
    public void onFiltrar(ActionEvent event) {
        String texto     = txtBuscar.getText().trim().toLowerCase();
        String categoria = cmbCategoria.getValue();
        ObservableList<Producto> filtrada = FXCollections.observableArrayList();
        for (Producto p : listaProductos) {
            boolean coincideTexto = texto.isEmpty()
                    || p.getNombre().toLowerCase().contains(texto)
                    || p.getCategoria().toLowerCase().contains(texto)
                    || String.valueOf(p.getIdProducto()).contains(texto);
            boolean coincideCategoria = categoria == null || categoria.equals("Todas")
                    || p.getCategoria().equals(categoria);
            if (coincideTexto && coincideCategoria) filtrada.add(p);
        }
        tablaProductos.setItems(filtrada);
        lblTotal.setText(filtrada.size() + " productos");
    }

    @FXML
    public void onLimpiarFiltro(ActionEvent event) {
        txtBuscar.clear();
        cmbCategoria.setValue("Todas");
        tablaProductos.setItems(listaProductos);
        lblTotal.setText(listaProductos.size() + " productos");
    }

    // ── Mostrar detalle ───────────────────────────────────────────────────
    private void mostrarDetalle(Producto p) {
        productoSeleccionado = p;
        precioOficialActivo  = p.getPrecio();

        lblDetNombre.setText(p.getNombre());
        lblDetId.setText(String.valueOf(p.getIdProducto()));
        lblDetCategoria.setText(p.getCategoria());
        lblDetStock.setText(String.valueOf(p.getStockActual()));
        lblDetStockMin.setText(String.valueOf(p.getStockMinimo()));
        lblDetDescuento.setText(p.getDescuento() + "%");
        lblDetUbicacion.setText(p.getUbicacion() != null && !p.getUbicacion().isBlank()
                ? p.getUbicacion() : "—");

        // Precio oficial (verde, solo lectura)
        if (precioOficialActivo > 0) {
            lblDetPrecio.setText("RD$ " + String.format("%.2f", precioOficialActivo));
            lblDetPrecio.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");
        } else {
            lblDetPrecio.setText("Sin precio registrado");
            lblDetPrecio.setStyle("-fx-font-size: 13px; -fx-text-fill: #9E9E9E; -fx-font-style: italic;");
        }

        // Precio con descuento
        double precioFinal = precioOficialActivo * (1.0 - p.getDescuento() / 100.0);
        lblDetPrecioFinal.setText("RD$ " + String.format("%.2f", precioFinal));

        // Stock: rojo si en o bajo el mínimo
        lblDetStock.setStyle(p.getStockActual() <= p.getStockMinimo()
                ? "-fx-font-size: 13px; -fx-text-fill: #C62828; -fx-font-weight: bold;"
                : "-fx-font-size: 13px; -fx-text-fill: #2E7D32;");

        // Mostrar precio fijo en la sección "Agregar" — NO editable
        if (lblPrecioFijo != null) {
            if (precioOficialActivo > 0) {
                lblPrecioFijo.setText("RD$ " + String.format("%.2f", precioOficialActivo));
                lblPrecioFijo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                        "-fx-text-fill: #2E7D32; -fx-background-color: #F1F8E9; " +
                        "-fx-background-radius: 6; -fx-padding: 6 10 6 10;");
            } else {
                lblPrecioFijo.setText("Sin precio");
                lblPrecioFijo.setStyle("-fx-font-size: 13px; -fx-text-fill: #9E9E9E; " +
                        "-fx-background-color: #F5F5F5; -fx-background-radius: 6; -fx-padding: 6 10 6 10;");
            }
        }

        if (txtCantidad.getText().isBlank()) txtCantidad.setText("1");
        ocultarAdvertencia();
        calcularSubtotal();
    }

    // ── Calcular subtotal ─────────────────────────────────────────────────
    private void calcularSubtotal() {
        if (precioOficialActivo <= 0) {
            lblSubtotal.setText("Subtotal: RD$ 0.00");
            return;
        }
        try {
            int cant = Integer.parseInt(txtCantidad.getText().trim());
            lblSubtotal.setText("Subtotal: RD$ " + String.format("%.2f", cant * precioOficialActivo));
        } catch (NumberFormatException ignored) {
            lblSubtotal.setText("Subtotal: RD$ 0.00");
        }
    }

    // ── Advertencia ───────────────────────────────────────────────────────
    private void mostrarAdvertencia(String mensaje) {
        if (lblAdvertenciaPrecio != null) {
            lblAdvertenciaPrecio.setText(mensaje);
            lblAdvertenciaPrecio.setVisible(true);
            lblAdvertenciaPrecio.setManaged(true);
        }
        if (btnAgregarVenta != null) {
            btnAgregarVenta.setDisable(true);
            btnAgregarVenta.setStyle(
                    "-fx-background-color: #BDBDBD; -fx-text-fill: #616161; " +
                            "-fx-font-weight: bold; -fx-font-size: 13px; " +
                            "-fx-background-radius: 6; -fx-padding: 10 0 10 0;");
        }
    }

    private void ocultarAdvertencia() {
        if (lblAdvertenciaPrecio != null) {
            lblAdvertenciaPrecio.setVisible(false);
            lblAdvertenciaPrecio.setManaged(false);
        }
        if (btnAgregarVenta != null) {
            btnAgregarVenta.setDisable(false);
            btnAgregarVenta.setStyle(
                    "-fx-background-color: #388E3C; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-font-size: 13px; " +
                            "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 0 10 0;");
        }
    }

    // ── Agregar producto a la venta ───────────────────────────────────────
    @FXML
    public void onSeleccionarProducto(ActionEvent event) {
        if (productoSeleccionado == null) {
            JOptionPane.showMessageDialog(null, "Selecciona un producto de la tabla primero."); return;
        }
        if (txtCantidad.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Ingresa la cantidad."); return;
        }
        if (precioOficialActivo <= 0) {
            JOptionPane.showMessageDialog(null,
                    "Este producto no tiene precio registrado.\n" +
                            "Registra el precio en Inventario antes de venderlo.",
                    "Sin precio", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int cant = Integer.parseInt(txtCantidad.getText().trim());
            if (cant <= 0) {
                JOptionPane.showMessageDialog(null, "La cantidad debe ser mayor a 0."); return;
            }
            if (ventaController != null) {
                ventaController.recibirProductoDelCatalogo(
                        productoSeleccionado.getIdProducto(),
                        productoSeleccionado.getNombre(),
                        cant,
                        precioOficialActivo   // siempre el precio de la BD
                );
            }
            volverAVentas();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "La cantidad debe ser un número entero.");
        }
    }

    // ── Ir a Inventario ───────────────────────────────────────────────────
    @FXML
    public void onIrAInventario(ActionEvent event) {
        if (contentArea == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/farmaventa/Inventario.fxml"));
            Node vista = loader.load();
            contentArea.getChildren().setAll(vista);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al abrir Inventario: " + e.getMessage());
        }
    }

    // ── Volver a Ventas ───────────────────────────────────────────────────
    @FXML
    public void onVolver(ActionEvent event) { volverAVentas(); }

    private void volverAVentas() {
        if (contentArea == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/farmaventa/Ventas.fxml"));
            Node ventasVista = loader.load();
            HelloController nuevoCtrl = loader.getController();
            if (ventaController != null) ventaController.restaurarEstadoEn(nuevoCtrl);
            contentArea.getChildren().setAll(ventasVista);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al volver a Ventas: " + e.getMessage());
        }
    }
}