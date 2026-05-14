package com.example.farmaventa.modelo;

import javafx.beans.property.*;


public class VentaSeguroItem {

    private final SimpleIntegerProperty idProducto       = new SimpleIntegerProperty();
    private final SimpleStringProperty  producto         = new SimpleStringProperty();
    private final SimpleIntegerProperty cantidad         = new SimpleIntegerProperty();
    private final SimpleDoubleProperty  precioUnitario   = new SimpleDoubleProperty();
    private final SimpleDoubleProperty  subtotal         = new SimpleDoubleProperty();
    private final SimpleDoubleProperty  porcentajeCobert = new SimpleDoubleProperty(); // % aseguradora
    private final SimpleDoubleProperty  porcentajeCli    = new SimpleDoubleProperty(); // % cliente

    private final SimpleDoubleProperty  montoAseguradora = new SimpleDoubleProperty();
    private final SimpleDoubleProperty  montoCliente     = new SimpleDoubleProperty();
    private int idPresentacion = 1;

    public VentaSeguroItem() {}

    public VentaSeguroItem(int idProducto, String producto, int cantidad,
                           double precioUnitario, double pctCobertura) {
        this.idProducto.set(idProducto);
        this.producto.set(producto != null ? producto : "");
        recalcular(cantidad, precioUnitario, pctCobertura);
    }


    public void recalcular(int cant, double precio, double pctCobertura) {
        double sub  = cant * precio;
        double aseg = sub * (pctCobertura / 100.0);
        cantidad.set(cant);
        precioUnitario.set(precio);
        subtotal.set(sub);
        porcentajeCobert.set(pctCobertura);
        porcentajeCli.set(100.0 - pctCobertura);
        montoAseguradora.set(aseg);
        montoCliente.set(sub - aseg);
    }

    public int    getIdProducto()       { return idProducto.get(); }
    public String getProducto()         { return producto.get(); }
    public int    getCantidad()         { return cantidad.get(); }
    public double getPrecioUnitario()   { return precioUnitario.get(); }
    public double getSubtotal()         { return subtotal.get(); }
    public double getPorcentajeCobert() { return porcentajeCobert.get(); }
    public double getPorcentajeCli()    { return porcentajeCli.get(); }
    public double getMontoAseguradora() { return montoAseguradora.get(); }
    public double getMontoCliente()     { return montoCliente.get(); }
    public int    getIdPresentacion()   { return idPresentacion; }
    public void   setIdPresentacion(int v) { idPresentacion = v; }

    public SimpleIntegerProperty idProductoProperty()       { return idProducto; }
    public SimpleStringProperty  productoProperty()         { return producto; }
    public SimpleIntegerProperty cantidadProperty()         { return cantidad; }
    public SimpleDoubleProperty  precioUnitarioProperty()   { return precioUnitario; }
    public SimpleDoubleProperty  subtotalProperty()         { return subtotal; }
    public SimpleDoubleProperty  porcentajeCobertProperty() { return porcentajeCobert; }
    public SimpleDoubleProperty  porcentajeCliProperty()    { return porcentajeCli; }
    public SimpleDoubleProperty  montoAseguradoraProperty() { return montoAseguradora; }
    public SimpleDoubleProperty  montoClienteProperty()     { return montoCliente; }
}