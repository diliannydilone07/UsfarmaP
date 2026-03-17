package com.example.farmaventa.modelo;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Mapea TBL_PRODUCTO + JOIN TBL_CATEGORIA_DE_PRODUCTO
 * Columnas reales:
 *   id_producto, nombre, descuento, cantidad_minima,
 *   cantidad_disponible, ubicacion, id_categoria, nombre_categoria
 */
public class Producto {

    private final SimpleIntegerProperty idProducto   = new SimpleIntegerProperty();
    private final SimpleStringProperty  nombre       = new SimpleStringProperty();
    private final SimpleStringProperty  categoria    = new SimpleStringProperty(); // nombre_categoria
    private final SimpleIntegerProperty stockActual  = new SimpleIntegerProperty(); // cantidad_disponible
    private final SimpleIntegerProperty stockMinimo  = new SimpleIntegerProperty(); // cantidad_minima
    private final SimpleDoubleProperty  descuento    = new SimpleDoubleProperty();
    private final SimpleStringProperty  ubicacion    = new SimpleStringProperty();

    public Producto() {}

    public Producto(int idProducto, String nombre, String categoria,
                    int stockActual, int stockMinimo,
                    double descuento, String ubicacion) {
        this.idProducto.set(idProducto);
        this.nombre.set(nombre != null ? nombre : "");
        this.categoria.set(categoria != null ? categoria : "");
        this.stockActual.set(stockActual);
        this.stockMinimo.set(stockMinimo);
        this.descuento.set(descuento);
        this.ubicacion.set(ubicacion != null ? ubicacion : "");
    }

    public SimpleIntegerProperty idProductoProperty()  { return idProducto; }
    public SimpleStringProperty  nombreProperty()      { return nombre; }
    public SimpleStringProperty  categoriaProperty()   { return categoria; }
    public SimpleIntegerProperty stockActualProperty() { return stockActual; }
    public SimpleIntegerProperty stockMinimoProperty() { return stockMinimo; }
    public SimpleDoubleProperty  descuentoProperty()   { return descuento; }
    public SimpleStringProperty  ubicacionProperty()   { return ubicacion; }

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
    public double getDescuento()            { return descuento.get(); }
    public void   setDescuento(double v)    { descuento.set(v); }
    public String getUbicacion()            { return ubicacion.get(); }
    public void   setUbicacion(String v)    { ubicacion.set(v); }
}