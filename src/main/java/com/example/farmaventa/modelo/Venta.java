package com.example.farmaventa.modelo;

import javafx.beans.property.*;

/**
 * Modelo Venta según diagrama de clases.
 *
 * Atributos del diagrama:
 *   id, fecha, total, cantidad, subtotal, producto, cliente, empleado, estado
 *
 * En BD se guarda en TBL_VENTA (cabecera) y TBL_VENTA_PRODUCTO (detalle).
 * La tabla en pantalla muestra el JOIN: nombre del producto, nombre del cliente, etc.
 */
public class Venta {

    // Atributos del diagrama de clases
    private final SimpleIntegerProperty id        = new SimpleIntegerProperty();
    private final SimpleStringProperty  fecha     = new SimpleStringProperty();
    private final SimpleDoubleProperty  total     = new SimpleDoubleProperty();
    private final SimpleIntegerProperty cantidad  = new SimpleIntegerProperty();
    private final SimpleDoubleProperty  subtotal  = new SimpleDoubleProperty();
    private final SimpleStringProperty  producto  = new SimpleStringProperty(); // JOIN TBL_PRODUCTO
    private final SimpleStringProperty  cliente   = new SimpleStringProperty(); // JOIN TBL_PERSONA
    private final SimpleStringProperty  empleado  = new SimpleStringProperty(); // JOIN TBL_PERSONA
    private final SimpleStringProperty  estado    = new SimpleStringProperty(); // tipo_venta

    // IDs adicionales para operaciones de BD
    private int    idVenta;
    private int    idCliente;
    private int    idEmpleado;
    private String condicion;

    public Venta() {}

    public Venta(int idVenta, String fecha, double total, int cantidad, double subtotal,
                 String producto, String cliente, String empleado, String estado,
                 int idCliente, int idEmpleado, String condicion) {
        this.idVenta = idVenta;
        this.id.set(idVenta);
        this.fecha.set(fecha   != null ? fecha    : "");
        this.total.set(total);
        this.cantidad.set(cantidad);
        this.subtotal.set(subtotal);
        this.producto.set(producto != null ? producto : "");
        this.cliente.set(cliente   != null ? cliente  : "");
        this.empleado.set(empleado != null ? empleado : "");
        this.estado.set(estado     != null ? estado   : "");
        this.idCliente  = idCliente;
        this.idEmpleado = idEmpleado;
        this.condicion  = condicion;
    }

    // Métodos del diagrama
    public double calcularTotal()    { return total.get(); }
    public double calcularSubtotal() { return subtotal.get(); }

    // Properties para TableView
    public SimpleIntegerProperty idProperty()       { return id; }
    public SimpleStringProperty  fechaProperty()    { return fecha; }
    public SimpleDoubleProperty  totalProperty()    { return total; }
    public SimpleIntegerProperty cantidadProperty() { return cantidad; }
    public SimpleDoubleProperty  subtotalProperty() { return subtotal; }
    public SimpleStringProperty  productoProperty() { return producto; }
    public SimpleStringProperty  clienteProperty()  { return cliente; }
    public SimpleStringProperty  empleadoProperty() { return empleado; }
    public SimpleStringProperty  estadoProperty()   { return estado; }

    // Getters / Setters
    public int    getIdVenta()           { return idVenta; }
    public void   setIdVenta(int v)      { idVenta = v; id.set(v); }
    public String getFecha()             { return fecha.get(); }
    public void   setFecha(String v)     { fecha.set(v); }
    public double getTotal()             { return total.get(); }
    public void   setTotal(double v)     { total.set(v); }
    public int    getCantidad()          { return cantidad.get(); }
    public void   setCantidad(int v)     { cantidad.set(v); }
    public double getSubtotal()          { return subtotal.get(); }
    public void   setSubtotal(double v)  { subtotal.set(v); }
    public String getProducto()          { return producto.get(); }
    public void   setProducto(String v)  { producto.set(v); }
    public String getCliente()           { return cliente.get(); }
    public void   setCliente(String v)   { cliente.set(v); }
    public String getEmpleado()          { return empleado.get(); }
    public void   setEmpleado(String v)  { empleado.set(v); }
    public String getEstado()            { return estado.get(); }
    public void   setEstado(String v)    { estado.set(v); }
    public int    getIdCliente()         { return idCliente; }
    public void   setIdCliente(int v)    { idCliente = v; }
    public int    getIdEmpleado()        { return idEmpleado; }
    public void   setIdEmpleado(int v)   { idEmpleado = v; }
    public String getCondicion()         { return condicion; }
    public void   setCondicion(String v) { condicion = v; }
}