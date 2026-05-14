package com.example.farmaventa.modelo;

import javafx.beans.property.*;


public class Producto {

    private final SimpleIntegerProperty idProducto     = new SimpleIntegerProperty();
    private final SimpleStringProperty  nombre         = new SimpleStringProperty();
    private final SimpleStringProperty  categoria      = new SimpleStringProperty();
    private final SimpleIntegerProperty stockActual    = new SimpleIntegerProperty();
    private final SimpleIntegerProperty stockMinimo    = new SimpleIntegerProperty();
    private final SimpleDoubleProperty  precio         = new SimpleDoubleProperty();
    private final SimpleDoubleProperty  descuento      = new SimpleDoubleProperty();
    private final SimpleStringProperty  ubicacion      = new SimpleStringProperty();
    private final SimpleStringProperty  presentacion   = new SimpleStringProperty();
    private final SimpleIntegerProperty idPresentacion = new SimpleIntegerProperty();

    public Producto() {}


    public Producto(int idProducto, String nombre, String categoria,
                    int stockActual, int stockMinimo,
                    double precio, double descuento, String ubicacion) {
        this(idProducto, nombre, categoria, stockActual, stockMinimo,
                precio, descuento, ubicacion, "", 0);
    }


    public Producto(int idProducto, String nombre, String categoria,
                    int stockActual, int stockMinimo,
                    double precio, double descuento, String ubicacion,
                    String presentacion, int idPresentacion) {
        this.idProducto.set(idProducto);
        this.nombre.set(nombre            != null ? nombre        : "");
        this.categoria.set(categoria       != null ? categoria     : "");
        this.stockActual.set(stockActual);
        this.stockMinimo.set(stockMinimo);
        this.precio.set(precio);
        this.descuento.set(descuento);
        this.ubicacion.set(ubicacion       != null ? ubicacion     : "");
        this.presentacion.set(presentacion  != null ? presentacion : "");
        this.idPresentacion.set(idPresentacion);
    }

    public SimpleIntegerProperty idProductoProperty()     { return idProducto; }
    public SimpleStringProperty  nombreProperty()         { return nombre; }
    public SimpleStringProperty  categoriaProperty()      { return categoria; }
    public SimpleIntegerProperty stockActualProperty()    { return stockActual; }
    public SimpleIntegerProperty stockMinimoProperty()    { return stockMinimo; }
    public SimpleDoubleProperty  precioProperty()         { return precio; }
    public SimpleDoubleProperty  descuentoProperty()      { return descuento; }
    public SimpleStringProperty  ubicacionProperty()      { return ubicacion; }
    public SimpleStringProperty  presentacionProperty()   { return presentacion; }
    public SimpleIntegerProperty idPresentacionProperty() { return idPresentacion; }

    public int    getIdProducto()           { return idProducto.get(); }
    public void   setIdProducto(int v)      { idProducto.set(v); }

    public String getNombre()               { return nombre.get(); }
    public void   setNombre(String v)       { nombre.set(v); }

    public String getCategoria()            { return categoria.get(); }
    public void   setCategoria(String v)    { categoria.set(v); }

    public int    getStockActual()          { return stockActual.get(); }
    public void   setStockActual(int v)     { stockActual.set(v); }

    public int    getStockMinimo()          { return stockMinimo.get(); }
    public void   setStockMinimo(int v)     { stockMinimo.set(v); }

    public double getPrecio()               { return precio.get(); }
    public void   setPrecio(double v)       { precio.set(v); }

    public double getDescuento()            { return descuento.get(); }
    public void   setDescuento(double v)    { descuento.set(v); }

    public String getUbicacion()            { return ubicacion.get(); }
    public void   setUbicacion(String v)    { ubicacion.set(v); }

    public String getPresentacion()         { return presentacion.get(); }
    public void   setPresentacion(String v) { presentacion.set(v); }

    public int    getIdPresentacion()       { return idPresentacion.get(); }
    public void   setIdPresentacion(int v)  { idPresentacion.set(v); }
}