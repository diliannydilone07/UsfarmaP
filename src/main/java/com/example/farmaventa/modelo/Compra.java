package com.example.farmaventa.modelo;

import javafx.beans.property.*;

/**
 * Modelo Compra — representa el JOIN de TBL_COMPRA + TBL_COMPRA_PRODUCTO + TBL_PRODUCTO
 * Cada fila en la TableView = un producto dentro de una compra.
 */
public class Compra {

    // ── Propiedades para TableView ────────────────────────────────────────
    private final SimpleIntegerProperty idCompra      = new SimpleIntegerProperty();
    private final SimpleStringProperty  proveedor     = new SimpleStringProperty();
    private final SimpleStringProperty  producto      = new SimpleStringProperty();
    private final SimpleIntegerProperty cantidad      = new SimpleIntegerProperty();
    private final SimpleDoubleProperty  precioUnitario = new SimpleDoubleProperty();
    private final SimpleDoubleProperty  subtotal      = new SimpleDoubleProperty();
    private final SimpleStringProperty  tipoCompra    = new SimpleStringProperty();
    private final SimpleStringProperty  fecha         = new SimpleStringProperty();
    private final SimpleStringProperty  condicion     = new SimpleStringProperty();

    // ── IDs necesarios para operaciones en BD ────────────────────────────
    private int    idProducto;
    private int    idProveedor;
    private int    idPedidoC;
    private int    idPresentacion;
    private double montoTotal;
    private double montoPendiente;

    public Compra() {}

    public Compra(int idCompra, String proveedor, String producto, int cantidad,
                  double precioUnitario, double subtotal, String tipoCompra,
                  String fecha, String condicion,
                  int idProducto, int idProveedor, int idPedidoC,
                  int idPresentacion, double montoTotal, double montoPendiente) {

        this.idCompra.set(idCompra);
        this.proveedor.set(proveedor   != null ? proveedor  : "");
        this.producto.set(producto     != null ? producto   : "");
        this.cantidad.set(cantidad);
        this.precioUnitario.set(precioUnitario);
        this.subtotal.set(subtotal);
        this.tipoCompra.set(tipoCompra != null ? tipoCompra : "");
        this.fecha.set(fecha           != null ? fecha      : "");
        this.condicion.set(condicion   != null ? condicion  : "");

        this.idProducto     = idProducto;
        this.idProveedor    = idProveedor;
        this.idPedidoC      = idPedidoC;
        this.idPresentacion = idPresentacion;
        this.montoTotal     = montoTotal;
        this.montoPendiente = montoPendiente;
    }

    // ── Properties ───────────────────────────────────────────────────────
    public SimpleIntegerProperty idCompraProperty()       { return idCompra; }
    public SimpleStringProperty  proveedorProperty()      { return proveedor; }
    public SimpleStringProperty  productoProperty()       { return producto; }
    public SimpleIntegerProperty cantidadProperty()       { return cantidad; }
    public SimpleDoubleProperty  precioUnitarioProperty() { return precioUnitario; }
    public SimpleDoubleProperty  subtotalProperty()       { return subtotal; }
    public SimpleStringProperty  tipoCompraProperty()     { return tipoCompra; }
    public SimpleStringProperty  fechaProperty()          { return fecha; }
    public SimpleStringProperty  condicionProperty()      { return condicion; }

    // ── Getters / Setters ─────────────────────────────────────────────────
    public int    getIdCompra()               { return idCompra.get(); }
    public void   setIdCompra(int v)          { idCompra.set(v); }
    public String getProveedor()              { return proveedor.get(); }
    public void   setProveedor(String v)      { proveedor.set(v); }
    public String getProducto()               { return producto.get(); }
    public void   setProducto(String v)       { producto.set(v); }
    public int    getCantidad()               { return cantidad.get(); }
    public void   setCantidad(int v)          { cantidad.set(v); }
    public double getPrecioUnitario()         { return precioUnitario.get(); }
    public void   setPrecioUnitario(double v) { precioUnitario.set(v); }
    public double getSubtotal()               { return subtotal.get(); }
    public void   setSubtotal(double v)       { subtotal.set(v); }
    public String getTipoCompra()             { return tipoCompra.get(); }
    public void   setTipoCompra(String v)     { tipoCompra.set(v); }
    public String getFecha()                  { return fecha.get(); }
    public void   setFecha(String v)          { fecha.set(v); }
    public String getCondicion()              { return condicion.get(); }
    public void   setCondicion(String v)      { condicion.set(v); }

    public int    getIdProducto()             { return idProducto; }
    public void   setIdProducto(int v)        { idProducto = v; }
    public int    getIdProveedor()            { return idProveedor; }
    public void   setIdProveedor(int v)       { idProveedor = v; }
    public int    getIdPedidoC()              { return idPedidoC; }
    public void   setIdPedidoC(int v)         { idPedidoC = v; }
    public int    getIdPresentacion()         { return idPresentacion; }
    public void   setIdPresentacion(int v)    { idPresentacion = v; }
    public double getMontoTotal()             { return montoTotal; }
    public void   setMontoTotal(double v)     { montoTotal = v; }
    public double getMontoPendiente()         { return montoPendiente; }
    public void   setMontoPendiente(double v) { montoPendiente = v; }

    // Métodos de negocio
    public double calcularSubtotal() { return precioUnitario.get() * cantidad.get(); }
}